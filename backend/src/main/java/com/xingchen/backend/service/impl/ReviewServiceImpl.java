package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.Review;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.mapper.ReviewMapper;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.service.ReviewService;
import com.xingchen.backend.dto.request.review.CreateReviewRequest;
import com.xingchen.backend.dto.request.review.ReplyReviewRequest;
import com.xingchen.backend.dto.response.review.ReviewListResponse;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Review createReview(CreateReviewRequest request, Long userId) {
        // 查询订单
        Order order = orderMapper.selectOneById(request.getOrderId());
        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权评价此订单");
        }

        // 验证订单状态（已完成的订单才能评价）
        if (order.getStatus() != Constants.OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单未完成，无法评价");
        }

        // 检查是否已评价
        if (hasReviewed(request.getOrderId())) {
            throw new BusinessException(ErrorCode.REVIEW_EXIST, "订单已评价");
        }

        // 创建评价
        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        // 处理图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            review.setImages(String.join(",", request.getImages()));
        }

        review.setIsShow(1);
        review.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        reviewMapper.insert(review);

        // 更新商家评分
        updateMerchantRating(order.getMerchantId());

        log.info("评价创建成功: reviewId={}, orderId={}, userId={}, rating={}",
                review.getId(), request.getOrderId(), userId, request.getRating());

        return review;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(ReplyReviewRequest request, Long merchantId) {
        // 查询评价
        Review review = reviewMapper.selectOneById(request.getReviewId());
        if (review == null || review.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_EXIST, "评价不存在");
        }

        // 验证商家权限
        if (!review.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_MATCH, "无权回复此评价");
        }

        // 更新回复
        review.setMerchantReply(request.getReply());
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        reviewMapper.update(review);

        log.info("评价回复成功: reviewId={}, merchantId={}", request.getReviewId(), merchantId);
    }

    @Override
    public ReviewListResponse getMerchantReviews(Long merchantId, int page, int size) {
        int limit = size;
        int offset = (page - 1) * size;

        // 查询评价列表
        List<Review> reviewList = reviewMapper.selectByMerchantId(merchantId, limit, offset);

        // 查询总数
        long total = reviewMapper.countByMerchantId(merchantId);

        // 查询平均评分
        Double averageRating = reviewMapper.calculateAverageRating(merchantId);

        // 构建响应
        ReviewListResponse response = new ReviewListResponse();
        response.setReviewList(reviewList);
        response.setPage(page);
        response.setPageSize(size);
        response.setTotal(total);
        response.setTotalPages((int) Math.ceil((double) total / size));
        response.setAverageRating(averageRating != null ? averageRating : 5.0);
        response.setTotalCount(total);

        return response;
    }

    @Override
    public Review getReviewByOrderId(Long orderId) {
        return reviewMapper.selectByOrderId(orderId);
    }

    @Override
    public boolean hasReviewed(Long orderId) {
        return reviewMapper.selectByOrderId(orderId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantRating(Long merchantId) {
        Double averageRating = reviewMapper.calculateAverageRating(merchantId);
        if (averageRating == null) {
            averageRating = 5.0;
        }

        // 更新商家评分
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        if (merchant != null) {
            merchant.setRating(BigDecimal.valueOf(averageRating));
            merchant.setUpdateTime(LocalDateTime.now());
            merchantMapper.update(merchant);

            log.info("商家评分更新: merchantId={}, rating={}", merchantId, averageRating);
        }
    }
}
