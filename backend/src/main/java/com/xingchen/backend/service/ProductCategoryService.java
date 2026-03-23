package com.xingchen.backend.service;

import com.xingchen.backend.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryService {
    
    /**
     * 获取商品分类列表
     */
    List<ProductCategory> getProductCategoryList();
    
    /**
     * 获取分类树形结构
     */
    List<ProductCategory> getCategoryTree();
    
    /**
     * 根据父ID获取子分?     */
    List<ProductCategory> getSubCategories(Integer parentId);
    
    /**
     * 创建商品分类
     */
    ProductCategory createProductCategory(ProductCategory category);
    
    /**
     * 更新商品分类
     */
    ProductCategory updateProductCategory(ProductCategory category);
    
    /**
     * 删除商品分类
     */
    void deleteProductCategory(Integer id);
    
    /**
     * 根据ID获取分类
     */
    ProductCategory getProductCategoryById(Integer id);
}
