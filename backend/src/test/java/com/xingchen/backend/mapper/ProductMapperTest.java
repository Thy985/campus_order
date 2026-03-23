package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Product;
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
 * ProductMapper集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setMerchantId(1L);
        testProduct.setCategoryId(1);
        testProduct.setName("测试商品");
        testProduct.setSubtitle("测试商品副标题");
        testProduct.setImage("http://example.com/product.jpg");
        testProduct.setImages("[\"http://example.com/product1.jpg\",\"http://example.com/product2.jpg\"]");
        testProduct.setDescription("这是一个测试商品的详细描述");
        testProduct.setPrice(new BigDecimal("25.50"));
        testProduct.setOriginalPrice(new BigDecimal("30.00"));
        testProduct.setUnit("份");
        testProduct.setStock(100);
        testProduct.setSalesVolume(50);
        testProduct.setStatus(Constants.ProductStatus.ON_SHELF);
        testProduct.setSortOrder(10);
        testProduct.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        testProduct.setVersion(0);
        testProduct.setCreateTime(LocalDateTime.now());
        testProduct.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试商品插入操作")
    void testInsertProduct() {
        int result = productMapper.insert(testProduct);

        assertTrue(result > 0, "商品插入应该成功");
        assertNotNull(testProduct.getId(), "插入后商品ID不应为空");

        Product savedProduct = productMapper.selectOneById(testProduct.getId());
        assertNotNull(savedProduct, "查询到的商品不应为空");
        assertEquals(testProduct.getName(), savedProduct.getName());
        assertEquals(testProduct.getMerchantId(), savedProduct.getMerchantId());
        assertEquals(0, testProduct.getPrice().compareTo(savedProduct.getPrice()));
        assertEquals(testProduct.getStock(), savedProduct.getStock());
    }

    @Test
    @DisplayName("测试商品更新操作")
    void testUpdateProduct() {
        productMapper.insert(testProduct);

        testProduct.setName("更新后的商品名称");
        testProduct.setPrice(new BigDecimal("35.00"));
        testProduct.setStock(80);
        testProduct.setDescription("更新后的商品描述");
        testProduct.setUpdateTime(LocalDateTime.now());

        int result = productMapper.update(testProduct);

        assertTrue(result > 0, "商品更新应该成功");

        Product updatedProduct = productMapper.selectOneById(testProduct.getId());
        assertEquals("更新后的商品名称", updatedProduct.getName());
        assertEquals(0, new BigDecimal("35.00").compareTo(updatedProduct.getPrice()));
        assertEquals(80, updatedProduct.getStock());
        assertEquals("更新后的商品描述", updatedProduct.getDescription());
    }

    @Test
    @DisplayName("测试根据ID查询商品")
    void testSelectById() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();

        Product foundProduct = productMapper.selectOneById(productId);

        assertNotNull(foundProduct, "根据ID应能查询到商品");
        assertEquals(productId, foundProduct.getId());
        assertEquals(testProduct.getName(), foundProduct.getName());
    }

    @Test
    @DisplayName("测试根据商家ID查询商品列表")
    void testSelectByMerchantId() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(2);
        product2.setName("测试商品2");
        product2.setPrice(new BigDecimal("15.00"));
        product2.setUnit("份");
        product2.setStock(50);
        product2.setStatus(Constants.ProductStatus.ON_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        List<Product> products = productMapper.selectByMerchantId(1L, 10, 0);

        assertNotNull(products);
        assertTrue(products.size() >= 2, "应至少返回2条商品记录");
        assertTrue(products.stream().allMatch(p -> p.getMerchantId().equals(1L)),
                "所有商品应属于同一商家");
    }

    @Test
    @DisplayName("测试根据商家ID和分类ID查询商品列表")
    void testSelectByMerchantIdAndCategoryId() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(2);
        product2.setName("测试商品2");
        product2.setPrice(new BigDecimal("15.00"));
        product2.setUnit("份");
        product2.setStock(50);
        product2.setStatus(Constants.ProductStatus.ON_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        List<Product> category1Products = productMapper.selectByMerchantIdAndCategoryId(1L, 1, 10, 0);

        assertNotNull(category1Products);
        assertTrue(category1Products.stream().allMatch(p ->
                p.getMerchantId().equals(1L) && p.getCategoryId().equals(1)),
                "所有商品应属于同一商家且分类ID为1");
    }

    @Test
    @DisplayName("测试根据商家ID和状态查询商品列表")
    void testSelectByMerchantIdAndStatus() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(1);
        product2.setName("下架商品");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setUnit("份");
        product2.setStock(30);
        product2.setStatus(Constants.ProductStatus.OFF_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        List<Product> onShelfProducts = productMapper.selectByMerchantIdAndStatus(1L, Constants.ProductStatus.ON_SHELF, 10, 0);

        assertNotNull(onShelfProducts);
        assertTrue(onShelfProducts.stream().allMatch(p ->
                p.getMerchantId().equals(1L) && p.getStatus().equals(Constants.ProductStatus.ON_SHELF)),
                "所有商品应属于同一商家且状态为上架");
    }

    @Test
    @DisplayName("测试库存更新操作-扣减库存")
    void testUpdateStock() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();
        int initialStock = testProduct.getStock();

        int result = productMapper.updateStock(productId, 10);

        assertTrue(result > 0, "库存扣减应该成功");

        Product updatedProduct = productMapper.selectOneById(productId);
        assertEquals(initialStock - 10, updatedProduct.getStock(), "库存应减少10");
    }

    @Test
    @DisplayName("测试库存更新操作-库存不足")
    void testUpdateStock_NotEnough() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();

        int result = productMapper.updateStock(productId, 200);

        assertEquals(0, result, "库存不足时应返回0");

        Product product = productMapper.selectOneById(productId);
        assertEquals(100, product.getStock(), "库存应保持不变");
    }

    @Test
    @DisplayName("测试回滚库存")
    void testRollbackStock() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();
        int initialStock = testProduct.getStock();

        productMapper.updateStock(productId, 20);

        int result = productMapper.rollbackStock(productId, 20);

        assertTrue(result > 0, "库存回滚应该成功");

        Product updatedProduct = productMapper.selectOneById(productId);
        assertEquals(initialStock, updatedProduct.getStock(), "库存应恢复到初始值");
    }

    @Test
    @DisplayName("测试恢复库存")
    void testRestoreStock() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();
        int initialStock = testProduct.getStock();

        int result = productMapper.restoreStock(productId, 30);

        assertTrue(result > 0, "库存恢复应该成功");

        Product updatedProduct = productMapper.selectOneById(productId);
        assertEquals(initialStock + 30, updatedProduct.getStock(), "库存应增加30");
    }

    @Test
    @DisplayName("测试搜索商品")
    void testSearchProduct() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(1);
        product2.setName("美味汉堡");
        product2.setSubtitle("汉堡副标题");
        product2.setDescription("这是汉堡的描述");
        product2.setPrice(new BigDecimal("28.00"));
        product2.setUnit("份");
        product2.setStock(50);
        product2.setStatus(Constants.ProductStatus.ON_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        List<Product> searchResults = productMapper.searchProduct(1L, "汉堡", 10, 0);

        assertNotNull(searchResults);
        assertTrue(searchResults.size() >= 1, "应至少返回1条搜索结果");
    }

    @Test
    @DisplayName("测试根据条件筛选商品")
    void testSelectByMerchantIdWithFilter() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(1);
        product2.setName("测试商品2");
        product2.setPrice(new BigDecimal("15.00"));
        product2.setUnit("份");
        product2.setStock(50);
        product2.setStatus(Constants.ProductStatus.OFF_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        List<Product> filteredProducts = productMapper.selectByMerchantIdWithFilter(1L, Constants.ProductStatus.ON_SHELF, "测试", 10, 0);

        assertNotNull(filteredProducts);
        assertTrue(filteredProducts.stream().allMatch(p ->
                p.getMerchantId().equals(1L) &&
                        p.getStatus().equals(Constants.ProductStatus.ON_SHELF) &&
                        p.getName().contains("测试")),
                "所有商品应符合筛选条件");
    }

    @Test
    @DisplayName("测试统计商家商品数量")
    void testCountByMerchantIdWithFilter() {
        productMapper.insert(testProduct);

        Product product2 = new Product();
        product2.setMerchantId(1L);
        product2.setCategoryId(1);
        product2.setName("测试商品2");
        product2.setPrice(new BigDecimal("15.00"));
        product2.setUnit("份");
        product2.setStock(50);
        product2.setStatus(Constants.ProductStatus.OFF_SHELF);
        product2.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);

        long totalCount = productMapper.countByMerchantIdWithFilter(1L, null, null);
        long onShelfCount = productMapper.countByMerchantIdWithFilter(1L, Constants.ProductStatus.ON_SHELF, null);

        assertTrue(totalCount >= 2, "总商品数应至少为2");
        assertTrue(onShelfCount >= 1, "上架商品数应至少为1");
    }

    @Test
    @DisplayName("测试商品分页查询")
    void testPagination() {
        for (int i = 0; i < 5; i++) {
            Product product = new Product();
            product.setMerchantId(1L);
            product.setCategoryId(1);
            product.setName("分页商品" + i);
            product.setPrice(new BigDecimal("10.00"));
            product.setUnit("份");
            product.setStock(100);
            product.setStatus(Constants.ProductStatus.ON_SHELF);
            product.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.insert(product);
        }

        List<Product> page1 = productMapper.selectByMerchantId(1L, 2, 0);
        List<Product> page2 = productMapper.selectByMerchantId(1L, 2, 2);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(2, page1.size(), "第一页应返回2条记录");
        assertEquals(2, page2.size(), "第二页应返回2条记录");
    }

    @Test
    @DisplayName("测试商品版本号更新")
    void testVersionUpdate() {
        productMapper.insert(testProduct);
        Long productId = testProduct.getId();
        int initialVersion = testProduct.getVersion();

        productMapper.updateStock(productId, 10);

        Product updatedProduct = productMapper.selectOneById(productId);
        assertTrue(updatedProduct.getVersion() > initialVersion, "版本号应该增加");
    }

    @Test
    @DisplayName("测试逻辑删除商品")
    void testDeleteProduct() {
        productMapper.insert(testProduct);

        testProduct.setIsDeleted(Constants.DeleteFlag.DELETED);
        productMapper.update(testProduct);

        Product deletedProduct = productMapper.selectOneById(testProduct.getId());
        assertEquals(Constants.DeleteFlag.DELETED, deletedProduct.getIsDeleted());
    }
}
