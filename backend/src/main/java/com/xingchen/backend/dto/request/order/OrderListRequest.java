package com.xingchen.backend.dto.request.order;

import lombok.Data;

@Data
public class OrderListRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 订单状?     */
    private Integer status;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
