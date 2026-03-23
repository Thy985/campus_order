package com.xingchen.backend.dto.response.order;

import com.xingchen.backend.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情DTO
 * 包含订单完整信息，包括用户信息、商家信息、订单项
 */
@Data
public class OrderDetailDTO {
    
    // 订单基本信息
    private Long id;
    private String orderNo;
    private Integer status;
    private Integer payStatus;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    
    // 用户信息
    private Long userId;
    private String userNickname;
    private String userPhone;
    private String userAvatar;
    
    // 商家信息
    private Long merchantId;
    private String merchantName;
    private String merchantLogo;
    private String merchantPhone;
    private String merchantAddress;
    
    // 地址信息
    private String contactName;
    private String contactPhone;
    private String addressDetail;
    
    // 订单项
    private List<OrderItem> items;

    /**
     * 设置订单项（兼容方法）
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * 获取订单项（兼容方法）
     */
    public List<OrderItem> getItems() {
        return this.items;
    }
}
