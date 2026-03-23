package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xingchen.backend.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类Mapper
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    
    /**
     * 插入分类
     */
    int insert(ProductCategory category);
    
    /**
     * 根据ID查询
     */
    ProductCategory selectById(@Param("id") Long id);
    
    /**
     * 根据商家ID查询分类列表
     */
    List<ProductCategory> selectByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID和分类名称查询
     */
    ProductCategory selectByMerchantIdAndName(@Param("merchantId") Long merchantId, @Param("name") String name);
    
    /**
     * 更新分类
     */
    int update(ProductCategory category);
    
    /**
     * 删除分类
     */
    int deleteById(@Param("id") Long id);
}
