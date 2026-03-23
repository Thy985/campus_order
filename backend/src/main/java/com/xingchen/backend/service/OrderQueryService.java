package com.xingchen.backend.service;

import com.xingchen.backend.dto.response.order.OrderDetailDTO;

/**
 * 订单查询服务接口
 * 提供订单详情查询功能
 */
public interface OrderQueryService {

    /**
     * 获取订单详情
     */
    OrderDetailDTO getOrderDetail(Long orderId);

    /**
     * 获取商家订单详情（商家视角）
     */
    OrderDetailDTO getMerchantOrderDetail(Long orderId, Long merchantId);

    /**
     * 根据用户ID获取关联的商家ID
     */
    Long getMerchantIdByUserId(Long userId);
}
