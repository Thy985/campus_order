package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Order;
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
 * OrderMapper集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderNo("ORD202403140001");
        testOrder.setUserId(1L);
        testOrder.setMerchantId(1L);
        testOrder.setOrderType(1);
        testOrder.setTotalAmount(new BigDecimal("50.00"));
        testOrder.setActualAmount(new BigDecimal("45.00"));
        testOrder.setRemark("测试订单备注");
        testOrder.setStatus(Constants.OrderStatus.WAIT_PAY);
        testOrder.setPayStatus(Constants.PayStatus.UNPAID);
        testOrder.setPayMethod(Constants.PaymentMethod.ALIPAY);
        testOrder.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        testOrder.setIsArchived(0);
        testOrder.setCreateTime(LocalDateTime.now());
        testOrder.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试订单插入操作")
    void testInsertOrder() {
        int result = orderMapper.insert(testOrder);

        assertTrue(result > 0, "订单插入应该成功");
        assertNotNull(testOrder.getId(), "插入后订单ID不应为空");

        Order savedOrder = orderMapper.selectOneById(testOrder.getId());
        assertNotNull(savedOrder, "查询到的订单不应为空");
        assertEquals(testOrder.getOrderNo(), savedOrder.getOrderNo());
        assertEquals(testOrder.getUserId(), savedOrder.getUserId());
        assertEquals(testOrder.getMerchantId(), savedOrder.getMerchantId());
        assertEquals(0, testOrder.getTotalAmount().compareTo(savedOrder.getTotalAmount()));
        assertEquals(0, testOrder.getActualAmount().compareTo(savedOrder.getActualAmount()));
    }

    @Test
    @DisplayName("测试订单更新操作")
    void testUpdateOrder() {
        orderMapper.insert(testOrder);

        testOrder.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        testOrder.setPayStatus(Constants.PayStatus.PAID);
        testOrder.setPayTime(LocalDateTime.now());
        testOrder.setRemark("更新后的备注");
        testOrder.setUpdateTime(LocalDateTime.now());

        int result = orderMapper.update(testOrder);

        assertTrue(result > 0, "订单更新应该成功");

        Order updatedOrder = orderMapper.selectOneById(testOrder.getId());
        assertEquals(Constants.OrderStatus.WAIT_ACCEPT, updatedOrder.getStatus());
        assertEquals(Constants.PayStatus.PAID, updatedOrder.getPayStatus());
        assertNotNull(updatedOrder.getPayTime());
        assertEquals("更新后的备注", updatedOrder.getRemark());
    }

    @Test
    @DisplayName("测试根据ID查询订单")
    void testSelectById() {
        orderMapper.insert(testOrder);
        Long orderId = testOrder.getId();

        Order foundOrder = orderMapper.selectOneById(orderId);

        assertNotNull(foundOrder, "根据ID应能查询到订单");
        assertEquals(orderId, foundOrder.getId());
        assertEquals(testOrder.getOrderNo(), foundOrder.getOrderNo());
    }

    @Test
    @DisplayName("测试根据订单号查询订单")
    void testSelectByOrderNo() {
        orderMapper.insert(testOrder);

        Order foundOrder = orderMapper.selectByOrderNo("ORD202403140001");

        assertNotNull(foundOrder, "根据订单号应能查询到订单");
        assertEquals(testOrder.getOrderNo(), foundOrder.getOrderNo());
        assertEquals(testOrder.getUserId(), foundOrder.getUserId());
    }

    @Test
    @DisplayName("测试根据用户ID查询订单列表")
    void testSelectByUserId() {
        orderMapper.insert(testOrder);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140002");
        order2.setUserId(1L);
        order2.setMerchantId(2L);
        order2.setTotalAmount(new BigDecimal("30.00"));
        order2.setActualAmount(new BigDecimal("30.00"));
        order2.setStatus(Constants.OrderStatus.COMPLETED);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        List<Order> orders = orderMapper.selectByUserId(1L, 10, 0);

        assertNotNull(orders);
        assertTrue(orders.size() >= 2, "应至少返回2条订单记录");
        assertTrue(orders.stream().allMatch(o -> o.getUserId().equals(1L)),
                "所有订单应属于同一用户");
    }

    @Test
    @DisplayName("测试根据用户ID和状态查询订单列表")
    void testSelectByUserIdAndStatus() {
        orderMapper.insert(testOrder);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140003");
        order2.setUserId(1L);
        order2.setMerchantId(2L);
        order2.setTotalAmount(new BigDecimal("30.00"));
        order2.setActualAmount(new BigDecimal("30.00"));
        order2.setStatus(Constants.OrderStatus.COMPLETED);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        List<Order> waitPayOrders = orderMapper.selectByUserIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY, 10, 0);

        assertNotNull(waitPayOrders);
        assertTrue(waitPayOrders.stream().allMatch(o ->
                o.getUserId().equals(1L) && o.getStatus().equals(Constants.OrderStatus.WAIT_PAY)),
                "所有订单应属于同一用户且状态为待支付");
    }

    @Test
    @DisplayName("测试根据商家ID查询订单列表")
    void testSelectByMerchantId() {
        orderMapper.insert(testOrder);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140004");
        order2.setUserId(2L);
        order2.setMerchantId(1L);
        order2.setTotalAmount(new BigDecimal("25.00"));
        order2.setActualAmount(new BigDecimal("25.00"));
        order2.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        List<Order> orders = orderMapper.selectByMerchantId(1L, 10, 0);

        assertNotNull(orders);
        assertTrue(orders.size() >= 2, "应至少返回2条订单记录");
        assertTrue(orders.stream().allMatch(o -> o.getMerchantId().equals(1L)),
                "所有订单应属于同一商家");
    }

    @Test
    @DisplayName("测试根据商家ID和状态查询订单列表")
    void testSelectByMerchantIdAndStatus() {
        orderMapper.insert(testOrder);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140005");
        order2.setUserId(2L);
        order2.setMerchantId(1L);
        order2.setTotalAmount(new BigDecimal("25.00"));
        order2.setActualAmount(new BigDecimal("25.00"));
        order2.setStatus(Constants.OrderStatus.COMPLETED);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        List<Order> waitPayOrders = orderMapper.selectByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY, 10, 0);

        assertNotNull(waitPayOrders);
        assertTrue(waitPayOrders.stream().allMatch(o ->
                o.getMerchantId().equals(1L) && o.getStatus().equals(Constants.OrderStatus.WAIT_PAY)),
                "所有订单应属于同一商家且状态为待支付");
    }

    @Test
    @DisplayName("测试分页查询")
    void testPagination() {
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderNo("ORD20240314010" + i);
            order.setUserId(1L);
            order.setMerchantId(1L);
            order.setTotalAmount(new BigDecimal("20.00"));
            order.setActualAmount(new BigDecimal("20.00"));
            order.setStatus(Constants.OrderStatus.WAIT_PAY);
            order.setPayStatus(Constants.PayStatus.UNPAID);
            order.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(order);
        }

        List<Order> page1 = orderMapper.selectByUserId(1L, 2, 0);
        List<Order> page2 = orderMapper.selectByUserId(1L, 2, 2);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(2, page1.size(), "第一页应返回2条记录");
        assertEquals(2, page2.size(), "第二页应返回2条记录");
    }

    @Test
    @DisplayName("测试统计用户订单数量")
    void testCountByUserId() {
        orderMapper.insert(testOrder);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140006");
        order2.setUserId(1L);
        order2.setMerchantId(2L);
        order2.setTotalAmount(new BigDecimal("30.00"));
        order2.setActualAmount(new BigDecimal("30.00"));
        order2.setStatus(Constants.OrderStatus.COMPLETED);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        long count = orderMapper.countByUserId(1L);

        assertTrue(count >= 2, "用户订单数量应至少为2");
    }

    @Test
    @DisplayName("测试统计用户指定状态订单数量")
    void testCountByUserIdAndStatus() {
        orderMapper.insert(testOrder);

        long waitPayCount = orderMapper.countByUserIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY);

        assertTrue(waitPayCount >= 1, "待支付订单数量应至少为1");
    }

    @Test
    @DisplayName("测试统计商家订单数量")
    void testCountByMerchantId() {
        orderMapper.insert(testOrder);

        long count = orderMapper.countByMerchantId(1L);

        assertTrue(count >= 1, "商家订单数量应至少为1");
    }

    @Test
    @DisplayName("测试统计商家指定状态订单数量")
    void testCountByMerchantIdAndStatus() {
        orderMapper.insert(testOrder);

        long waitPayCount = orderMapper.countByMerchantIdAndStatus(1L, Constants.OrderStatus.WAIT_PAY);

        assertTrue(waitPayCount >= 1, "商家待支付订单数量应至少为1");
    }

    @Test
    @DisplayName("测试统计商家总销售额")
    void testSumAmountByMerchantId() {
        orderMapper.insert(testOrder);

        BigDecimal totalAmount = orderMapper.sumAmountByMerchantId(1L);

        assertNotNull(totalAmount);
        assertTrue(totalAmount.compareTo(BigDecimal.ZERO) >= 0, "销售额应大于等于0");
    }

    @Test
    @DisplayName("测试统计商家待处理订单数")
    void testCountPendingOrdersByMerchantId() {
        Order order1 = new Order();
        order1.setOrderNo("ORD202403140007");
        order1.setUserId(1L);
        order1.setMerchantId(1L);
        order1.setTotalAmount(new BigDecimal("20.00"));
        order1.setActualAmount(new BigDecimal("20.00"));
        order1.setStatus(Constants.OrderStatus.WAIT_ACCEPT);
        order1.setPayStatus(Constants.PayStatus.PAID);
        order1.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order1.setCreateTime(LocalDateTime.now());
        order1.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order1);

        Order order2 = new Order();
        order2.setOrderNo("ORD202403140008");
        order2.setUserId(2L);
        order2.setMerchantId(1L);
        order2.setTotalAmount(new BigDecimal("25.00"));
        order2.setActualAmount(new BigDecimal("25.00"));
        order2.setStatus(Constants.OrderStatus.MAKING);
        order2.setPayStatus(Constants.PayStatus.PAID);
        order2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        order2.setCreateTime(LocalDateTime.now());
        order2.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order2);

        long pendingCount = orderMapper.countPendingOrdersByMerchantId(1L);

        assertTrue(pendingCount >= 2, "待处理订单数应至少为2");
    }

    @Test
    @DisplayName("测试查询用户订单列表（带分页）")
    void testSelectUserOrders() {
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setOrderNo("ORD20240314020" + i);
            order.setUserId(1L);
            order.setMerchantId(1L);
            order.setTotalAmount(new BigDecimal("20.00"));
            order.setActualAmount(new BigDecimal("20.00"));
            order.setStatus(Constants.OrderStatus.WAIT_PAY);
            order.setPayStatus(Constants.PayStatus.UNPAID);
            order.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(order);
        }

        List<Order> orders = orderMapper.selectUserOrders(1L, Constants.OrderStatus.WAIT_PAY, 1, 2);

        assertNotNull(orders);
        assertEquals(2, orders.size(), "应返回2条记录");
    }

    @Test
    @DisplayName("测试统计用户订单数量（带状态筛选）")
    void testCountUserOrders() {
        orderMapper.insert(testOrder);

        Long count = orderMapper.countUserOrders(1L, Constants.OrderStatus.WAIT_PAY);

        assertNotNull(count);
        assertTrue(count >= 1, "待支付订单数量应至少为1");
    }
}
