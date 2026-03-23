package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.util.constant.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据用户ID查询订单列表
     */
    default List<Order> selectByUserId(Long userId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("user_id = ? AND is_deleted = 0", userId)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据用户ID和状态查询订单列?     */
    default List<Order> selectByUserIdAndStatus(Long userId, Integer status, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("user_id = ? AND status = ? AND is_deleted = 0", userId, status)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据商家ID查询订单列表
     */
    default List<Order> selectByMerchantId(Long merchantId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据商家ID和状态查询订单列?     */
    default List<Order> selectByMerchantIdAndStatus(Long merchantId, Integer status, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND status = ? AND is_deleted = 0", merchantId, status)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据订单号查询订?     */
    default Order selectByOrderNo(String orderNo) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("order_no = ? AND is_deleted = 0", orderNo);
        return selectOneByQuery(queryWrapper);
    }
    
    /**
     * 查询超时未支付的订单
     */
    default List<Order> selectTimeoutOrders(int timeoutMinutes) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("status = " + Constants.OrderStatus.WAIT_PAY + " AND is_deleted = 0")
                .and("TIMESTAMPDIFF(MINUTE, create_time, NOW()) > ?", timeoutMinutes);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 统计用户订单数量
     */
    default long countByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("user_id = ? AND is_deleted = 0", userId);
        return selectCountByQuery(queryWrapper);
    }
    
    /**
     * 统计用户指定状态的订单数量
     */
    default long countByUserIdAndStatus(Long userId, int status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("user_id = ? AND status = ? AND is_deleted = 0", userId, status);
        return selectCountByQuery(queryWrapper);
    }
    
    /**
     * 统计商家订单数量
     */
    default long countByMerchantId(Long merchantId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId);
        return selectCountByQuery(queryWrapper);
    }
    
    /**
     * 统计商家指定状态的订单数量
     */
    default long countByMerchantIdAndStatus(Long merchantId, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND status = ? AND is_deleted = 0", merchantId, status);
        return selectCountByQuery(queryWrapper);
    }
    
    /**
     * 统计商家总销售额
     */
    default BigDecimal sumAmountByMerchantId(Long merchantId) {
        List<Order> orders = selectListByQuery(
            QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0 AND status NOT IN (6, 7)", merchantId)
        );
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }

    /**
     * 统计商家今日订单?     */
    default long countTodayOrdersByMerchantId(Long merchantId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .and("DATE(create_time) = CURDATE()");
        return selectCountByQuery(queryWrapper);
    }

    /**
     * 统计商家今日销售额
     */
    default BigDecimal sumTodayAmountByMerchantId(Long merchantId) {
        List<Order> orders = selectListByQuery(
            QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0 AND status NOT IN (6, 7)", merchantId)
                .and("DATE(create_time) = CURDATE()")
        );
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }

    /**
     * 统计商家待处理订单数
     */
    default long countPendingOrdersByMerchantId(Long merchantId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .and("status IN (2, 3)"); // 待接?2) + 制作?3)
        return selectCountByQuery(queryWrapper);
    }

    /**
     * 查询商家本周销售数?     */
    @Select("""
        SELECT 
            CASE DAYOFWEEK(create_time)
                WHEN 1 THEN '周日'
                WHEN 2 THEN '周一'
                WHEN 3 THEN '周二'
                WHEN 4 THEN '周三'
                WHEN 5 THEN '周四'
                WHEN 6 THEN '周五'
                WHEN 7 THEN '周六'
            END as day,
            COALESCE(SUM(total_amount), 0) as salesAmount
        FROM `order`
        WHERE merchant_id = #{merchantId}
            AND is_deleted = 0
            AND status NOT IN (6, 7)
            AND YEARWEEK(create_time, 1) = YEARWEEK(NOW(), 1)
        GROUP BY DAYOFWEEK(create_time)
        ORDER BY DAYOFWEEK(create_time)
        """)
    List<Map<String, Object>> selectWeeklySalesByMerchantId(Long merchantId);
    
    /**
     * 统计商家指定日期的销售额
     */
    default BigDecimal sumAmountByMerchantIdAndDate(Long merchantId, String date) {
        List<Order> orders = selectListByQuery(
            QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0 AND status NOT IN (6, 7)", merchantId)
                .and("DATE(create_time) = ?", date)
        );
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getActualAmount() != null) {
                total = total.add(order.getActualAmount());
            }
        }
        return total;
    }
    
    /**
     * 统计商家指定日期的订单数
     */
    default long countByMerchantIdAndDate(Long merchantId, String date) {
        return selectCountByQuery(
            QueryWrapper.create()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .and("DATE(create_time) = ?", date)
        );
    }
    
    /**
     * 统计商家指定日期范围的销售额
     */
    default BigDecimal sumAmountByMerchantIdAndDateRange(Long merchantId, String startDate, String endDate) {
        List<Order> orders = selectListByQuery(
            QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0 AND status NOT IN (6, 7)", merchantId)
                .and("DATE(create_time) BETWEEN ? AND ?", startDate, endDate)
        );
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getActualAmount() != null) {
                total = total.add(order.getActualAmount());
            }
        }
        return total;
    }
    
    /**
     * 统计商家指定日期范围的订单数
     */
    default long countByMerchantIdAndDateRange(Long merchantId, String startDate, String endDate) {
        return selectCountByQuery(
            QueryWrapper.create()
                .from(Order.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .and("DATE(create_time) BETWEEN ? AND ?", startDate, endDate)
        );
    }

    /**
     * 查询商家营收趋势（按天）
     */
    @Select("""
        SELECT 
            DATE(create_time) as date,
            COALESCE(SUM(actual_amount), 0) as revenue,
            COUNT(*) as orderCount
        FROM `order`
        WHERE merchant_id = #{merchantId}
            AND is_deleted = 0
            AND status NOT IN (6, 7)
            AND create_time >= #{startDate}
            AND create_time < #{endDate}
        GROUP BY DATE(create_time)
        ORDER BY DATE(create_time)
        """)
    List<Map<String, Object>> selectRevenueTrendByDay(Long merchantId, String startDate, String endDate);

    /**
     * 查询商家营收趋势（按周）
     */
    @Select("""
        SELECT 
            CONCAT(YEAR(create_time), '-', WEEK(create_time, 1)) as week,
            COALESCE(SUM(actual_amount), 0) as revenue,
            COUNT(*) as orderCount
        FROM `order`
        WHERE merchant_id = #{merchantId}
            AND is_deleted = 0
            AND status NOT IN (6, 7)
            AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{weeks} WEEK)
        GROUP BY YEAR(create_time), WEEK(create_time, 1)
        ORDER BY YEAR(create_time), WEEK(create_time, 1)
        """)
    List<Map<String, Object>> selectRevenueTrendByWeek(Long merchantId, int weeks);

    /**
     * 查询商家营收趋势（按月）
     */
    @Select("""
        SELECT 
            DATE_FORMAT(create_time, '%Y-%m') as month,
            COALESCE(SUM(actual_amount), 0) as revenue,
            COUNT(*) as orderCount
        FROM `order`
        WHERE merchant_id = #{merchantId}
            AND is_deleted = 0
            AND status NOT IN (6, 7)
            AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH)
        GROUP BY DATE_FORMAT(create_time, '%Y-%m')
        ORDER BY DATE_FORMAT(create_time, '%Y-%m')
        """)
    List<Map<String, Object>> selectRevenueTrendByMonth(Long merchantId, int months);

    /**
     * 查询用户订单列表
     */
    default List<Order> selectUserOrders(Long userId, Integer status, int page, int pageSize) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("user_id = ? AND is_deleted = 0", userId);
        
        if (status != null) {
            queryWrapper.and("status = ?", status);
        }
        
        queryWrapper.orderBy("create_time DESC")
                .limit((page - 1) * pageSize, pageSize);
        return selectListByQuery(queryWrapper);
    }

    /**
     * 统计用户订单数量
     */
    default Long countUserOrders(Long userId, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Order.class)
                .where("user_id = ? AND is_deleted = 0", userId);
        
        if (status != null) {
            queryWrapper.and("status = ?", status);
        }
        
        return selectCountByQuery(queryWrapper);
    }
}
