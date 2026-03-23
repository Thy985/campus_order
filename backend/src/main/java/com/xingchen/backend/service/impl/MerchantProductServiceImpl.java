package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Product;
import com.xingchen.backend.mapper.ProductMapper;
import com.xingchen.backend.service.MerchantProductService;
import com.xingchen.backend.dto.request.product.CreateProductRequest;
import com.xingchen.backend.dto.request.product.UpdateProductRequest;
import com.xingchen.backend.dto.response.product.ProductListResponse;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductServiceImpl implements MerchantProductService {

    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(CreateProductRequest request, Long merchantId) {
        // 创建商品
        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        if (request.getCategory() != null) {
            try {
                product.setCategoryId(Integer.valueOf(request.getCategory()));
            } catch (NumberFormatException e) {
                log.warn("分类ID格式错误，使用默认值: {}", e.getMessage());
                product.setCategoryId(null);
            }
        }
        product.setStatus(request.getStatus() != null ? request.getStatus() : Constants.ProductStatus.ON_SHELF);
        product.setSalesVolume(0);
        product.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        productMapper.insert(product);

        log.info("创建商品成功: productId={}, merchantId={}, name={}",
                product.getId(), merchantId, request.getName());

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(Long productId, UpdateProductRequest request, Long merchantId) {
        // 查询商品
        Product product = productMapper.selectOneById(productId);
        if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在");
        }

        // 验证商品归属
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_MATCH, "无权操作此商品");
        }

        // 更新商品信息
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImage() != null) {
            product.setImage(request.getImage());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategory() != null) {
            try {
                product.setCategoryId(Integer.valueOf(request.getCategory()));
            } catch (NumberFormatException e) {
                log.warn("分类ID格式错误,使用默认值: {}", e.getMessage());
                product.setCategoryId(null);
            }
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);

        log.info("更新商品成功: productId={}, merchantId={}", productId, merchantId);

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId, Long merchantId) {
        // 查询商品
        Product product = productMapper.selectOneById(productId);
        if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在");
        }

        // 验证商品归属
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_MATCH, "无权操作此商品");
        }

        // 逻辑删除
        product.setIsDeleted(Constants.DeleteFlag.DELETED);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);

        log.info("删除商品成功: productId={}, merchantId={}", productId, merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long productId, Integer status, Long merchantId) {
        // 查询商品
        Product product = productMapper.selectOneById(productId);
        if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在或已删除");
        }

        // 验证商品归属
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_MATCH, "无权操作此商品");
        }

        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);

        String statusText = status == Constants.ProductStatus.ON_SHELF ? "上架" : "下架";
        log.info("商品{}成功: productId={}, merchantId={}", statusText, productId, merchantId);
    }

    @Override
    public ProductListResponse getMerchantProducts(Long merchantId, Integer status, String keyword, int page, int size) {
        int limit = size;
        int offset = (page - 1) * size;

        // 查询商品列表
        List<Product> products = productMapper.selectByMerchantIdWithFilter(
                merchantId, status, keyword, limit, offset);

        // 查询总数
        long total = productMapper.countByMerchantIdWithFilter(merchantId, status, keyword);

        // 构建响应
        ProductListResponse response = new ProductListResponse();
        response.setProductList(products);
        response.setPage(page);
        response.setPageSize(size);
        response.setTotal(total);
        response.setTotalPages((int) Math.ceil((double) total / size));

        return response;
    }

    @Override
    public Product getProductDetail(Long productId, Long merchantId) {
        Product product = productMapper.selectOneById(productId);
        if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在或已删除");
        }

        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_MATCH, "无权查看此商品");
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStock(List<ProductStockUpdate> updates, Long merchantId) {
        for (ProductStockUpdate update : updates) {
            Product product = productMapper.selectOneById(update.getProductId());
            if (product == null || product.getIsDeleted() == Constants.DeleteFlag.DELETED) {
                continue;
            }

            if (!product.getMerchantId().equals(merchantId)) {
                continue;
            }

            product.setStock(update.getStock());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.update(product);
        }

        log.info("批量更新库存成功: merchantId={}, count={}", merchantId, updates.size());
    }
}
