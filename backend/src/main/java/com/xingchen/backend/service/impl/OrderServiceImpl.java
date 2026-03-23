package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.entity.Product;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.OrderItemMapper;
import com.xingchen.backend.mapper.ProductMapper;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.event.NewOrderEvent;
import com.xingchen.backend.event.OrderStatusChangeEvent;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.OrderStatusService;
import com.xingchen.backend.util.idempotent.IdempotentUtil;
import org.springframework.context.ApplicationEventPublisher;
import com.xingchen.backend.dto.request.order.CreateOrderRequest;
import com.xingchen.backend.dto.request.order.OrderListRequest;
import com.xingchen.backend.dto.response.order.OrderDetailResponse;
import com.xingchen.backend.dto.response.order.OrderListResponse;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import com.xingchen.backend.util.lock.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final OrderStatusService orderStatusService;
    private final ApplicationEventPublisher eventPublisher;
    private final IdempotentUtil idempotentUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderRequest request) {
        // 生成幂等性key：基于用户ID+商品ID列表的hash
        String idempotentKey = "create_order:" + request.getUserId() + ":" + request.hashCode();

        // 尝试获取幂等性锁
        IdempotentUtil.IdempotentLock lock = idempotentUtil.tryLock(idempotentKey, 10);
        if (lock == null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "下单处理中，请勿重复提交");
        }

        try {
            // 验证商家
            Merchant merchant = merchantMapper.selectOneById(request.getMerchantId());
            if (merchant == null || merchant.getIsDeleted() == Constants.DeleteFlag.DELETED) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_EXIST, "商家不存在");
            }

            if (merchant.getStatus() != Constants.MerchantStatus.OPEN) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_OPEN, "商家未营业");
            }

            // 验证商品并扣减库存
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                // 查询商品
                Product product = productMapper.selectOneById(itemRequest.getProductId());

                if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
                    throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在");
                }

                if (product.getStatus() != Constants.ProductStatus.ON_SHELF) {
                    throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF, "商品已下架");
                }

                // 检查库存
                if (product.getStock() != Constants.Stock.UNLIMITED && product.getStock() < itemRequest.getQuantity()) {
                    throw new BusinessException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH,
                            "商品【" + product.getName() + "】库存不足");
                }

                // 扣减库存
                if (product.getStock() != Constants.Stock.UNLIMITED) {
                    int updateCount = productMapper.updateStock(product.getId(), itemRequest.getQuantity());
                    if (updateCount == 0) {
                        throw new BusinessException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH,
                                "商品【" + product.getName() + "】库存不足");
                    }
                }

                // 计算金额 - 使用下单时的价格（价格锁定）
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                // 创建订单明细 - 快照商品信息
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setPrice(product.getPrice());  // 下单时锁定价格
                orderItem.setProductImage(product.getImage());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setTotalAmount(itemTotal);
                orderItem.setCreateTime(LocalDateTime.now());

                orderItems.add(orderItem);
            }

            // 生成订单号
            String orderNo = generateOrderNo();

            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(request.getUserId());
            order.setMerchantId(request.getMerchantId());
            order.setTotalAmount(totalAmount);
            order.setActualAmount(totalAmount);  // 下单时锁定金额
            order.setStatus(Constants.OrderStatus.WAIT_PAY);
            order.setPayStatus(Constants.PayStatus.UNPAID);
            order.setRemark(request.getRemark());
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setContactPhone(request.getContactPhone());
            order.setContactName(request.getContactName());
            order.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 保存订单
            log.info("准备保存订单: orderNo={}, userId={}, merchantId={}, totalAmount={}", orderNo, request.getUserId(), request.getMerchantId(), totalAmount);
            orderMapper.insert(order);

            // 通过orderNo查询订单，获取生成的id
            Order savedOrder = orderMapper.selectByOrderNo(orderNo);
            if (savedOrder != null) {
                log.info("订单保存成功，生成的订单ID: {}", savedOrder.getId());

                // 保存订单明细
                for (OrderItem orderItem : orderItems) {
                    orderItem.setOrderId(savedOrder.getId());
                    log.info("准备保存订单明细: orderId={}, productId={}, quantity={}", savedOrder.getId(), orderItem.getProductId(), orderItem.getQuantity());
                    orderItemMapper.insert(orderItem);
                }
            } else {
                log.error("订单保存失败，无法通过orderNo查询到订单: {}", orderNo);
                throw new BusinessException(ErrorCode.ORDER_CREATE_FAILED, "订单创建失败");
            }

            // 发布订单创建事件
            eventPublisher.publishEvent(new NewOrderEvent(this, savedOrder.getId(), savedOrder.getMerchantId(), "新订单创建"));

            log.info("订单创建成功: orderNo={}, orderId={}", orderNo, savedOrder.getId());
            return savedOrder;
        } finally {
            // 释放幂等性锁
            lock.unlock();
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }
        return order;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }
        return order;
    }

    @Override
    public OrderListResponse getUserOrders(Long userId, OrderListRequest request) {
        // 查询订单列表
        List<Order> orders = orderMapper.selectUserOrders(userId, request.getStatus(), request.getPage(), request.getPageSize());

        // 查询总数
        Long total = orderMapper.countUserOrders(userId, request.getStatus());

        // 构建响应
        OrderListResponse response = new OrderListResponse();
        response.setOrders(orders);
        response.setTotal(total);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setTotalPages((int) Math.ceil((double) total / request.getPageSize()));

        return response;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权查看此订单");
        }

        // 查询订单明细
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);

        // 构建响应
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrder(order);
        response.setOrderItems(orderItems);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权取消此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法取消");
        }

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.CANCELLED, userId, 1, "用户取消订单");

        // 恢复库存
        restoreStock(orderId);

        // 更新订单
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, userId, order.getMerchantId(),
                Constants.OrderStatus.WAIT_PAY, Constants.OrderStatus.CANCELLED, "用户取消订单"));

        log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Integer paymentMethod) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法支付");
        }

        // 更新订单支付信息
        order.setPayMethod(paymentMethod);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.WAIT_ACCEPT, order.getUserId(), 1, "用户支付订单");

        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, order.getUserId(), order.getMerchantId(),
                Constants.OrderStatus.WAIT_PAY, Constants.OrderStatus.WAIT_ACCEPT, "用户支付订单"));

        log.info("订单支付成功: orderId={}, paymentMethod={}", orderId, paymentMethod);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(Long orderId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 状态冲突检查：订单已取消，不能再支付
        if (order.getStatus() == Constants.OrderStatus.CANCELLED) {
            log.error("严重状态冲突：用户支付但订单已取消！orderId={}, orderNo={}", orderId, order.getOrderNo());
            // 钱已付但订单取消了！需要人工介入处理
            // 这里抛出异常阻止支付，同时记录日志供人工处理
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                    "订单已取消，支付无效。如已扣款请请联系客服处理");
        }

        // 防重检查：只有待支付状态才能标记支付成功
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            if (order.getPayStatus() == Constants.PayStatus.PAID) {
                log.warn("订单已支付，忽略重复支付请求: orderId={}", orderId);
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已支付，请勿重复操作");
            }
            log.warn("订单状态错误，无法标记支付成功: orderId={}, status={}, payStatus={}", orderId, order.getStatus(), order.getPayStatus());
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误");
        }

        // 执行状态转换（transition 内部会设置 payStatus）
        orderStatusService.transition(order, Constants.OrderStatus.WAIT_ACCEPT, order.getUserId(), 1, "支付成功");

        // 设置支付相关信息
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, order.getUserId(), order.getMerchantId(),
                Constants.OrderStatus.WAIT_PAY, Constants.OrderStatus.WAIT_ACCEPT, "支付成功"));

        log.info("订单支付成功: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId, Long merchantId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证商家权限
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权处理此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_ACCEPT) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法接单");
        }

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.MAKING, merchantId, 2, "商家接单");

        order.setAcceptTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, order.getUserId(), merchantId,
                Constants.OrderStatus.WAIT_ACCEPT, Constants.OrderStatus.MAKING, "商家接单"));

        log.info("订单接单成功: orderId={}, merchantId={}", orderId, merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long orderId, Long merchantId, String reason) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证商家权限
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权处理此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_ACCEPT) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法拒单");
        }

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.REFUSED, merchantId, 2, reason);

        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 恢复库存
        restoreStock(orderId);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, order.getUserId(), merchantId,
                Constants.OrderStatus.WAIT_ACCEPT, Constants.OrderStatus.REFUSED, reason));

        log.info("订单拒单成功: orderId={}, merchantId={}, reason={}", orderId, merchantId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long merchantId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证商家权限
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权处理此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.MAKING) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法完成");
        }

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.WAIT_PICKUP, merchantId, 2, "订单制作完成");

        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, order.getUserId(), merchantId,
                Constants.OrderStatus.MAKING, Constants.OrderStatus.WAIT_PICKUP, "订单制作完成"));

        log.info("订单完成制作: orderId={}, merchantId={}", orderId, merchantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void pickupOrder(Long orderId, Long userId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PICKUP) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法取餐");
        }

        // 执行状态转换
        orderStatusService.transition(order, Constants.OrderStatus.COMPLETED, userId, 1, "用户取餐");

        order.setFinishTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 发布订单状态变更事件
        eventPublisher.publishEvent(new OrderStatusChangeEvent(this, orderId, userId, order.getMerchantId(),
                Constants.OrderStatus.WAIT_PICKUP, Constants.OrderStatus.COMPLETED, "用户取餐"));

        log.info("订单取餐完成: orderId={}, userId={}", orderId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId, Long userId) {
        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权删除此订单");
        }

        // 逻辑删除
        order.setIsDeleted(Constants.DeleteFlag.DELETED);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        log.info("订单删除成功: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 恢复库存
     */
    private void restoreStock(Long orderId) {
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem orderItem : orderItems) {
            productMapper.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    @Override
    public OrderListResponse getMerchantOrderList(Long merchantId, Integer status, int page, int size) {
        // 查询订单列表
        List<Order> orders;
        if (status != null) {
            orders = orderMapper.selectByMerchantIdAndStatus(merchantId, status, size, (page - 1) * size);
        } else {
            orders = orderMapper.selectByMerchantId(merchantId, size, (page - 1) * size);
        }

        // 查询总数
        long total;
        if (status != null) {
            total = orderMapper.countByMerchantIdAndStatus(merchantId, status);
        } else {
            total = orderMapper.countByMerchantId(merchantId);
        }

        // 构建响应
        OrderListResponse response = new OrderListResponse();
        response.setOrders(orders);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(size);
        response.setTotalPages((int) Math.ceil((double) total / size));

        return response;
    }

    @Override
    public Map<String, Object> getMerchantOrderList(OrderListRequest request) {
        Long merchantId = request.getMerchantId();
        Integer status = request.getStatus();
        int page = request.getPage();
        int pageSize = request.getPageSize();

        // 查询订单列表
        List<Order> orders;
        if (status != null) {
            orders = orderMapper.selectByMerchantIdAndStatus(merchantId, status, pageSize, (page - 1) * pageSize);
        } else {
            orders = orderMapper.selectByMerchantId(merchantId, pageSize, (page - 1) * pageSize);
        }

        // 查询总数
        long total;
        if (status != null) {
            total = orderMapper.countByMerchantIdAndStatus(merchantId, status);
        } else {
            total = orderMapper.countByMerchantId(merchantId);
        }

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));

        return result;
    }

    @Override
    public void refuseOrder(Long orderId, Long merchantId, String reason) {
        // 调用rejectOrder实现
        rejectOrder(orderId, merchantId, reason);
    }

    @Override
    public void startMaking(Long orderId, Long merchantId) {
        // 调用acceptOrder实现
        acceptOrder(orderId, merchantId);
    }

    @Override
    public void prepareOrder(Long orderId, Long merchantId) {
        // 调用acceptOrder实现
        acceptOrder(orderId, merchantId);
    }

    @Override
    public void finishMaking(Long orderId, Long merchantId) {
        // 调用completeOrder实现
        completeOrder(orderId, merchantId);
    }

    @Override
    public OrderListResponse getOrderList(OrderListRequest request) {
        OrderListResponse response = new OrderListResponse();

        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;

        List<Order> orders = orderMapper.selectUserOrders(
                request.getUserId(),
                request.getStatus(),
                page,
                pageSize
        );

        Long total = orderMapper.countUserOrders(request.getUserId(), request.getStatus());

        // 查询订单明细
        Map<Long, List<OrderItem>> orderItemsMap = new HashMap<>();
        if (orders != null && !orders.isEmpty()) {
            for (Order order : orders) {
                List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                orderItemsMap.put(order.getId(), items);
            }
        }

        response.setOrderList(orders);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalPages((int) Math.ceil((double) total / pageSize));
        response.setOrderItemsMap(orderItemsMap);
        return response;
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectOneById(orderId);
        if (order != null) {
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }

    @Override
    public void updatePaymentStatus(Long orderId, Integer paymentStatus) {
        Order order = orderMapper.selectOneById(orderId);
        if (order != null) {
            order.setPayStatus(paymentStatus);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }

    @Override
    public void confirmOrder(Long orderId, Long userId) {
        // 调用pickupOrder实现
        pickupOrder(orderId, userId);
    }

    @Override
    public void confirmPickup(Long orderId, Long userId) {
        // 调用pickupOrder实现
        pickupOrder(orderId, userId);
    }

    @Override
    public Map<String, Object> getOrderStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOrders", 0);
        statistics.put("pendingOrders", 0);
        statistics.put("completedOrders", 0);
        return statistics;
    }

    @Override
    public void confirmReceipt(Long orderId) {
        // 确认收货逻辑
        Order order = orderMapper.selectOneById(orderId);
        if (order != null) {
            order.setStatus(Constants.OrderStatus.COMPLETED);
            order.setFinishTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public void rollbackStock(Long orderId) {
        restoreStock(orderId);
    }
}
