package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Merchant;
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
 * MerchantMapper集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MerchantMapperTest {

    @Autowired
    private MerchantMapper merchantMapper;

    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        testMerchant = new Merchant();
        testMerchant.setName("测试商家");
        testMerchant.setLogo("http://example.com/logo.jpg");
        testMerchant.setBanner("http://example.com/banner.jpg");
        testMerchant.setCategoryId(1);
        testMerchant.setDescription("这是一个测试商家的描述");
        testMerchant.setNotice("商家公告：欢迎光临");
        testMerchant.setPhone("13800138000");
        testMerchant.setAddress("测试地址：某某街道123号");
        testMerchant.setAvgPrice(new BigDecimal("25.50"));
        testMerchant.setRating(new BigDecimal("4.8"));
        testMerchant.setSalesVolume(1000);
        testMerchant.setStatus(Constants.MerchantStatus.OPEN);
        testMerchant.setSortOrder(10);
        testMerchant.setMinOrderAmount(new BigDecimal("15.00"));
        testMerchant.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        testMerchant.setCreateTime(LocalDateTime.now());
        testMerchant.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试商家插入操作")
    void testInsertMerchant() {
        int result = merchantMapper.insert(testMerchant);

        assertTrue(result > 0, "商家插入应该成功");
        assertNotNull(testMerchant.getId(), "插入后商家ID不应为空");

        Merchant savedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertNotNull(savedMerchant, "查询到的商家不应为空");
        assertEquals(testMerchant.getName(), savedMerchant.getName());
        assertEquals(testMerchant.getPhone(), savedMerchant.getPhone());
        assertEquals(0, testMerchant.getRating().compareTo(savedMerchant.getRating()));
        assertEquals(testMerchant.getSalesVolume(), savedMerchant.getSalesVolume());
    }

    @Test
    @DisplayName("测试商家更新操作")
    void testUpdateMerchant() {
        merchantMapper.insert(testMerchant);

        testMerchant.setName("更新后的商家名称");
        testMerchant.setDescription("更新后的商家描述");
        testMerchant.setNotice("更新后的公告");
        testMerchant.setPhone("13900139000");
        testMerchant.setRating(new BigDecimal("4.9"));
        testMerchant.setSalesVolume(1500);
        testMerchant.setStatus(Constants.MerchantStatus.CLOSED);
        testMerchant.setUpdateTime(LocalDateTime.now());

        int result = merchantMapper.update(testMerchant);

        assertTrue(result > 0, "商家更新应该成功");

        Merchant updatedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals("更新后的商家名称", updatedMerchant.getName());
        assertEquals("更新后的商家描述", updatedMerchant.getDescription());
        assertEquals("更新后的公告", updatedMerchant.getNotice());
        assertEquals("13900139000", updatedMerchant.getPhone());
        assertEquals(0, new BigDecimal("4.9").compareTo(updatedMerchant.getRating()));
        assertEquals(1500, updatedMerchant.getSalesVolume());
        assertEquals(Constants.MerchantStatus.CLOSED, updatedMerchant.getStatus());
    }

    @Test
    @DisplayName("测试根据ID查询商家")
    void testSelectById() {
        merchantMapper.insert(testMerchant);
        Long merchantId = testMerchant.getId();

        Merchant foundMerchant = merchantMapper.selectOneById(merchantId);

        assertNotNull(foundMerchant, "根据ID应能查询到商家");
        assertEquals(merchantId, foundMerchant.getId());
        assertEquals(testMerchant.getName(), foundMerchant.getName());
    }

    @Test
    @DisplayName("测试查询商家列表")
    void testSelectMerchantList() {
        merchantMapper.insert(testMerchant);

        Merchant merchant2 = new Merchant();
        merchant2.setName("测试商家2");
        merchant2.setCategoryId(2);
        merchant2.setPhone("13900139000");
        merchant2.setAvgPrice(new BigDecimal("30.00"));
        merchant2.setRating(new BigDecimal("4.5"));
        merchant2.setSalesVolume(800);
        merchant2.setStatus(Constants.MerchantStatus.OPEN);
        merchant2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchant2.setCreateTime(LocalDateTime.now());
        merchant2.setUpdateTime(LocalDateTime.now());
        merchantMapper.insert(merchant2);

        Merchant merchant3 = new Merchant();
        merchant3.setName("休息中商家");
        merchant3.setCategoryId(1);
        merchant3.setPhone("13700137000");
        merchant3.setAvgPrice(new BigDecimal("20.00"));
        merchant3.setRating(new BigDecimal("4.0"));
        merchant3.setSalesVolume(500);
        merchant3.setStatus(Constants.MerchantStatus.CLOSED);
        merchant3.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchant3.setCreateTime(LocalDateTime.now());
        merchant3.setUpdateTime(LocalDateTime.now());
        merchantMapper.insert(merchant3);

        List<Merchant> allMerchants = merchantMapper.selectMerchantList(null, null, 10, 0);
        List<Merchant> category1Merchants = merchantMapper.selectMerchantList(1, null, 10, 0);
        List<Merchant> openMerchants = merchantMapper.selectMerchantList(null, Constants.MerchantStatus.OPEN, 10, 0);
        List<Merchant> category1OpenMerchants = merchantMapper.selectMerchantList(1, Constants.MerchantStatus.OPEN, 10, 0);

        assertNotNull(allMerchants);
        assertTrue(allMerchants.size() >= 3, "应至少返回3条商家记录");

        assertNotNull(category1Merchants);
        assertTrue(category1Merchants.stream().allMatch(m -> m.getCategoryId().equals(1)),
                "所有商家应属于分类1");

        assertNotNull(openMerchants);
        assertTrue(openMerchants.stream().allMatch(m -> m.getStatus().equals(Constants.MerchantStatus.OPEN)),
                "所有商家应处于营业状态");

        assertNotNull(category1OpenMerchants);
        assertTrue(category1OpenMerchants.stream().allMatch(m ->
                m.getCategoryId().equals(1) && m.getStatus().equals(Constants.MerchantStatus.OPEN)),
                "所有商家应属于分类1且处于营业状态");
    }

    @Test
    @DisplayName("测试根据分类ID查询商家列表")
    void testSelectByCategoryId() {
        merchantMapper.insert(testMerchant);

        Merchant merchant2 = new Merchant();
        merchant2.setName("分类2商家");
        merchant2.setCategoryId(2);
        merchant2.setPhone("13900139000");
        merchant2.setAvgPrice(new BigDecimal("35.00"));
        merchant2.setRating(new BigDecimal("4.5"));
        merchant2.setSalesVolume(800);
        merchant2.setStatus(Constants.MerchantStatus.OPEN);
        merchant2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchant2.setCreateTime(LocalDateTime.now());
        merchant2.setUpdateTime(LocalDateTime.now());
        merchantMapper.insert(merchant2);

        List<Merchant> category1Merchants = merchantMapper.selectByCategoryId(1, 10, 0);

        assertNotNull(category1Merchants);
        assertTrue(category1Merchants.stream().allMatch(m -> m.getCategoryId().equals(1)),
                "所有商家应属于分类1");
    }

    @Test
    @DisplayName("测试根据状态查询商家列表")
    void testSelectByStatus() {
        merchantMapper.insert(testMerchant);

        Merchant merchant2 = new Merchant();
        merchant2.setName("休息中商家");
        merchant2.setCategoryId(1);
        merchant2.setPhone("13700137000");
        merchant2.setAvgPrice(new BigDecimal("25.00"));
        merchant2.setRating(new BigDecimal("4.0"));
        merchant2.setSalesVolume(500);
        merchant2.setStatus(Constants.MerchantStatus.CLOSED);
        merchant2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchant2.setCreateTime(LocalDateTime.now());
        merchant2.setUpdateTime(LocalDateTime.now());
        merchantMapper.insert(merchant2);

        List<Merchant> openMerchants = merchantMapper.selectByStatus(Constants.MerchantStatus.OPEN, 10, 0);

        assertNotNull(openMerchants);
        assertTrue(openMerchants.stream().allMatch(m -> m.getStatus().equals(Constants.MerchantStatus.OPEN)),
                "所有商家应处于营业状态");
    }

    @Test
    @DisplayName("测试搜索商家")
    void testSearchMerchant() {
        merchantMapper.insert(testMerchant);

        Merchant merchant2 = new Merchant();
        merchant2.setName("美味餐厅");
        merchant2.setCategoryId(1);
        merchant2.setDescription("这是一家美味餐厅的描述");
        merchant2.setPhone("13900139000");
        merchant2.setAvgPrice(new BigDecimal("40.00"));
        merchant2.setRating(new BigDecimal("4.7"));
        merchant2.setSalesVolume(1200);
        merchant2.setStatus(Constants.MerchantStatus.OPEN);
        merchant2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchant2.setCreateTime(LocalDateTime.now());
        merchant2.setUpdateTime(LocalDateTime.now());
        merchantMapper.insert(merchant2);

        List<Merchant> searchResults = merchantMapper.searchMerchant("美味", 10, 0);

        assertNotNull(searchResults);
        assertTrue(searchResults.size() >= 1, "应至少返回1条搜索结果");
    }

    @Test
    @DisplayName("测试商家分页查询")
    void testPagination() {
        for (int i = 0; i < 5; i++) {
            Merchant merchant = new Merchant();
            merchant.setName("分页商家" + i);
            merchant.setCategoryId(1);
            merchant.setPhone("1380000000" + i);
            merchant.setAvgPrice(new BigDecimal("20.00"));
            merchant.setRating(new BigDecimal("4.0"));
            merchant.setSalesVolume(100 * i);
            merchant.setStatus(Constants.MerchantStatus.OPEN);
            merchant.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            merchant.setCreateTime(LocalDateTime.now());
            merchant.setUpdateTime(LocalDateTime.now());
            merchantMapper.insert(merchant);
        }

        List<Merchant> page1 = merchantMapper.selectMerchantList(null, null, 2, 0);
        List<Merchant> page2 = merchantMapper.selectMerchantList(null, null, 2, 2);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(2, page1.size(), "第一页应返回2条记录");
        assertEquals(2, page2.size(), "第二页应返回2条记录");
    }

    @Test
    @DisplayName("测试更新商家评分")
    void testUpdateRating() {
        merchantMapper.insert(testMerchant);

        testMerchant.setRating(new BigDecimal("4.9"));
        merchantMapper.update(testMerchant);

        Merchant updatedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(0, new BigDecimal("4.9").compareTo(updatedMerchant.getRating()));
    }

    @Test
    @DisplayName("测试更新商家销量")
    void testUpdateSalesVolume() {
        merchantMapper.insert(testMerchant);

        testMerchant.setSalesVolume(2000);
        merchantMapper.update(testMerchant);

        Merchant updatedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(2000, updatedMerchant.getSalesVolume());
    }

    @Test
    @DisplayName("测试更新商家排序")
    void testUpdateSortOrder() {
        merchantMapper.insert(testMerchant);

        testMerchant.setSortOrder(100);
        merchantMapper.update(testMerchant);

        Merchant updatedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(100, updatedMerchant.getSortOrder());
    }

    @Test
    @DisplayName("测试更新商家起送金额")
    void testUpdateMinOrderAmount() {
        merchantMapper.insert(testMerchant);

        testMerchant.setMinOrderAmount(new BigDecimal("20.00"));
        merchantMapper.update(testMerchant);

        Merchant updatedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(0, new BigDecimal("20.00").compareTo(updatedMerchant.getMinOrderAmount()));
    }

    @Test
    @DisplayName("测试逻辑删除商家")
    void testDeleteMerchant() {
        merchantMapper.insert(testMerchant);

        testMerchant.setIsDeleted(Constants.DeleteFlag.DELETED);
        merchantMapper.update(testMerchant);

        Merchant deletedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(Constants.DeleteFlag.DELETED, deletedMerchant.getIsDeleted());
    }

    @Test
    @DisplayName("测试商家营业状态切换")
    void testToggleStatus() {
        merchantMapper.insert(testMerchant);

        assertEquals(Constants.MerchantStatus.OPEN, testMerchant.getStatus());

        testMerchant.setStatus(Constants.MerchantStatus.CLOSED);
        merchantMapper.update(testMerchant);

        Merchant closedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(Constants.MerchantStatus.CLOSED, closedMerchant.getStatus());

        closedMerchant.setStatus(Constants.MerchantStatus.OPEN);
        merchantMapper.update(closedMerchant);

        Merchant reopenedMerchant = merchantMapper.selectOneById(testMerchant.getId());
        assertEquals(Constants.MerchantStatus.OPEN, reopenedMerchant.getStatus());
    }
}
