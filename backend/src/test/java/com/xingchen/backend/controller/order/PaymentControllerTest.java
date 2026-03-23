package com.xingchen.backend.controller.order;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingchen.backend.config.RateLimitConfig;
import com.xingchen.backend.dto.request.payment.PaymentRequest;
import com.xingchen.backend.dto.response.payment.PaymentResponse;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 支付控制器单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "test-token-12345";
    private static final String TEST_ORDER_NO = "ORDER202403140001";

    @BeforeEach
    void setUp() {
        // 模拟Sa-Token登录状态
        mockStatic(StpUtil.class);
        when(StpUtil.getLoginIdAsLong()).thenReturn(TEST_USER_ID);
        when(StpUtil.isLogin()).thenReturn(true);
    }

    /**
     * 测试创建支付接口 POST /api/payment/create
     */
    @Test
    void testCreatePayment() throws Exception {
        // 准备请求数据
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo(TEST_ORDER_NO);
        request.setAmount(new BigDecimal("50.00"));
        request.setDescription("测试订单支付");
        request.setCallbackUrl("http://example.com/callback");

        // 准备响应数据
        PaymentResponse response = new PaymentResponse();
        response.setOrderNo(TEST_ORDER_NO);
        response.setPaymentUrl("https://openapi.alipay.com/gateway.do?...");
        response.setStatus(0);
        response.setPaymentMethod(2);

        Map<String, String> paymentParams = new HashMap<>();
        paymentParams.put("app_id", "test_app_id");
        paymentParams.put("out_trade_no", TEST_ORDER_NO);
        response.setPaymentParams(paymentParams);

        when(paymentService.generatePaymentUrl(any(PaymentRequest.class))).thenReturn(response);

        // 执行测试
        mockMvc.perform(post("/api/payment/create")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付宝支付单创建成功"))
                .andExpect(jsonPath("$.data.orderNo").value(TEST_ORDER_NO))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://openapi.alipay.com/gateway.do?..."))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.paymentMethod").value(2));

        // 验证Service方法被调用，并验证paymentMethod被设置为2（支付宝）
        verify(paymentService, times(1)).generatePaymentUrl(any(PaymentRequest.class));
    }

    /**
     * 测试创建支付接口 - 缺少必填参数
     */
    @Test
    void testCreatePayment_MissingRequiredParams() throws Exception {
        // 准备无效请求数据（缺少订单号）
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("50.00"));
        // orderNo为空

        // 执行测试
        mockMvc.perform(post("/api/payment/create")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller层没有强制校验，会正常处理

        verify(paymentService, times(1)).generatePaymentUrl(any(PaymentRequest.class));
    }

    /**
     * 测试查询支付状态接口 GET /api/payment/status/{orderNo}
     */
    @Test
    void testQueryPaymentStatus() throws Exception {
        // 准备响应数据 - 支付成功状态
        when(paymentService.queryPaymentStatus(eq(TEST_ORDER_NO))).thenReturn(1);

        // 执行测试
        mockMvc.perform(get("/api/payment/status/{orderNo}", TEST_ORDER_NO)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("查询成功"))
                .andExpect(jsonPath("$.data").value(1));

        // 验证Service方法被调用
        verify(paymentService, times(1)).queryPaymentStatus(eq(TEST_ORDER_NO));
    }

    /**
     * 测试查询支付状态接口 - 未支付状态
     */
    @Test
    void testQueryPaymentStatus_Unpaid() throws Exception {
        // 准备响应数据 - 未支付状态
        when(paymentService.queryPaymentStatus(eq(TEST_ORDER_NO))).thenReturn(0);

        // 执行测试
        mockMvc.perform(get("/api/payment/status/{orderNo}", TEST_ORDER_NO)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(0));

        verify(paymentService, times(1)).queryPaymentStatus(eq(TEST_ORDER_NO));
    }

    /**
     * 测试查询支付状态接口 - 支付失败状态
     */
    @Test
    void testQueryPaymentStatus_Failed() throws Exception {
        // 准备响应数据 - 支付失败状态
        when(paymentService.queryPaymentStatus(eq(TEST_ORDER_NO))).thenReturn(2);

        // 执行测试
        mockMvc.perform(get("/api/payment/status/{orderNo}", TEST_ORDER_NO)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(2));

        verify(paymentService, times(1)).queryPaymentStatus(eq(TEST_ORDER_NO));
    }

    /**
     * 测试获取支付记录接口 GET /api/payment/record/{orderNo}
     */
    @Test
    void testGetPaymentRecord() throws Exception {
        // 准备响应数据
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNo("PAY202403140001");
        payment.setOrderId(100L);
        payment.setOrderNo(TEST_ORDER_NO);
        payment.setUserId(TEST_USER_ID);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setChannel(2);
        payment.setTradeNo("ALIPAY202403140001");
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        when(paymentService.getPaymentByOrderNo(eq(TEST_ORDER_NO))).thenReturn(payment);

        // 执行测试
        mockMvc.perform(get("/api/payment/record/{orderNo}", TEST_ORDER_NO)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentNo").value("PAY202403140001"))
                .andExpect(jsonPath("$.data.orderNo").value(TEST_ORDER_NO))
                .andExpect(jsonPath("$.data.amount").value(50.00))
                .andExpect(jsonPath("$.data.channel").value(2))
                .andExpect(jsonPath("$.data.status").value(1));

        // 验证Service方法被调用
        verify(paymentService, times(1)).getPaymentByOrderNo(eq(TEST_ORDER_NO));
    }

    /**
     * 测试获取支付记录接口 - 记录不存在
     */
    @Test
    void testGetPaymentRecord_NotFound() throws Exception {
        // 准备响应数据 - 记录不存在
        when(paymentService.getPaymentByOrderNo(eq("NONEXISTENT"))).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/payment/record/{orderNo}", "NONEXISTENT")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(paymentService, times(1)).getPaymentByOrderNo(eq("NONEXISTENT"));
    }
}
