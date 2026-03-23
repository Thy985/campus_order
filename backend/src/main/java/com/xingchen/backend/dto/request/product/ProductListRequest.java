package com.xingchen.backend.dto.request.product;

import lombok.Data;

@Data
public class ProductListRequest {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "sort_order";

    /**
     * 排序方向
     */
    private String sortDirection = "desc";
}
