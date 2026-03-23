package com.xingchen.backend.service;

import com.xingchen.backend.entity.Review;
import com.xingchen.backend.dto.request.review.CreateReviewRequest;
import com.xingchen.backend.dto.request.review.ReplyReviewRequest;
import com.xingchen.backend.dto.response.review.ReviewListResponse;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    Review createReview(CreateReviewRequest request, Long userId);

    /**
     * 商家回复评价
     */
    void replyReview(ReplyReviewRequest request, Long merchantId);

    /**
     * 获取商家的评价列?     */
    ReviewListResponse getMerchantReviews(Long merchantId, int page, int size);

    /**
     * 获取订单的评?     */
    Review getReviewByOrderId(Long orderId);

    /**
     * 检查订单是否已评价
     */
    boolean hasReviewed(Long orderId);

    /**
     * 更新商家评分
     */
    void updateMerchantRating(Long merchantId);
}
