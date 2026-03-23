package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Product;
import com.xingchen.backend.entity.ProductCategory;
import com.xingchen.backend.mapper.ProductMapper;
import com.xingchen.backend.mapper.ProductCategoryMapper;
import com.xingchen.backend.service.ProductService;
import com.xingchen.backend.dto.request.product.ProductListRequest;
import com.xingchen.backend.dto.response.product.ProductDetailResponse;
import com.xingchen.backend.dto.response.product.ProductListResponse;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public ProductListResponse getProductList(ProductListRequest request) {
        // 计算分页参数
        int limit = request.getPageSize();
        int offset = (request.getPage() - 1) * request.getPageSize();

        List<Product> productList;

        // 构建总数查询条件
        QueryWrapper countWrapper = QueryWrapper.create()
                .from(Product.class)
                .where("merchant_id = ? AND is_deleted = 0", request.getMerchantId());

        // 根据参数查询商品列表
        if (request.getCategoryId() != null) {
            countWrapper.and("category_id = ?", request.getCategoryId());
            productList = productMapper.selectByMerchantIdAndCategoryId(request.getMerchantId(), request.getCategoryId(), limit, offset);
        } else {
            productList = productMapper.selectByMerchantId(request.getMerchantId(), limit, offset);
        }

        // 查询真实总记录数
        long total = productMapper.selectCountByQuery(countWrapper);
        int totalPages = (int) Math.ceil((double) total / request.getPageSize());

        // 构建响应
        ProductListResponse response = new ProductListResponse();
        response.setProductList(productList);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setTotal(total);
        response.setTotalPages(totalPages);

        return response;
    }

    @Override
    public ProductDetailResponse getProductDetail(Long id) {
        // 查询商品详情
        Product product = productMapper.selectOneById(id);
        if (product == null) {
            return null;
        }

        // 查询商品分类
        ProductCategory category = null;
        if (product.getCategoryId() != null) {
            category = productCategoryMapper.selectOneById(product.getCategoryId());
        }

        // 构建响应
        ProductDetailResponse response = new ProductDetailResponse();
        response.setProduct(product);
        response.setCategory(category);

        return response;
    }

    @Override
    public Product createProduct(Product product) {
        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getIsDeleted() == null) {
            product.setIsDeleted(0);
        }
        if (product.getSortOrder() == null) {
            product.setSortOrder(0);
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }

        // 保存商品
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        productMapper.update(product);
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public void updateStock(Long productId, Integer stock) {
        Product product = productMapper.selectOneById(productId);
        if (product != null) {
            product.setStock(stock);
            productMapper.update(product);
        }
    }

    @Override
    public boolean decreaseStock(Long productId, Integer quantity) {
        Product product = productMapper.selectOneById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }

        product.setStock(product.getStock() - quantity);
        productMapper.update(product);
        return true;
    }

    @Override
    public void increaseStock(Long productId, Integer quantity) {
        Product product = productMapper.selectOneById(productId);
        if (product != null) {
            product.setStock(product.getStock() + quantity);
            productMapper.update(product);
        }
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectOneById(id);
    }

    @Override
    public List<Product> getProductsByMerchantId(Long merchantId) {
        return productMapper.selectByMerchantId(merchantId, 100, 0);
    }

    @Override
    public ProductListResponse searchProducts(ProductListRequest request) {
        // 计算分页参数
        int limit = request.getPageSize();
        int offset = (request.getPage() - 1) * request.getPageSize();

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("is_deleted = 0");

        // 如果指定了商家ID
        if (request.getMerchantId() != null) {
            queryWrapper.and("merchant_id = ?", request.getMerchantId());
        }

        // 如果指定了分类ID
        if (request.getCategoryId() != null) {
            queryWrapper.and("category_id = ?", request.getCategoryId());
        }

        // 关键词搜索（按名称和描述）
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = "%" + request.getKeyword().trim() + "%";
            queryWrapper.and("(name LIKE ? OR description LIKE ?)", keyword, keyword);
        }

        queryWrapper.orderBy("sort_order DESC, sales_volume DESC, create_time DESC")
                .limit(offset, limit);

        List<Product> productList = productMapper.selectListByQuery(queryWrapper);

        // 查询总数
        QueryWrapper countWrapper = QueryWrapper.create()
                .from(Product.class)
                .where("is_deleted = 0");

        if (request.getMerchantId() != null) {
            countWrapper.and("merchant_id = ?", request.getMerchantId());
        }

        if (request.getCategoryId() != null) {
            countWrapper.and("category_id = ?", request.getCategoryId());
        }

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = "%" + request.getKeyword().trim() + "%";
            countWrapper.and("(name LIKE ? OR description LIKE ?)", keyword, keyword);
        }

        long total = productMapper.selectCountByQuery(countWrapper);
        int totalPages = (int) Math.ceil((double) total / request.getPageSize());

        // 构建响应
        ProductListResponse response = new ProductListResponse();
        response.setProductList(productList);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setTotal(total);
        response.setTotalPages(totalPages);

        return response;
    }
}
