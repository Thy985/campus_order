package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.mapper.OrderItemMapper;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.service.OrderQueryService;
import com.xingchen.backend.dto.response.order.OrderDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单查询服务实现类 */
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId) {
        // 查询订单基本信息
        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            return null;
        }

        OrderDetailDTO dto = new OrderDetailDTO();
        // 复制订单基本信息
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(order.getStatus());
        dto.setPayStatus(order.getPayStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setActualAmount(order.getActualAmount());
        dto.setRemark(order.getRemark());
        dto.setCreateTime(order.getCreateTime());
        dto.setPayTime(order.getPayTime());
        dto.setAcceptTime(order.getAcceptTime());
        dto.setFinishTime(order.getFinishTime());

        // 查询用户信息
        if (order.getUserId() != null) {
            User user = userMapper.selectOneById(order.getUserId());
            if (user != null) {
                dto.setUserId(user.getId());
                dto.setUserNickname(user.getNickname());
                dto.setUserPhone(user.getPhone());
                dto.setUserAvatar(user.getAvatar());
            }
        }

        // 查询商家信息
        if (order.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectOneById(order.getMerchantId());
            if (merchant != null) {
                dto.setMerchantId(merchant.getId());
                dto.setMerchantName(merchant.getName());
                dto.setMerchantLogo(merchant.getLogo());
                dto.setMerchantPhone(merchant.getPhone());
            }
        }

        // 查询地址信息 - 注意：Order 实体没有 addressId 字段，暂不处理
        // 查询订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        dto.setItems(items);

        return dto;
    }

    @Override
    public OrderDetailDTO getMerchantOrderDetail(Long orderId, Long merchantId) {
        OrderDetailDTO dto = getOrderDetail(orderId);
        if (dto == null) {
            return null;
        }

        // 验证订单是否属于该商家
        if (!dto.getMerchantId().equals(merchantId)) {
            return null;
        }

        return dto;
    }

    @Override
    public Long getMerchantIdByUserId(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            return null;
        }
        return user.getMerchantId();
    }
}
