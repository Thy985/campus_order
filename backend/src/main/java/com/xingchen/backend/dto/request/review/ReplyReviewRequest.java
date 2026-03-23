package com.xingchen.backend.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回复评价请求
 */
@Data
public class ReplyReviewRequest {

    @NotNull(message = "评价ID不能为空")
    private Long reviewId;

    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
