package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xingchen.backend.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 订单项Mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单项
     */
    @Select("SELECT id, order_id, product_id, product_name, price, product_image, quantity, total_amount, create_time FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单ID删除订单项
     */
    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 统计商家所有菜品的销量
     */
    @Select("SELECT SUM(quantity) as totalSales FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status IN (2, 3, 4)")
    java.util.Map<String, Object> countSalesByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 查询商家热销菜品
     */
    @Select("SELECT oi.product_name as name, SUM(oi.quantity) as salesCount, oi.price as price " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status IN (2, 3, 4) " +
            "GROUP BY oi.product_id, oi.product_name, oi.price " +
            "ORDER BY salesCount DESC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> selectTopDishesByMerchantId(@Param("merchantId") Long merchantId, @Param("limit") int limit);
}
