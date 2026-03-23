package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.ProductCategory;
import com.xingchen.backend.mapper.ProductCategoryMapper;
import com.xingchen.backend.service.ProductCategoryService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public List<ProductCategory> getProductCategoryList() {
        return productCategoryMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(ProductCategory.class)
                        .where("is_deleted = 0")
                        .orderBy("sort_order DESC, create_time ASC")
        );
    }

    @Override
    public List<ProductCategory> getCategoryTree() {
        // 暂时返回所有分类，后续可以实现树形结构的查询
        return productCategoryMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(ProductCategory.class)
                        .where("is_deleted = 0")
                        .orderBy("sort_order DESC, create_time ASC")
        );
    }

    @Override
    public List<ProductCategory> getSubCategories(Integer parentId) {
        return productCategoryMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(ProductCategory.class)
                        .where("parent_id = ? AND is_deleted = 0", parentId)
                        .orderBy("sort_order DESC, create_time ASC")
        );
    }

    @Override
    public ProductCategory createProductCategory(ProductCategory category) {
        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getIsDeleted() == null) {
            category.setIsDeleted(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getLevel() == null) {
            category.setLevel(1);
        }
        if (category.getParentId() == null) {
            category.setParentId(0);
        }

        // 保存分类
        productCategoryMapper.insert(category);
        return category;
    }

    @Override
    public ProductCategory updateProductCategory(ProductCategory category) {
        productCategoryMapper.update(category);
        return category;
    }

    @Override
    public void deleteProductCategory(Integer id) {
        productCategoryMapper.deleteById(id);
    }

    @Override
    public ProductCategory getProductCategoryById(Integer id) {
        return productCategoryMapper.selectOneById(id);
    }
}
