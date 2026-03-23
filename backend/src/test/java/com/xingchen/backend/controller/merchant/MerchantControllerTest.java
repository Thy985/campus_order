package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingchen.backend.config.RateLimitConfig;
import com.xingchen.backend.dto.request.merchant.MerchantListRequest;
import com.xingchen.backend.dto.request.product.ProductListRequest;
import com.xingchen.backend.dto.response.merchant.MerchantDetailResponse;
import com.xingchen.backend.dto.response.merchant.MerchantListResponse;
import com.xingchen.backend.dto.response.product.ProductListResponse;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.MerchantCategory;
import com.xingchen.backend.entity.Product;
import com.xingchen.backend.entity.ProductCategory;
import com.xingchen.backend.service.MerchantService;
import com.xingchen.backend.service.ProductCategoryService;
import com.xingchen.backend.service.ProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商家控制器单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductCategoryService productCategoryService;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_MERCHANT_ID = 100L;
    private static final String TEST_TOKEN = "test-token-12345";

    @BeforeEach
    void setUp() {
        // 模拟Sa-Token登录状态
        mockStatic(StpUtil.class);
        when(StpUtil.getLoginIdAsLong()).thenReturn(TEST_USER_ID);
        when(StpUtil.isLogin()).thenReturn(true);
    }

    private Merchant createTestMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(TEST_MERCHANT_ID);
        merchant.setName("测试商家");
        merchant.setLogo("http://example.com/logo.jpg");
        merchant.setBanner("http://example.com/banner.jpg");
        merchant.setCategoryId(1);
        merchant.setDescription("这是一个测试商家");
        merchant.setNotice("商家公告");
        merchant.setPhone("13800138000");
        merchant.setAvgPrice(new BigDecimal("25.00"));
        merchant.setRating(new BigDecimal("4.5"));
        merchant.setSalesVolume(1000);
        merchant.setStatus(1);
        merchant.setSortOrder(1);
        merchant.setIsDeleted(0);
        merchant.setAddress("测试地址");
        merchant.setMinOrderAmount(new BigDecimal("15.00"));
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        return merchant;
    }

    private MerchantCategory createTestCategory() {
        MerchantCategory category = new MerchantCategory();
        category.setId(1);
        category.setName("美食");
        category.setIcon("http://example.com/icon.jpg");
        category.setSortOrder(1);
        category.setIsDeleted(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试商品");
        product.setDescription("测试商品描述");
        product.setPrice(new BigDecimal("20.00"));
        product.setOriginalPrice(new BigDecimal("25.00"));
        product.setImage("http://example.com/product.jpg");
        product.setMerchantId(TEST_MERCHANT_ID);
        product.setCategoryId(1);
        product.setStock(100);
        product.setSalesVolume(50);
        product.setStatus(1);
        product.setSortOrder(1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        return product;
    }

    /**
     * 测试获取商家列表接口 GET /api/merchant/list
     */
    @Test
    void testGetMerchantList() throws Exception {
        // 准备响应数据
        MerchantListResponse response = new MerchantListResponse();
        response.setTotal(10L);
        response.setPage(1);
        response.setPageSize(10);
        response.setTotalPages(1);

        Merchant merchant = createTestMerchant();
        response.setMerchantList(Collections.singletonList(merchant));

        when(merchantService.getMerchantList(any(MerchantListRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/list")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.merchantList[0].id").value(TEST_MERCHANT_ID))
                .andExpect(jsonPath("$.data.merchantList[0].name").value("测试商家"))
                .andExpect(jsonPath("$.data.merchantList[0].rating").value(4.5));

        verify(merchantService, times(1)).getMerchantList(any(MerchantListRequest.class));
    }

    /**
     * 测试获取商家列表接口 - 参数校验失败
     */
    @Test
    void testGetMerchantList_InvalidParams() throws Exception {
        // page小于1
        mockMvc.perform(get("/api/merchant/list")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("页码必须大于等于1"));

        // pageSize超出范围
        mockMvc.perform(get("/api/merchant/list")
                        .param("page", "1")
                        .param("pageSize", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("每页数量必须在1-100之间"));

        verify(merchantService, never()).getMerchantList(any(MerchantListRequest.class));
    }

    /**
     * 测试获取商家详情接口 GET /api/merchant/detail/{id}
     */
    @Test
    void testGetMerchantDetail() throws Exception {
        // 准备响应数据
        MerchantDetailResponse response = new MerchantDetailResponse();
        Merchant merchant = createTestMerchant();
        MerchantCategory category = createTestCategory();
        response.setMerchant(merchant);
        response.setCategory(category);

        when(merchantService.getMerchantDetail(eq(TEST_MERCHANT_ID))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/detail/{id}", TEST_MERCHANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.merchant.id").value(TEST_MERCHANT_ID))
                .andExpect(jsonPath("$.data.merchant.name").value("测试商家"))
                .andExpect(jsonPath("$.data.merchant.description").value("这是一个测试商家"))
                .andExpect(jsonPath("$.data.category.name").value("美食"));

        verify(merchantService, times(1)).getMerchantDetail(eq(TEST_MERCHANT_ID));
    }

    /**
     * 测试获取商家详情接口 - 商家不存在
     */
    @Test
    void testGetMerchantDetail_NotFound() throws Exception {
        when(merchantService.getMerchantDetail(eq(999L))).thenReturn(null);

        mockMvc.perform(get("/api/merchant/detail/{id}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("商家不存在"));

        verify(merchantService, times(1)).getMerchantDetail(eq(999L));
    }

    /**
     * 测试获取商家分类列表接口 GET /api/merchant/category/list
     */
    @Test
    void testGetMerchantCategoryList() throws Exception {
        // 准备响应数据
        List<MerchantCategory> categories = new ArrayList<>();
        categories.add(createTestCategory());

        MerchantCategory category2 = createTestCategory();
        category2.setId(2);
        category2.setName("饮品");
        categories.add(category2);

        when(merchantService.getMerchantCategoryList()).thenReturn(categories);

        mockMvc.perform(get("/api/merchant/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("美食"))
                .andExpect(jsonPath("$.data[1].name").value("饮品"));

        verify(merchantService, times(1)).getMerchantCategoryList();
    }

    /**
     * 测试创建商家接口 POST /api/merchant
     */
    @Test
    void testCreateMerchant() throws Exception {
        Merchant merchant = createTestMerchant();
        when(merchantService.createMerchant(any(Merchant.class))).thenReturn(merchant);

        Merchant request = new Merchant();
        request.setName("新商家");
        request.setDescription("新商家描述");
        request.setPhone("13900139000");

        mockMvc.perform(post("/api/merchant")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_MERCHANT_ID))
                .andExpect(jsonPath("$.data.name").value("测试商家"));

        verify(merchantService, times(1)).createMerchant(any(Merchant.class));
    }

    /**
     * 测试创建商家接口 - 商家名称为空
     */
    @Test
    void testCreateMerchant_EmptyName() throws Exception {
        Merchant request = new Merchant();
        request.setName(""); // 空名称
        request.setDescription("新商家描述");

        mockMvc.perform(post("/api/merchant")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("商家名称不能为空"));

        verify(merchantService, never()).createMerchant(any(Merchant.class));
    }

    /**
     * 测试更新商家接口 PUT /api/merchant/{id}
     */
    @Test
    void testUpdateMerchant() throws Exception {
        Merchant existingMerchant = createTestMerchant();
        when(merchantService.getMerchantById(eq(TEST_MERCHANT_ID))).thenReturn(existingMerchant);

        Merchant updatedMerchant = createTestMerchant();
        updatedMerchant.setName("更新后的商家名称");
        when(merchantService.updateMerchant(any(Merchant.class))).thenReturn(updatedMerchant);

        Merchant request = new Merchant();
        request.setName("更新后的商家名称");
        request.setDescription("更新后的描述");

        mockMvc.perform(put("/api/merchant/{id}", TEST_MERCHANT_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后的商家名称"));

        verify(merchantService, times(1)).getMerchantById(eq(TEST_MERCHANT_ID));
        verify(merchantService, times(1)).updateMerchant(any(Merchant.class));
    }

    /**
     * 测试更新商家接口 - 商家不存在
     */
    @Test
    void testUpdateMerchant_NotFound() throws Exception {
        when(merchantService.getMerchantById(eq(999L))).thenReturn(null);

        Merchant request = new Merchant();
        request.setName("更新名称");

        mockMvc.perform(put("/api/merchant/{id}", 999)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("商家不存在"));

        verify(merchantService, times(1)).getMerchantById(eq(999L));
        verify(merchantService, never()).updateMerchant(any(Merchant.class));
    }

    /**
     * 测试删除商家接口 DELETE /api/merchant/{id}
     */
    @Test
    void testDeleteMerchant() throws Exception {
        Merchant existingMerchant = createTestMerchant();
        when(merchantService.getMerchantById(eq(TEST_MERCHANT_ID))).thenReturn(existingMerchant);

        mockMvc.perform(delete("/api/merchant/{id}", TEST_MERCHANT_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("商家删除成功"));

        verify(merchantService, times(1)).getMerchantById(eq(TEST_MERCHANT_ID));
        verify(merchantService, times(1)).deleteMerchant(eq(TEST_MERCHANT_ID));
    }

    /**
     * 测试删除商家接口 - 商家不存在
     */
    @Test
    void testDeleteMerchant_NotFound() throws Exception {
        when(merchantService.getMerchantById(eq(999L))).thenReturn(null);

        mockMvc.perform(delete("/api/merchant/{id}", 999)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("商家不存在"));

        verify(merchantService, never()).deleteMerchant(any());
    }

    /**
     * 测试更新商家状态接口 PUT /api/merchant/{id}/status
     */
    @Test
    void testUpdateMerchantStatus() throws Exception {
        Merchant existingMerchant = createTestMerchant();
        when(merchantService.getMerchantById(eq(TEST_MERCHANT_ID))).thenReturn(existingMerchant);

        Merchant updatedMerchant = createTestMerchant();
        updatedMerchant.setStatus(0);
        when(merchantService.updateMerchant(any(Merchant.class))).thenReturn(updatedMerchant);

        mockMvc.perform(put("/api/merchant/{id}/status", TEST_MERCHANT_ID)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("商家状态更新成功"));

        verify(merchantService, times(1)).getMerchantById(eq(TEST_MERCHANT_ID));
        verify(merchantService, times(1)).updateMerchant(any(Merchant.class));
    }

    /**
     * 测试搜索商家接口 GET /api/merchant/search
     */
    @Test
    void testSearchMerchants() throws Exception {
        MerchantListResponse response = new MerchantListResponse();
        response.setTotal(5L);
        response.setPage(1);
        response.setPageSize(10);
        response.setMerchantList(Collections.singletonList(createTestMerchant()));

        when(merchantService.getMerchantList(any(MerchantListRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/search")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.merchantList[0].name").value("测试商家"));

        verify(merchantService, times(1)).getMerchantList(any(MerchantListRequest.class));
    }

    /**
     * 测试获取附近商家接口 GET /api/merchant/nearby
     */
    @Test
    void testGetNearbyMerchants() throws Exception {
        MerchantListResponse response = new MerchantListResponse();
        response.setTotal(3L);
        response.setPage(1);
        response.setPageSize(10);
        response.setMerchantList(Collections.singletonList(createTestMerchant()));

        when(merchantService.getMerchantList(any(MerchantListRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/nearby")
                        .param("latitude", "39.9042")
                        .param("longitude", "116.4074")
                        .param("distance", "5000")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3));

        verify(merchantService, times(1)).getMerchantList(any(MerchantListRequest.class));
    }

    /**
     * 测试获取热门商家接口 GET /api/merchant/hot
     */
    @Test
    void testGetHotMerchants() throws Exception {
        MerchantListResponse response = new MerchantListResponse();
        response.setMerchantList(Collections.singletonList(createTestMerchant()));

        when(merchantService.getMerchantList(any(MerchantListRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/hot")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("测试商家"));

        verify(merchantService, times(1)).getMerchantList(any(MerchantListRequest.class));
    }

    /**
     * 测试获取商家商品接口 GET /api/merchant/products
     */
    @Test
    void testGetProducts() throws Exception {
        ProductListResponse response = new ProductListResponse();
        response.setTotal(10L);
        response.setPage(1);
        response.setPageSize(20);
        response.setTotalPages(1);
        response.setProductList(Collections.singletonList(createTestProduct()));

        when(productService.getProductList(any(ProductListRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/merchant/products")
                        .param("merchantId", TEST_MERCHANT_ID.toString())
                        .param("categoryId", "1")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.productList[0].id").value(1))
                .andExpect(jsonPath("$.data.productList[0].name").value("测试商品"))
                .andExpect(jsonPath("$.data.productList[0].price").value(20.00));

        verify(productService, times(1)).getProductList(any(ProductListRequest.class));
    }

    /**
     * 测试获取商品分类接口 GET /api/merchant/products/categories
     */
    @Test
    void testGetProductCategories() throws Exception {
        // 准备响应数据
        List<ProductCategory> categories = new ArrayList<>();

        ProductCategory category1 = new ProductCategory();
        category1.setId(1);
        category1.setName("主食");
        category1.setParentId(0);
        category1.setSortOrder(1);
        category1.setStatus(1);
        categories.add(category1);

        ProductCategory category2 = new ProductCategory();
        category2.setId(2);
        category2.setName("小吃");
        category2.setParentId(0);
        category2.setSortOrder(2);
        category2.setStatus(1);
        categories.add(category2);

        when(productCategoryService.getProductCategoryList()).thenReturn(categories);

        mockMvc.perform(get("/api/merchant/products/categories")
                        .param("merchantId", TEST_MERCHANT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("主食"))
                .andExpect(jsonPath("$.data[1].name").value("小吃"));

        verify(productCategoryService, times(1)).getProductCategoryList();
    }
}
