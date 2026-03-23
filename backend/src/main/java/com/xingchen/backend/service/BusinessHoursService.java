package com.xingchen.backend.service;

import com.xingchen.backend.entity.BusinessHours;

import java.util.List;

public interface BusinessHoursService {
    
    /**
     * 获取商家营业时间列表
     */
    List<BusinessHours> getBusinessHoursByMerchantId(Long merchantId);
    
    /**
     * 设置商家营业时间
     */
    void setBusinessHours(Long merchantId, List<BusinessHours> businessHoursList);
    
    /**
     * 根据ID获取营业时间
     */
    BusinessHours getBusinessHoursById(Long id);
    
    /**
     * 创建营业时间
     */
    BusinessHours createBusinessHours(BusinessHours businessHours);
    
    /**
     * 更新营业时间
     */
    BusinessHours updateBusinessHours(BusinessHours businessHours);
    
    /**
     * 删除营业时间
     */
    void deleteBusinessHours(Long id);
}
