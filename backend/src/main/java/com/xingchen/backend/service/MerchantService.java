package com.xingchen.backend.service;

import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.MerchantCategory;
import com.xingchen.backend.dto.request.merchant.MerchantListRequest;
import com.xingchen.backend.dto.response.merchant.MerchantDetailResponse;
import com.xingchen.backend.dto.response.merchant.MerchantListResponse;

import java.util.List;

public interface MerchantService {
    
    /**
     * 获取商家列表
     */
    MerchantListResponse getMerchantList(MerchantListRequest request);
    
    /**
     * 获取商家详情
     */
    MerchantDetailResponse getMerchantDetail(Long id);
    
    /**
     * 获取商家分类列表
     */
    List<MerchantCategory> getMerchantCategoryList();
    
    /**
     * 根据ID获取商家
     */
    Merchant getMerchantById(Long id);
    
    /**
     * 创建商家
     */
    Merchant createMerchant(Merchant merchant);
    
    /**
     * 更新商家信息
     */
    Merchant updateMerchant(Merchant merchant);
    
    /**
     * 删除商家
     */
    void deleteMerchant(Long id);
}
