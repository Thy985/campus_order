package com.xingchen.backend.dto.response.merchant;

import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.MerchantCategory;
import lombok.Data;

@Data
public class MerchantDetailResponse {
    
    /**
     * 商家信息
     */
    private Merchant merchant;
    
    /**
     * 商家分类信息
     */
    private MerchantCategory category;
}
