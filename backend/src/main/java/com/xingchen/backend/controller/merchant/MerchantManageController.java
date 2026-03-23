package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.BusinessHours;
import com.xingchen.backend.service.MerchantService;
import com.xingchen.backend.service.BusinessHoursService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/merchant/manage")
@RequiredArgsConstructor
@Tag(name = "Merchant Management", description = "Merchant store management endpoints")
@SaCheckLogin
@SaCheckRole("merchant")
public class MerchantManageController {

    private final MerchantService merchantService;
    private final BusinessHoursService businessHoursService;

    @GetMapping("/info")
    @Operation(summary = "Get merchant info", description = "Get current logged-in merchant's store information")
    public Result<Merchant> getMerchantInfo() {
        Long merchantId = StpUtil.getLoginIdAsLong();

        Merchant merchant = merchantService.getMerchantById(merchantId);

        if (merchant == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Merchant does not exist");
        }

        return Result.success(merchant);
    }

    @PutMapping("/info")
    @Operation(summary = "Update merchant info", description = "Update current logged-in merchant's store information")
    public Result<Merchant> updateMerchantInfo(@RequestBody Merchant merchantUpdate) {
        Long merchantId = StpUtil.getLoginIdAsLong();

        Merchant merchant = merchantService.getMerchantById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Merchant does not exist");
        }

        // 只允许更新部分字段
        if (merchantUpdate.getName() != null) {
            merchant.setName(merchantUpdate.getName());
        }
        if (merchantUpdate.getDescription() != null) {
            merchant.setDescription(merchantUpdate.getDescription());
        }
        if (merchantUpdate.getLogo() != null) {
            merchant.setLogo(merchantUpdate.getLogo());
        }
        if (merchantUpdate.getPhone() != null) {
            merchant.setPhone(merchantUpdate.getPhone());
        }
        if (merchantUpdate.getAddress() != null) {
            merchant.setAddress(merchantUpdate.getAddress());
        }
        if (merchantUpdate.getNotice() != null) {
            merchant.setNotice(merchantUpdate.getNotice());
        }
        if (merchantUpdate.getMinOrderAmount() != null) {
            merchant.setMinOrderAmount(merchantUpdate.getMinOrderAmount());
        }

        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.updateMerchant(merchant);

        log.info("Merchant info updated: merchantId={}", merchantId);
        return Result.success("Updated successfully", merchant);
    }

    @PutMapping("/status")
    @Operation(summary = "Update merchant status", description = "Open or close the store")
    public Result<Void> updateMerchantStatus(
            @Parameter(description = "Status: 1-open, 0-closed") @RequestParam Integer status) {
        Long merchantId = StpUtil.getLoginIdAsLong();

        Merchant merchant = merchantService.getMerchantById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Merchant does not exist");
        }

        merchant.setStatus(status);
        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.updateMerchant(merchant);

        log.info("Merchant status updated: merchantId={}, status={}", merchantId, status);
        return Result.success(status == 1 ? "Store opened" : "Store closed");
    }

    @GetMapping("/business-hours")
    @Operation(summary = "Get business hours", description = "Get merchant's business hours settings")
    public Result<List<BusinessHours>> getBusinessHours() {
        Long merchantId = StpUtil.getLoginIdAsLong();
        List<BusinessHours> businessHoursList = businessHoursService.getBusinessHoursByMerchantId(merchantId);
        return Result.success(businessHoursList);
    }

    @PutMapping("/business-hours")
    @Operation(summary = "Update business hours", description = "Update merchant's business hours settings")
    public Result<Void> updateBusinessHours(@RequestBody List<BusinessHours> businessHoursList) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        businessHoursService.setBusinessHours(merchantId, businessHoursList);
        log.info("Business hours updated: merchantId={}", merchantId);
        return Result.success("Business hours updated");
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data", description = "Get merchant's dashboard statistics")
    public Result<Map<String, Object>> getDashboard() {
        Long merchantId = StpUtil.getLoginIdAsLong();

        Merchant merchant = merchantService.getMerchantById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Merchant does not exist");
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("merchant", merchant);
        dashboard.put("businessHours", businessHoursService.getBusinessHoursByMerchantId(merchantId));

        return Result.success(dashboard);
    }
}
