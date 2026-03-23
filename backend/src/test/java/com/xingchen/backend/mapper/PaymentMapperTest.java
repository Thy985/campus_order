package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.util.constant.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentMapper集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setPaymentNo("PAY202403140001");
        testPayment.setOrderId(1L);
        testPayment.setOrderNo("ORD202403140001");
        testPayment.setUserId(1L);
        testPayment.setAmount(new BigDecimal("50.00"));
        testPayment.setChannel(Constants.PaymentChannel.ALIPAY);
        testPayment.setTradeNo("TRADE202403140001");
        testPayment.setStatus(Constants.PaymentStatus.UNPAID);
        testPayment.setCreateTime(LocalDateTime.now());
        testPayment.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试支付记录插入操作")
    void testInsertPayment() {
        int result = paymentMapper.insert(testPayment);

        assertTrue(result > 0, "支付记录插入应该成功");
        assertNotNull(testPayment.getId(), "插入后支付记录ID不应为空");

        Payment savedPayment = paymentMapper.selectOneById(testPayment.getId());
        assertNotNull(savedPayment, "查询到的支付记录不应为空");
        assertEquals(testPayment.getPaymentNo(), savedPayment.getPaymentNo());
        assertEquals(testPayment.getOrderId(), savedPayment.getOrderId());
        assertEquals(testPayment.getOrderNo(), savedPayment.getOrderNo());
        assertEquals(0, testPayment.getAmount().compareTo(savedPayment.getAmount()));
        assertEquals(testPayment.getChannel(), savedPayment.getChannel());
    }

    @Test
    @DisplayName("测试支付记录更新操作")
    void testUpdatePayment() {
        paymentMapper.insert(testPayment);

        testPayment.setStatus(Constants.PaymentStatus.PAID);
        testPayment.setPayTime(LocalDateTime.now());
        testPayment.setCallbackTime(LocalDateTime.now());
        testPayment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateStatusWithVersion(
                testPayment.getId(),
                Constants.PaymentStatus.PAID,
                testPayment.getPayTime(),
                testPayment.getTradeNo(),
                testPayment.getCallbackTime(),
                0
        );

        assertTrue(result > 0, "支付记录更新应该成功");

        Payment updatedPayment = paymentMapper.selectOneById(testPayment.getId());
        assertEquals(Constants.PaymentStatus.PAID, updatedPayment.getStatus());
        assertNotNull(updatedPayment.getPayTime());
        assertNotNull(updatedPayment.getCallbackTime());
    }

    @Test
    @DisplayName("测试根据ID查询支付记录")
    void testSelectById() {
        paymentMapper.insert(testPayment);
        Long paymentId = testPayment.getId();

        Payment foundPayment = paymentMapper.selectOneById(paymentId);

        assertNotNull(foundPayment, "根据ID应能查询到支付记录");
        assertEquals(paymentId, foundPayment.getId());
        assertEquals(testPayment.getPaymentNo(), foundPayment.getPaymentNo());
    }

    @Test
    @DisplayName("测试根据支付单号查询支付记录")
    void testSelectByPaymentNo() {
        paymentMapper.insert(testPayment);

        Payment foundPayment = paymentMapper.selectByPaymentNo("PAY202403140001");

        assertNotNull(foundPayment, "根据支付单号应能查询到支付记录");
        assertEquals(testPayment.getPaymentNo(), foundPayment.getPaymentNo());
        assertEquals(testPayment.getOrderId(), foundPayment.getOrderId());
    }

    @Test
    @DisplayName("测试根据支付单号查询支付记录-不存在")
    void testSelectByPaymentNo_NotExist() {
        Payment foundPayment = paymentMapper.selectByPaymentNo("NONEXISTENT");

        assertNull(foundPayment, "不存在的支付单号应返回null");
    }

    @Test
    @DisplayName("测试根据订单ID查询支付记录")
    void testSelectByOrderId() {
        paymentMapper.insert(testPayment);

        Payment foundPayment = paymentMapper.selectByOrderId(1L);

        assertNotNull(foundPayment, "根据订单ID应能查询到支付记录");
        assertEquals(testPayment.getOrderId(), foundPayment.getOrderId());
    }

    @Test
    @DisplayName("测试根据订单号查询支付记录")
    void testSelectByOrderNo() {
        paymentMapper.insert(testPayment);

        Payment foundPayment = paymentMapper.selectByOrderNo("ORD202403140001");

        assertNotNull(foundPayment, "根据订单号应能查询到支付记录");
        assertEquals(testPayment.getOrderNo(), foundPayment.getOrderNo());
    }

    @Test
    @DisplayName("测试根据订单号查询支付记录-不存在")
    void testSelectByOrderNo_NotExist() {
        Payment foundPayment = paymentMapper.selectByOrderNo("NONEXISTENT");

        assertNull(foundPayment, "不存在的订单号应返回null");
    }

    @Test
    @DisplayName("测试根据第三方交易号查询支付记录")
    void testSelectByTradeNo() {
        paymentMapper.insert(testPayment);

        Payment foundPayment = paymentMapper.selectByTradeNo("TRADE202403140001");

        assertNotNull(foundPayment, "根据第三方交易号应能查询到支付记录");
        assertEquals(testPayment.getTradeNo(), foundPayment.getTradeNo());
    }

    @Test
    @DisplayName("测试根据第三方交易号查询支付记录-不存在")
    void testSelectByTradeNo_NotExist() {
        Payment foundPayment = paymentMapper.selectByTradeNo("NONEXISTENT");

        assertNull(foundPayment, "不存在的交易号应返回null");
    }

    @Test
    @DisplayName("测试根据用户ID查询支付记录列表")
    void testSelectByUserId() {
        paymentMapper.insert(testPayment);

        Payment payment2 = new Payment();
        payment2.setPaymentNo("PAY202403140002");
        payment2.setOrderId(2L);
        payment2.setOrderNo("ORD202403140002");
        payment2.setUserId(1L);
        payment2.setAmount(new BigDecimal("30.00"));
        payment2.setChannel(Constants.PaymentChannel.WECHAT);
        payment2.setTradeNo("TRADE202403140002");
        payment2.setStatus(Constants.PaymentStatus.PAID);
        payment2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        payment2.setCreateTime(LocalDateTime.now());
        payment2.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment2);

        List<Payment> payments = paymentMapper.selectByUserId(1L);

        assertNotNull(payments);
        assertTrue(payments.size() >= 2, "应至少返回2条支付记录");
        assertTrue(payments.stream().allMatch(p -> p.getUserId().equals(1L)),
                "所有支付记录应属于同一用户");
    }

    @Test
    @DisplayName("测试支付记录分页查询")
    void testPagination() {
        for (int i = 0; i < 5; i++) {
            Payment payment = new Payment();
            payment.setPaymentNo("PAY20240314010" + i);
            payment.setOrderId((long) (i + 1));
            payment.setOrderNo("ORD20240314010" + i);
            payment.setUserId(1L);
            payment.setAmount(new BigDecimal("20.00"));
            payment.setChannel(Constants.PaymentChannel.ALIPAY);
        payment.setStatus(Constants.PaymentStatus.UNPAID);
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.insert(payment);
        }

        List<Payment> allPayments = paymentMapper.selectByUserId(1L);
        // 手动分页
        List<Payment> page1 = allPayments.stream().limit(2).toList();
        List<Payment> page2 = allPayments.stream().skip(2).limit(2).toList();

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(2, page1.size(), "第一页应返回2条记录");
        assertEquals(2, page2.size(), "第二页应返回2条记录");
    }

    @Test
    @DisplayName("测试更新支付状态")
    void testUpdatePaymentStatus() {
        paymentMapper.insert(testPayment);

        paymentMapper.updateStatusWithVersion(
                testPayment.getId(),
                Constants.PaymentStatus.PAID,
                LocalDateTime.now(),
                testPayment.getTradeNo(),
                LocalDateTime.now(),
                0
        );

        Payment updatedPayment = paymentMapper.selectOneById(testPayment.getId());
        assertEquals(Constants.PaymentStatus.PAID, updatedPayment.getStatus());
    }

    @Test
    @DisplayName("测试更新支付时间和回调时间")
    void testUpdatePayAndCallbackTime() {
        paymentMapper.insert(testPayment);

        LocalDateTime payTime = LocalDateTime.now();
        LocalDateTime callbackTime = LocalDateTime.now();

        paymentMapper.updateStatusWithVersion(
                testPayment.getId(),
                testPayment.getStatus(),
                payTime,
                testPayment.getTradeNo(),
                callbackTime,
                0
        );

        Payment updatedPayment = paymentMapper.selectOneById(testPayment.getId());
        assertNotNull(updatedPayment.getPayTime());
        assertNotNull(updatedPayment.getCallbackTime());
    }

    @Test
    @DisplayName("测试不同支付渠道的支付记录")
    void testDifferentPaymentChannels() {
        Payment alipayPayment = new Payment();
        alipayPayment.setPaymentNo("PAY_ALIPAY_001");
        alipayPayment.setOrderId(1L);
        alipayPayment.setOrderNo("ORD001");
        alipayPayment.setUserId(1L);
        alipayPayment.setAmount(new BigDecimal("50.00"));
        alipayPayment.setChannel(Constants.PaymentChannel.ALIPAY);
        alipayPayment.setStatus(Constants.PaymentStatus.PAID);
        alipayPayment.setCreateTime(LocalDateTime.now());
        alipayPayment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(alipayPayment);

        Payment wechatPayment = new Payment();
        wechatPayment.setPaymentNo("PAY_WECHAT_001");
        wechatPayment.setOrderId(2L);
        wechatPayment.setOrderNo("ORD002");
        wechatPayment.setUserId(1L);
        wechatPayment.setAmount(new BigDecimal("30.00"));
        wechatPayment.setChannel(Constants.PaymentChannel.WECHAT);
        wechatPayment.setStatus(Constants.PaymentStatus.PAID);
        wechatPayment.setCreateTime(LocalDateTime.now());
        wechatPayment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(wechatPayment);

        List<Payment> payments = paymentMapper.selectByUserId(1L);

        assertNotNull(payments);
        assertTrue(payments.size() >= 2, "应至少返回2条支付记录");
        assertTrue(payments.stream().anyMatch(p -> p.getChannel().equals(Constants.PaymentChannel.ALIPAY)),
                "应包含支付宝支付记录");
        assertTrue(payments.stream().anyMatch(p -> p.getChannel().equals(Constants.PaymentChannel.WECHAT)),
                "应包含微信支付记录");
    }

    @Test
    @DisplayName("测试不同支付状态的支付记录")
    void testDifferentPaymentStatuses() {
        Payment waitPayPayment = new Payment();
        waitPayPayment.setPaymentNo("PAY_WAIT_001");
        waitPayPayment.setOrderId(1L);
        waitPayPayment.setOrderNo("ORD001");
        waitPayPayment.setUserId(1L);
        waitPayPayment.setAmount(new BigDecimal("50.00"));
        waitPayPayment.setChannel(Constants.PaymentChannel.ALIPAY);
        waitPayPayment.setStatus(Constants.PaymentStatus.UNPAID);
        waitPayPayment.setCreateTime(LocalDateTime.now());
        waitPayPayment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(waitPayPayment);

        Payment paidPayment = new Payment();
        paidPayment.setPaymentNo("PAY_PAID_001");
        paidPayment.setOrderId(2L);
        paidPayment.setOrderNo("ORD002");
        paidPayment.setUserId(1L);
        paidPayment.setAmount(new BigDecimal("30.00"));
        paidPayment.setChannel(Constants.PaymentChannel.ALIPAY);
        paidPayment.setStatus(Constants.PaymentStatus.PAID);
        paidPayment.setPayTime(LocalDateTime.now());
        paidPayment.setCreateTime(LocalDateTime.now());
        paidPayment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(paidPayment);

        Payment closedPayment = new Payment();
        closedPayment.setPaymentNo("PAY_CLOSED_001");
        closedPayment.setOrderId(3L);
        closedPayment.setOrderNo("ORD003");
        closedPayment.setUserId(1L);
        closedPayment.setAmount(new BigDecimal("20.00"));
        closedPayment.setChannel(Constants.PaymentChannel.ALIPAY);
        closedPayment.setStatus(Constants.PaymentStatus.CLOSED);
        closedPayment.setCreateTime(LocalDateTime.now());
        closedPayment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(closedPayment);

        List<Payment> payments = paymentMapper.selectByUserId(1L);

        assertNotNull(payments);
        assertTrue(payments.size() >= 3, "应至少返回3条支付记录");
        assertTrue(payments.stream().anyMatch(p -> p.getStatus().equals(Constants.PaymentStatus.UNPAID)),
                "应包含待支付记录");
        assertTrue(payments.stream().anyMatch(p -> p.getStatus().equals(Constants.PaymentStatus.PAID)),
                "应包含已支付记录");
        assertTrue(payments.stream().anyMatch(p -> p.getStatus().equals(Constants.PaymentStatus.CLOSED)),
                "应包含已关闭记录");
    }

    @Test
    @DisplayName("测试支付记录存在性")
    void testPaymentExists() {
        paymentMapper.insert(testPayment);

        Payment foundPayment = paymentMapper.selectOneById(testPayment.getId());
        assertNotNull(foundPayment);
        assertEquals(testPayment.getPaymentNo(), foundPayment.getPaymentNo());
    }

    @Test
    @DisplayName("测试支付金额精度")
    void testPaymentAmountPrecision() {
        Payment payment = new Payment();
        payment.setPaymentNo("PAY_PRECISION_001");
        payment.setOrderId(1L);
        payment.setOrderNo("ORD001");
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("99.99"));
        payment.setChannel(Constants.PaymentChannel.ALIPAY);
        payment.setStatus(Constants.PaymentStatus.PAID);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        Payment foundPayment = paymentMapper.selectByPaymentNo("PAY_PRECISION_001");

        assertNotNull(foundPayment);
        assertEquals(0, new BigDecimal("99.99").compareTo(foundPayment.getAmount()));
    }
}
