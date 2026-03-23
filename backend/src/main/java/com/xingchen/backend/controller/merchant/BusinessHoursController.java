package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xingchen.backend.service.BusinessHoursService;
import com.xingchen.backend.entity.BusinessHours;
import com.xingchen.backend.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/business-hours")
@RequiredArgsConstructor
public class BusinessHoursController {
    
    private final BusinessHoursService businessHoursService;
    
    @GetMapping
    public Result<List<BusinessHours>> getBusinessHours(@RequestParam Long merchantId) {
        if (merchantId == null) {
            return Result.badRequest("商家ID不能为空");
        }
        
        List<BusinessHours> businessHoursList = businessHoursService.getBusinessHoursByMerchantId(merchantId);
        return Result.success(businessHoursList);
    }
    
    @SaCheckLogin
    @PostMapping
    public Result<Void> setBusinessHours(@RequestParam Long merchantId, @RequestBody List<BusinessHours> businessHoursList) {
        if (merchantId == null) {
            return Result.badRequest("商家ID不能为空");
        }
        if (businessHoursList == null || businessHoursList.isEmpty()) {
            return Result.badRequest("营业时间列表不能为空");
        }
        
        for (BusinessHours hours : businessHoursList) {
            if (hours.getDayOfWeek() == null || hours.getDayOfWeek() < 1 || hours.getDayOfWeek() > 7) {
                return Result.badRequest("星期几必须在1-7之间");
            }
            if (hours.getStartTime() == null || hours.getEndTime() == null) {
                return Result.badRequest("营业时间和打烊时间不能为空");
            }
        }
        
        businessHoursService.setBusinessHours(merchantId, businessHoursList);
        return Result.success("营业时间设置成功");
    }
    
    @SaCheckLogin
    @PostMapping("/single")
    public Result<BusinessHours> createBusinessHours(@RequestBody BusinessHours businessHours) {
        if (businessHours.getMerchantId() == null) {
            return Result.badRequest("商家ID不能为空");
        }
        if (businessHours.getDayOfWeek() == null || businessHours.getDayOfWeek() < 1 || businessHours.getDayOfWeek() > 7) {
            return Result.badRequest("星期几必须在1-7之间");
        }
        if (businessHours.getStartTime() == null || businessHours.getEndTime() == null) {
            return Result.badRequest("营业时间和打烊时间不能为空");
        }
        
        BusinessHours created = businessHoursService.createBusinessHours(businessHours);
        return Result.success("营业时间创建成功", created);
    }
    
    @SaCheckLogin
    @PutMapping("/{id}")
    public Result<BusinessHours> updateBusinessHours(@PathVariable Long id, @RequestBody BusinessHours businessHours) {
        BusinessHours existing = businessHoursService.getBusinessHoursById(id);
        if (existing == null) {
            return Result.notFound("营业时间记录不存在");
        }
        
        if (businessHours.getDayOfWeek() != null) {
            if (businessHours.getDayOfWeek() < 1 || businessHours.getDayOfWeek() > 7) {
                return Result.badRequest("星期几必须在1-7之间");
            }
        }
        
        businessHours.setId(id);
        BusinessHours updated = businessHoursService.updateBusinessHours(businessHours);
        return Result.success("营业时间更新成功", updated);
    }
    
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public Result<Void> deleteBusinessHours(@PathVariable Long id) {
        BusinessHours existing = businessHoursService.getBusinessHoursById(id);
        if (existing == null) {
            return Result.notFound("营业时间记录不存在");
        }
        
        businessHoursService.deleteBusinessHours(id);
        return Result.success("营业时间删除成功");
    }
    
    @GetMapping("/check")
    public Result<java.util.Map<String, Object>> checkBusinessStatus(@RequestParam Long merchantId) {
        if (merchantId == null) {
            return Result.badRequest("商家ID不能为空");
        }
        
        List<BusinessHours> businessHoursList = businessHoursService.getBusinessHoursByMerchantId(merchantId);
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        java.time.LocalTime currentTime = now.toLocalTime();
        
        boolean isOpen = false;
        BusinessHours currentDayHours = null;
        
        for (BusinessHours hours : businessHoursList) {
            if (hours.getDayOfWeek() != null && hours.getDayOfWeek() == dayOfWeek) {
                currentDayHours = hours;
                if (hours.getStartTime() != null && hours.getEndTime() != null) {
                    try {
                        java.time.LocalTime openTime = java.time.LocalTime.parse(hours.getStartTime());
                        java.time.LocalTime closeTime = java.time.LocalTime.parse(hours.getEndTime());
                        if (currentTime.isAfter(openTime) && currentTime.isBefore(closeTime)) {
                            isOpen = true;
                        }
                    } catch (Exception e) {
                        // 时间格式错误，忽略
                    }
                }
                break;
            }
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("isOpen", isOpen);
        result.put("currentDayHours", currentDayHours);
        result.put("currentTime", currentTime);
        result.put("dayOfWeek", dayOfWeek);
        
        return Result.success(result);
    }
    
    @GetMapping("/week/{merchantId}")
    public Result<java.util.List<java.util.Map<String, Object>>> getWeekBusinessHours(@PathVariable Long merchantId) {
        List<BusinessHours> businessHoursList = businessHoursService.getBusinessHoursByMerchantId(merchantId);
        
        java.util.Map<Integer, BusinessHours> weekMap = new java.util.HashMap<>();
        for (BusinessHours hours : businessHoursList) {
            if (hours.getDayOfWeek() != null) {
                weekMap.put(hours.getDayOfWeek(), hours);
            }
        }
        
        java.util.List<java.util.Map<String, Object>> weekList = new java.util.ArrayList<>();
        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        
        for (int i = 1; i <= 7; i++) {
            java.util.Map<String, Object> dayInfo = new java.util.HashMap<>();
            dayInfo.put("dayOfWeek", i);
            dayInfo.put("dayName", dayNames[i]);
            
            BusinessHours hours = weekMap.get(i);
            if (hours != null) {
                dayInfo.put("openTime", hours.getStartTime());
                dayInfo.put("closeTime", hours.getEndTime());
                dayInfo.put("isOpen", hours.getStatus() != null && hours.getStatus() == 1);
            } else {
                dayInfo.put("openTime", null);
                dayInfo.put("closeTime", null);
                dayInfo.put("isOpen", false);
            }
            
            weekList.add(dayInfo);
        }
        
        return Result.success(weekList);
    }
}
