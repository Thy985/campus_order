package com.xingchen.backend.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    
    /**
     * 用户ID（后端自动填充，不需要前端传?     */
    private Long userId;
    
    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;
    
    /**
     * 配送地址
     */
    @NotBlank(message = "配送地址不能为空")
    @Size(max = 200, message = "配送地址不能超过200字符")
    private String deliveryAddress;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
    
    /**
     * 联系?     */
    @Size(max = 50, message = "联系人不能超?0字符")
    private String contactName;
    
    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
    
    /**
     * 订单明细列表
     */
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemRequest> orderItems;
    
    /**
     * 订单明细项DTO
     */
    @Data
    public static class OrderItemRequest {
        
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        /**
         * 商品名称
         */
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 100, message = "商品名称不能超过100字符")
        private String productName;
        
        /**
         * 商品价格
         */
        @NotNull(message = "商品价格不能为空")
        private BigDecimal productPrice;
        
        /**
         * 商品数量
         */
        @NotNull(message = "商品数量不能为空")
        private Integer quantity;
        
        /**
         * 商品图片
         */
        private String productImage;
    }
}
