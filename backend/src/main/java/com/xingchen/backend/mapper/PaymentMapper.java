package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Payment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 支付Mapper
 */
@Mapper
public interface PaymentMapper {

    /**
     * 插入支付记录（带版本号）
     */
    @Insert("INSERT INTO payment (order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, create_time, update_time, version, is_deleted) " +
            "VALUES (#{orderNo}, #{orderId}, #{paymentNo}, #{userId}, #{amount}, #{channel}, #{status}, #{tradeNo}, #{payTime}, #{createTime}, #{updateTime}, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    /**
     * 根据ID查询（带版本号）
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE id = #{id} AND is_deleted = 0")
    Payment selectById(@Param("id") Long id);

    /**
     * 根据ID查询（别名，带版本号）
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE id = #{id} AND is_deleted = 0")
    Payment selectOneById(@Param("id") Long id);

    /**
     * 根据订单ID查询（带版本号）
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE order_id = #{orderId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    Payment selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询 - 使用子查询避免JOIN的字段映射问题（带版本号）
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE order_id = (SELECT id FROM `order` WHERE order_no = #{orderNo}) AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    Payment selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据支付单号查询
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE payment_no = #{paymentNo} AND is_deleted = 0")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据交易号查询
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE trade_no = #{tradeNo} AND is_deleted = 0")
    Payment selectByTradeNo(@Param("tradeNo") String tradeNo);

    /**
     * 更新支付状态 - 只更新状态相关字段，避免更新不可变字段
     */
    @Update("UPDATE payment SET status = #{status}, pay_time = #{payTime}, trade_no = #{tradeNo}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, 
                     @Param("payTime") java.time.LocalDateTime payTime, @Param("tradeNo") String tradeNo);

    /**
     * 更新支付单号
     */
    @Update("UPDATE payment SET payment_no = #{paymentNo}, update_time = NOW() WHERE id = #{id}")
    int updatePaymentNo(@Param("id") Long id, @Param("paymentNo") String paymentNo);

    /**
     * 关闭支付（支付超时或失败）
     */
    @Update("UPDATE payment SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int closePayment(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询用户支付记录 - 使用子查询避免JOIN的字段映射问题
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE order_id IN (SELECT id FROM `order` WHERE user_id = #{userId}) AND is_deleted = 0 ORDER BY create_time DESC")
    List<Payment> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询超时的未支付支付记录
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE status = #{status} AND create_time < #{expireTime} AND is_deleted = 0")
    List<Payment> selectExpiredPayments(@Param("status") Integer status, @Param("expireTime") java.time.LocalDateTime expireTime);

    /**
     * 使用乐观锁更新支付状态
     */
    @Update("UPDATE payment SET status = #{status}, pay_time = #{payTime}, trade_no = #{tradeNo}, callback_time = #{callbackTime}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateStatusWithVersion(@Param("id") Long id, @Param("status") Integer status,
                                @Param("payTime") java.time.LocalDateTime payTime, @Param("tradeNo") String tradeNo,
                                @Param("callbackTime") java.time.LocalDateTime callbackTime, @Param("version") Integer version);

    /**
     * 查询需要主动查询的支付记录（支付中但超过2分钟的）
     * 用于主动向支付宝查询支付结果
     */
    @Select("SELECT id, order_no, order_id, payment_no, user_id, amount, channel, status, trade_no, pay_time, callback_time, version, is_deleted, create_time, update_time " +
            "FROM payment WHERE status = #{status} AND create_time > #{startTime} AND is_deleted = 0")
    List<Payment> selectPaymentsForQuery(@Param("status") Integer status, @Param("startTime") java.time.LocalDateTime startTime);
}
