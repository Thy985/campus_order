package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.BusinessHours;
import com.xingchen.backend.mapper.BusinessHoursMapper;
import com.xingchen.backend.service.BusinessHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessHoursServiceImpl implements BusinessHoursService {
    
    private final BusinessHoursMapper businessHoursMapper;
    
    @Override
    public List<BusinessHours> getBusinessHoursByMerchantId(Long merchantId) {
        return businessHoursMapper.selectByMerchantId(merchantId);
    }
    
    @Override
    public void setBusinessHours(Long merchantId, List<BusinessHours> businessHoursList) {
        // 先删除原有营业时间
        List<BusinessHours> existingHours = businessHoursMapper.selectByMerchantId(merchantId);
        for (BusinessHours hours : existingHours) {
            businessHoursMapper.deleteById(hours.getId());
        }
        
        // 再插入新的营业时间
        for (BusinessHours hours : businessHoursList) {
            hours.setMerchantId(merchantId);
            hours.setStatus(1);
            hours.setIsDeleted(0);
            businessHoursMapper.insert(hours);
        }
    }
    
    @Override
    public BusinessHours getBusinessHoursById(Long id) {
        return businessHoursMapper.selectOneById(id);
    }
    
    @Override
    public BusinessHours createBusinessHours(BusinessHours businessHours) {
        // 设置默认值
        if (businessHours.getStatus() == null) {
            businessHours.setStatus(1);
        }
        if (businessHours.getIsDeleted() == null) {
            businessHours.setIsDeleted(0);
        }
        
        // 保存营业时间
        businessHoursMapper.insert(businessHours);
        return businessHours;
    }
    
    @Override
    public BusinessHours updateBusinessHours(BusinessHours businessHours) {
        businessHoursMapper.update(businessHours);
        return businessHours;
    }
    
    @Override
    public void deleteBusinessHours(Long id) {
        businessHoursMapper.deleteById(id);
    }
}
