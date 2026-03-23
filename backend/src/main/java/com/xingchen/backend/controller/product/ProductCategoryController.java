package com.xingchen.backend.controller.product;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xingchen.backend.service.ProductCategoryService;
import com.xingchen.backend.entity.ProductCategory;
import com.xingchen.backend.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/list")
    public Result<List<ProductCategory>> getProductCategoryList() {
        List<ProductCategory> categoryList = productCategoryService.getProductCategoryList();
        return Result.success(categoryList);
    }

    @GetMapping("/tree")
    public Result<List<ProductCategory>> getCategoryTree() {
        List<ProductCategory> tree = productCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    @GetMapping("/sub")
    public Result<List<ProductCategory>> getSubCategories(@RequestParam Integer parentId) {
        if (parentId == null) {
            return Result.badRequest("父分类ID不能为空");
        }

        List<ProductCategory> subCategories = productCategoryService.getSubCategories(parentId);
        return Result.success(subCategories);
    }

    @GetMapping("/{id}")
    public Result<ProductCategory> getCategoryById(@PathVariable Integer id) {
        ProductCategory category = productCategoryService.getProductCategoryById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }

    @SaCheckLogin
    @PostMapping
    public Result<ProductCategory> createProductCategory(@RequestBody ProductCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.badRequest("分类名称不能为空");
        }

        if (category.getParentId() == null) {
            category.setParentId(0);
        }

        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        ProductCategory created = productCategoryService.createProductCategory(category);
        return Result.success(created);
    }

    @SaCheckLogin
    @PutMapping("/{id}")
    public Result<ProductCategory> updateProductCategory(@PathVariable Integer id, @RequestBody ProductCategory category) {
        ProductCategory existing = productCategoryService.getProductCategoryById(id);
        if (existing == null) {
            return Result.notFound("分类不存在");
        }

        category.setId(id);
        ProductCategory updated = productCategoryService.updateProductCategory(category);
        return Result.success(updated);
    }

    @SaCheckLogin
    @DeleteMapping("/{id}")
    public Result<Void> deleteProductCategory(@PathVariable Integer id) {
        ProductCategory existing = productCategoryService.getProductCategoryById(id);
        if (existing == null) {
            return Result.notFound("分类不存在");
        }

        List<ProductCategory> subCategories = productCategoryService.getSubCategories(id);
        if (!subCategories.isEmpty()) {
            return Result.badRequest("该分类下存在子分类，无法删除");
        }

        productCategoryService.deleteProductCategory(id);
        return Result.success("分类删除成功");
    }

    @SaCheckLogin
    @PutMapping("/{id}/status")
    public Result<Void> updateCategoryStatus(@PathVariable Integer id, @RequestParam Integer status) {
        ProductCategory existing = productCategoryService.getProductCategoryById(id);
        if (existing == null) {
            return Result.notFound("分类不存在");
        }

        existing.setStatus(status);
        productCategoryService.updateProductCategory(existing);
        return Result.success("分类状态更新成功");
    }

    @SaCheckLogin
    @PutMapping("/{id}/sort")
    public Result<Void> updateCategorySort(@PathVariable Integer id, @RequestParam Integer sortOrder) {
        ProductCategory existing = productCategoryService.getProductCategoryById(id);
        if (existing == null) {
            return Result.notFound("分类不存在");
        }

        existing.setSortOrder(sortOrder);
        productCategoryService.updateProductCategory(existing);
        return Result.success("分类排序更新成功");
    }

    @SaCheckLogin
    @PutMapping("/batch/sort")
    public Result<Void> batchUpdateSort(@RequestBody List<ProductCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return Result.badRequest("分类列表不能为空");
        }

        for (ProductCategory category : categories) {
            if (category.getId() != null && category.getSortOrder() != null) {
                ProductCategory existing = productCategoryService.getProductCategoryById(category.getId());
                if (existing != null) {
                    existing.setSortOrder(category.getSortOrder());
                    productCategoryService.updateProductCategory(existing);
                }
            }
        }

        return Result.success("批量排序更新成功");
    }

    @GetMapping("/root")
    public Result<List<ProductCategory>> getRootCategories() {
        List<ProductCategory> rootCategories = productCategoryService.getSubCategories(0);
        return Result.success(rootCategories);
    }

    @GetMapping("/{id}/path")
    public Result<List<ProductCategory>> getCategoryPath(@PathVariable Integer id) {
        ProductCategory category = productCategoryService.getProductCategoryById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }

        List<ProductCategory> path = new java.util.ArrayList<>();
        path.add(category);

        Integer parentId = category.getParentId();
        while (parentId != null && parentId > 0) {
            ProductCategory parent = productCategoryService.getProductCategoryById(parentId);
            if (parent != null) {
                path.add(0, parent);
                parentId = parent.getParentId();
            } else {
                break;
            }
        }

        return Result.success(path);
    }
}
