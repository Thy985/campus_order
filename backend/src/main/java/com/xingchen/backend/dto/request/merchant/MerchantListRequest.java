package com.xingchen.backend.dto.request.merchant;

import lombok.Data;

@Data
public class MerchantListRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 排序字段
     */
    private String sortField = "sort_order";

    /**
     * 排序方向
     */
    private String sortDirection = "desc";
}
