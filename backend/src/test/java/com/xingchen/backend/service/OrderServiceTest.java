package com.xingchen.backend.service;

import com.xingchen.backend.dto.request.order.CreateOrderRequest;
import com.xingchen.backend.dto.request.order.OrderListRequest;
import com.xingchen.backend.dto.response.order.OrderDetailResponse;
import com.xingchen.backend.dto.response.order.OrderListResponse;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.entity.Product;
import com.xingchen.backend.event.NewOrderEvent;
import com.xingchen.backend.event.OrderStatusChangeEvent;
import com.xingchen.backend.exception.BusinessException;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.mapper.OrderItemMapper;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.ProductMapper;
import com.xingchen.backend.service.impl.OrderServiceImpl;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private OrderStatusService orderStatusService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Merchant testMerchant;
    private Product testProduct;
    private Order testOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        testMerchant = new Merchant();
        testMerchant.setId(1L);
        testMerchant.setName("测试商家");
        testMerchant.setStatus(Constants.MerchantStatus.OPEN);
        testMerchant.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setMerchantId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("20.00"));
        testProduct.setStock(100);
        testProduct.setStatus(Constants.ProductStatus.ON_SHELF);
        testProduct.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD123456789");
        testOrder.setUserId(1L);
        testOrder.setMerchantId(1L);
        testOrder.setTotalAmount(new BigDecimal("40.00"));
        testOrder.setActualAmount(new BigDecimal("40.00"));
        testOrder.setStatus(Constants.OrderStatus.WAIT_PAY);
        testOrder.setPayStatus(Constants.PayStatus.UNPAID);
        testOrder.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        testOrder.setCreateTime(LocalDateTime.now());
        testOrder.setUpdateTime(LocalDateTime.now());

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1L);
        createOrderRequest.setMerchantId(1L);
        createOrderRequest.setDeliveryAddress("测试地址");
        createOrderRequest.setContactPhone("13800138000");
        createOrderRequest.setContactName("测试用户");
        createOrderRequest.setRemark("测试备注");

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setProductName("测试商品");
        itemRequest.setProductPrice(new BigDecimal("20.00"));
        itemRequest.setQuantity(2);
        createOrderRequest.setOrderItems(Collections.singletonList(itemRequest));
    }

    @Test
    @DisplayName("测试订单创建 - 正常场景")
    void testCreateOrder_Success() {
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(testProduct);
        when(productMapper.updateStock(1L, 2)).thenReturn(1);
        when(orderMapper.selectByOrderNo(anyString())).thenReturn(testOrder);

        Order result = orderService.createOrder(createOrderRequest);

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(merchantMapper, times(1)).selectOneById(1L);
        verify(productMapper, times(1)).selectOneById(1L);
        verify(productMapper, times(1)).updateStock(1L, 2);
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
        verify(eventPublisher, times(1)).publishEvent(any(NewOrderEvent.class));
    }

    @Test
    @DisplayName("测试订单创建 - 商家不存在")
    void testCreateOrder_MerchantNotExist() {
        when(merchantMapper.selectOneById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.MERCHANT_NOT_EXIST, exception.getCode());
        verify(merchantMapper, times(1)).selectOneById(1L);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("测试订单创建 - 商家已删除")
    void testCreateOrder_MerchantDeleted() {
        testMerchant.setIsDeleted(Constants.DeleteFlag.DELETED);
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.MERCHANT_NOT_EXIST, exception.getCode());
    }

    @Test
    @DisplayName("测试订单创建 - 商家未营业")
    void testCreateOrder_MerchantNotOpen() {
        testMerchant.setStatus(Constants.MerchantStatus.CLOSED);
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.MERCHANT_NOT_OPEN, exception.getCode());
    }

    @Test
    @DisplayName("测试订单创建 - 商品不存在")
    void testCreateOrder_ProductNotExist() {
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.PRODUCT_NOT_EXIST, exception.getCode());
    }

    @Test
    @DisplayName("测试订单创建 - 商品已下架")
    void testCreateOrder_ProductOffShelf() {
        testProduct.setStatus(Constants.ProductStatus.OFF_SHELF);
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(testProduct);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.PRODUCT_OFF_SHELF, exception.getCode());
    }

    @Test
    @DisplayName("测试订单创建 - 库存不足")
    void testCreateOrder_StockNotEnough() {
        testProduct.setStock(1);
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(testProduct);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH, exception.getCode());
    }

    @Test
    @DisplayName("测试订单创建 - 无限库存商品")
    void testCreateOrder_UnlimitedStock() {
        testProduct.setStock(Constants.Stock.UNLIMITED);
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(testProduct);
        when(orderMapper.selectByOrderNo(anyString())).thenReturn(testOrder);

        Order result = orderService.createOrder(createOrderRequest);

        assertNotNull(result);
        verify(productMapper, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("测试订单创建 - 库存扣减失败")
    void testCreateOrder_UpdateStockFail() {
        when(merchantMapper.selectOneById(1L)).thenReturn(testMerchant);
        when(productMapper.selectOneById(1L)).thenReturn(testProduct);
        when(productMapper.updateStock(1L, 2)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        assertEquals(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH, exception.getCode());
    }

    @Test
    @DisplayName("测试根据ID查询订单 - 成功")
    void testGetOrderById_Success() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(orderMapper, times(1)).selectOneById(1L);
    }

    @Test
    @DisplayName("测试根据ID查询订单 - 订单不存在")
    void testGetOrderById_NotExist() {
        when(orderMapper.selectOneById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.getOrderById(1L);
        });
        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getCode());
    }

    @Test
    @DisplayName("测试根据ID查询订单 - 订单已删除")
    void testGetOrderById_Deleted() {
        testOrder.setIsDeleted(Constants.DeleteFlag.DELETED);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.getOrderById(1L);
        });
        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getCode());
    }

    @Test
    @DisplayName("测试根据订单号查询订单 - 成功")
    void testGetOrderByOrderNo_Success() {
        when(orderMapper.selectByOrderNo("ORD123456789")).thenReturn(testOrder);

        Order result = orderService.getOrderByOrderNo("ORD123456789");

        assertNotNull(result);
        assertEquals("ORD123456789", result.getOrderNo());
    }

    @Test
    @DisplayName("测试获取用户订单列表")
    void testGetUserOrders() {
        OrderListRequest request = new OrderListRequest();
        request.setStatus(Constants.OrderStatus.WAIT_PAY);
        request.setPage(1);
        request.setPageSize(10);

        List<Order> orders = Collections.singletonList(testOrder);
        when(orderMapper.selectUserOrders(1L, Constants.OrderStatus.WAIT_PAY, 1, 10)).thenReturn(orders);
        when(orderMapper.countUserOrders(1L, Constants.OrderStatus.WAIT_PAY)).thenReturn(1L);

        OrderListResponse result = orderService.getUserOrders(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getOrders().size());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("测试获取订单详情 - 成功")
    void testGetOrderDetail_Success() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(new ArrayList<>());

        OrderDetailResponse result = orderService.getOrderDetail(1L, 1L);

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getOrder().getId());
    }

    @Test
    @DisplayName("测试获取订单详情 - 无权查看")
    void testGetOrderDetail_NoPermission() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.getOrderDetail(1L, 999L);
        });
        assertEquals(ErrorCode.USER_NO_PERMISSION, exception.getCode());
    }

    @Test
    @DisplayName("测试取消订单 - 成功")
    void testCancelOrder_Success() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.CANCELLED), anyLong(), anyInt(), anyString());
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(new ArrayList<>());

        orderService.cancelOrder(1L, 1L);

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.CANCELLED), eq(1L), eq(1), eq("用户取消订单"));
        verify(orderMapper, times(1)).update(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangeEvent.class));
    }

    @Test
    @DisplayName("测试取消订单 - 订单不存在")
    void testCancelOrder_NotExist() {
        when(orderMapper.selectOneById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getCode());
    }

    @Test
    @DisplayName("测试取消订单 - 无权取消")
    void testCancelOrder_NoPermission() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(1L, 999L);
        });
        assertEquals(ErrorCode.USER_NO_PERMISSION, exception.getCode());
    }

    @Test
    @DisplayName("测试取消订单 - 状态错误")
    void testCancelOrder_StatusError() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
        assertEquals(ErrorCode.ORDER_STATUS_ERROR, exception.getCode());
    }

    @Test
    @DisplayName("测试支付订单 - 成功")
    void testPayOrder_Success() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.WAIT_ACCEPT), anyLong(), anyInt(), anyString());

        orderService.payOrder(1L, Constants.PaymentMethod.ALIPAY);

        verify(orderMapper, times(1)).update(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangeEvent.class));
    }

    @Test
    @DisplayName("测试支付订单 - 订单状态错误")
    void testPayOrder_StatusError() {
        testOrder.setStatus(Constants.OrderStatus.COMPLETED);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.payOrder(1L, Constants.PaymentMethod.ALIPAY);
        });
        assertEquals(ErrorCode.ORDER_STATUS_ERROR, exception.getCode());
    }

    @Test
    @DisplayName("测试支付成功回调 - 成功")
    void testPaySuccess_Success() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.WAIT_ACCEPT), anyLong(), anyInt(), anyString());

        orderService.paySuccess(1L);

        verify(orderMapper, times(1)).update(any(Order.class));
    }

    @Test
    @DisplayName("测试支付成功回调 - 状态错误时忽略")
    void testPaySuccess_StatusError() {
        testOrder.setStatus(Constants.OrderStatus.COMPLETED);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        orderService.paySuccess(1L);

        verify(orderMapper, never()).update(any(Order.class));
    }

    @Test
    @DisplayName("测试商家接单 - 成功")
    void testAcceptOrder_Success() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.MAKING), anyLong(), anyInt(), anyString());

        orderService.acceptOrder(1L, 1L);

        verify(orderMapper, times(1)).update(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangeEvent.class));
    }

    @Test
    @DisplayName("测试商家接单 - 无权操作")
    void testAcceptOrder_NoPermission() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.acceptOrder(1L, 999L);
        });
        assertEquals(ErrorCode.USER_NO_PERMISSION, exception.getCode());
    }

    @Test
    @DisplayName("测试商家拒单 - 成功")
    void testRejectOrder_Success() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.REFUSED), anyLong(), anyInt(), anyString());
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(new ArrayList<>());

        orderService.rejectOrder(1L, 1L, "库存不足");

        verify(orderMapper, times(1)).update(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangeEvent.class));
    }

    @Test
    @DisplayName("测试完成订单制作 - 成功")
    void testCompleteOrder_Success() {
        testOrder.setStatus(Constants.OrderStatus.MAKING);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.WAIT_PICKUP), anyLong(), anyInt(), anyString());

        orderService.completeOrder(1L, 1L);

        verify(orderMapper, times(1)).update(any(Order.class));
    }

    @Test
    @DisplayName("测试用户取餐 - 成功")
    void testPickupOrder_Success() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_PICKUP);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.COMPLETED), anyLong(), anyInt(), anyString());

        orderService.pickupOrder(1L, 1L);

        verify(orderMapper, times(1)).update(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangeEvent.class));
    }

    @Test
    @DisplayName("测试删除订单 - 成功")
    void testDeleteOrder_Success() {
        testOrder.setStatus(Constants.OrderStatus.COMPLETED);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        orderService.deleteOrder(1L, 1L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper, times(1)).update(orderCaptor.capture());
        assertEquals(Constants.DeleteFlag.DELETED, orderCaptor.getValue().getIsDeleted());
    }

    @Test
    @DisplayName("测试获取商家订单列表 - 带状态筛选")
    void testGetMerchantOrderList_WithStatus() {
        List<Order> orders = Collections.singletonList(testOrder);
        when(orderMapper.selectByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY, 10, 0)).thenReturn(orders);
        when(orderMapper.countByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY)).thenReturn(1L);

        OrderListResponse result = orderService.getMerchantOrderList(1L, Constants.OrderStatus.WAIT_PAY, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getOrders().size());
    }

    @Test
    @DisplayName("测试获取商家订单列表 - 不带状态筛选")
    void testGetMerchantOrderList_WithoutStatus() {
        List<Order> orders = Collections.singletonList(testOrder);
        when(orderMapper.selectByMerchantId(1L, 10, 0)).thenReturn(orders);
        when(orderMapper.countByMerchantId(1L)).thenReturn(1L);

        OrderListResponse result = orderService.getMerchantOrderList(1L, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getOrders().size());
    }

    @Test
    @DisplayName("测试获取商家订单列表(Map返回)")
    void testGetMerchantOrderList_Map() {
        OrderListRequest request = new OrderListRequest();
        request.setMerchantId(1L);
        request.setStatus(Constants.OrderStatus.WAIT_PAY);
        request.setPage(1);
        request.setPageSize(10);

        List<Order> orders = Collections.singletonList(testOrder);
        when(orderMapper.selectByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY, 10, 0)).thenReturn(orders);
        when(orderMapper.countByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY)).thenReturn(1L);

        Map<String, Object> result = orderService.getMerchantOrderList(request);

        assertNotNull(result);
        assertEquals(orders, result.get("orders"));
        assertEquals(1L, result.get("total"));
    }

    @Test
    @DisplayName("测试更新订单状态")
    void testUpdateOrderStatus() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        orderService.updateOrderStatus(1L, Constants.OrderStatus.WAIT_ACCEPT);

        verify(orderMapper, times(1)).update(any(Order.class));
    }

    @Test
    @DisplayName("测试更新订单状态 - 订单不存在")
    void testUpdateOrderStatus_NotExist() {
        when(orderMapper.selectOneById(1L)).thenReturn(null);

        orderService.updateOrderStatus(1L, Constants.OrderStatus.WAIT_ACCEPT);

        verify(orderMapper, never()).update(any(Order.class));
    }

    @Test
    @DisplayName("测试更新支付状态")
    void testUpdatePaymentStatus() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        orderService.updatePaymentStatus(1L, Constants.PayStatus.PAID);

        verify(orderMapper, times(1)).update(any(Order.class));
    }

    @Test
    @DisplayName("测试确认收货")
    void testConfirmReceipt() {
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);

        orderService.confirmReceipt(1L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper, times(1)).update(orderCaptor.capture());
        assertEquals(Constants.OrderStatus.COMPLETED, orderCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("测试获取订单商品项")
    void testGetOrderItems() {
        List<OrderItem> items = Arrays.asList(new OrderItem(), new OrderItem());
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(items);

        List<OrderItem> result = orderService.getOrderItems(1L);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("测试回滚库存")
    void testRollbackStock() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(Collections.singletonList(item));

        orderService.rollbackStock(1L);

        verify(productMapper, times(1)).restoreStock(1L, 2);
    }

    @Test
    @DisplayName("测试获取订单统计")
    void testGetOrderStatistics() {
        Map<String, Object> result = orderService.getOrderStatistics(1L);

        assertNotNull(result);
        assertEquals(0, result.get("totalOrders"));
        assertEquals(0, result.get("pendingOrders"));
        assertEquals(0, result.get("completedOrders"));
    }

    @Test
    @DisplayName("测试获取订单列表 - 默认空列表")
    void testGetOrderList() {
        OrderListRequest request = new OrderListRequest();
        request.setPage(1);
        request.setPageSize(10);

        OrderListResponse result = orderService.getOrderList(request);

        assertNotNull(result);
        assertTrue(result.getOrders().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    @DisplayName("测试refuseOrder方法调用rejectOrder")
    void testRefuseOrder() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.REFUSED), anyLong(), anyInt(), anyString());
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(new ArrayList<>());

        orderService.refuseOrder(1L, 1L, "测试原因");

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.REFUSED), eq(1L), eq(2), eq("测试原因"));
    }

    @Test
    @DisplayName("测试startMaking方法调用acceptOrder")
    void testStartMaking() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.MAKING), anyLong(), anyInt(), anyString());

        orderService.startMaking(1L, 1L);

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.MAKING), eq(1L), eq(2), eq("商家接单"));
    }

    @Test
    @DisplayName("测试finishMaking方法调用completeOrder")
    void testFinishMaking() {
        testOrder.setStatus(Constants.OrderStatus.MAKING);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.WAIT_PICKUP), anyLong(), anyInt(), anyString());

        orderService.finishMaking(1L, 1L);

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.WAIT_PICKUP), eq(1L), eq(2), eq("订单制作完成"));
    }

    @Test
    @DisplayName("测试confirmOrder方法调用pickupOrder")
    void testConfirmOrder() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_PICKUP);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.COMPLETED), anyLong(), anyInt(), anyString());

        orderService.confirmOrder(1L, 1L);

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.COMPLETED), eq(1L), eq(1), eq("用户取餐"));
    }

    @Test
    @DisplayName("测试confirmPickup方法调用pickupOrder")
    void testConfirmPickup() {
        testOrder.setStatus(Constants.OrderStatus.WAIT_PICKUP);
        when(orderMapper.selectOneById(1L)).thenReturn(testOrder);
        doNothing().when(orderStatusService).transition(any(Order.class), eq(Constants.OrderStatus.COMPLETED), anyLong(), anyInt(), anyString());

        orderService.confirmPickup(1L, 1L);

        verify(orderStatusService, times(1)).transition(any(Order.class), eq(Constants.OrderStatus.COMPLETED), eq(1L), eq(1), eq("用户取餐"));
    }
}
