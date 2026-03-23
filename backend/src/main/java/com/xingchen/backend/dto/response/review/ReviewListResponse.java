package com.xingchen.backend.dto.response.review;

import com.xingchen.backend.entity.Review;
import lombok.Data;

import java.util.List;

/**
 * 评价列表响应
 */
@Data
public class ReviewListResponse {

    private List<Review> reviewList;

    private Integer page;

    private Integer pageSize;

    private Long total;

    private Integer totalPages;

    private Double averageRating;

    private Long totalCount;
}
