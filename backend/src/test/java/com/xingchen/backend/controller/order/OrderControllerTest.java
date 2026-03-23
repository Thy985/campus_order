package com.xingchen.backend.controller.order;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingchen.backend.config.RateLimitConfig;
import com.xingchen.backend.dto.request.order.CreateOrderRequest;
import com.xingchen.backend.dto.request.order.OrderListRequest;
import com.xingchen.backend.dto.response.order.OrderDetailResponse;
import com.xingchen.backend.dto.response.order.OrderListResponse;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.service.OrderService;
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
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 订单控制器单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ORDER_ID = 100L;
    private static final String TEST_TOKEN = "test-token-12345";

    @BeforeEach
    void setUp() {
        // 模拟Sa-Token登录状态
        mockStatic(StpUtil.class);
        when(StpUtil.getLoginIdAsLong()).thenReturn(TEST_USER_ID);
        when(StpUtil.isLogin()).thenReturn(true);
    }

    /**
     * 测试创建订单接口 POST /api/order
     */
    @Test
    void testCreateOrder() throws Exception {
        // 准备请求数据
        CreateOrderRequest request = new CreateOrderRequest();
        request.setMerchantId(1L);
        request.setDeliveryAddress("测试地址");
        request.setContactPhone("13800138000");
        request.setContactName("测试用户");
        request.setRemark("测试备注");

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setProductName("测试商品");
        item.setProductPrice(new BigDecimal("25.00"));
        item.setQuantity(2);
        item.setProductImage("http://example.com/image.jpg");
        request.setOrderItems(Collections.singletonList(item));

        // 准备响应数据
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORDER202403140001");
        order.setUserId(TEST_USER_ID);
        order.setMerchantId(1L);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setActualAmount(new BigDecimal("50.00"));
        order.setStatus(0);
        order.setPayStatus(0);
        order.setCreateTime(LocalDateTime.now());

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        // 执行测试
        mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("订单创建成功"))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER202403140001"))
                .andExpect(jsonPath("$.data.totalAmount").value(50.00))
                .andExpect(jsonPath("$.data.status").value(0));

        // 验证Service方法被调用
        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    /**
     * 测试创建订单接口 - 参数校验失败
     */
    @Test
    void testCreateOrder_ValidationFailed() throws Exception {
        // 准备无效请求数据（缺少必填字段）
        CreateOrderRequest request = new CreateOrderRequest();
        request.setMerchantId(null); // 商家ID为空
        request.setDeliveryAddress(""); // 地址为空
        request.setContactPhone("invalid"); // 手机号格式错误

        // 执行测试
        mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证Service方法未被调用
        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    /**
     * 测试查询订单列表接口 GET /api/order/list
     */
    @Test
    void testGetOrderList() throws Exception {
        // 准备响应数据
        OrderListResponse response = new OrderListResponse();
        response.setTotal(10L);
        response.setPage(1);
        response.setPageSize(10);
        response.setTotalPages(1);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORDER202403140001");
        order.setUserId(TEST_USER_ID);
        order.setMerchantId(1L);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus(1);
        order.setPayStatus(1);
        order.setCreateTime(LocalDateTime.now());

        response.setOrderList(Collections.singletonList(order));

        when(orderService.getOrderList(any(OrderListRequest.class))).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/order/list")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.orderList[0].orderNo").value("ORDER202403140001"));

        // 验证Service方法被调用
        verify(orderService, times(1)).getOrderList(any(OrderListRequest.class));
    }

    /**
     * 测试查询订单列表接口 - 空列表
     */
    @Test
    void testGetOrderList_Empty() throws Exception {
        // 准备响应数据
        OrderListResponse response = new OrderListResponse();
        response.setTotal(0L);
        response.setPage(1);
        response.setPageSize(10);
        response.setTotalPages(0);
        response.setOrderList(new ArrayList<>());

        when(orderService.getOrderList(any(OrderListRequest.class))).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/order/list")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.orderList").isArray())
                .andExpect(jsonPath("$.data.orderList").isEmpty());

        verify(orderService, times(1)).getOrderList(any(OrderListRequest.class));
    }

    /**
     * 测试查询订单详情接口 GET /api/order/{orderId}
     */
    @Test
    void testGetOrderDetail() throws Exception {
        // 准备响应数据
        OrderDetailResponse response = new OrderDetailResponse();

        Order order = new Order();
        order.setId(TEST_ORDER_ID);
        order.setOrderNo("ORDER202403140001");
        order.setUserId(TEST_USER_ID);
        order.setMerchantId(1L);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setActualAmount(new BigDecimal("50.00"));
        order.setStatus(1);
        order.setPayStatus(1);
        order.setCreateTime(LocalDateTime.now());

        response.setOrder(order);
        response.setOrderItems(new ArrayList<>());

        when(orderService.getOrderDetail(eq(TEST_ORDER_ID), eq(TEST_USER_ID))).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/order/{orderId}", TEST_ORDER_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.order.id").value(TEST_ORDER_ID))
                .andExpect(jsonPath("$.data.order.orderNo").value("ORDER202403140001"))
                .andExpect(jsonPath("$.data.order.totalAmount").value(50.00));

        // 验证Service方法被调用
        verify(orderService, times(1)).getOrderDetail(eq(TEST_ORDER_ID), eq(TEST_USER_ID));
    }

    /**
     * 测试取消订单接口 POST /api/order/{orderId}/cancel
     */
    @Test
    void testCancelOrder() throws Exception {
        // 执行测试
        mockMvc.perform(post("/api/order/{orderId}/cancel", TEST_ORDER_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("订单取消成功"));

        // 验证Service方法被调用
        verify(orderService, times(1)).cancelOrder(eq(TEST_ORDER_ID), eq(TEST_USER_ID));
    }

    /**
     * 测试确认取餐接口 POST /api/order/{orderId}/confirm-pickup
     */
    @Test
    void testConfirmPickup() throws Exception {
        // 执行测试
        mockMvc.perform(post("/api/order/{orderId}/confirm-pickup", TEST_ORDER_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("确认取餐成功"));

        // 验证Service方法被调用
        verify(orderService, times(1)).confirmPickup(eq(TEST_ORDER_ID), eq(TEST_USER_ID));
    }
}
