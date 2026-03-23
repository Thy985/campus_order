package com.xingchen.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Address;
import com.xingchen.backend.service.AddressService;
import com.xingchen.backend.dto.request.address.AddressRequest;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/address")
@Tag(name = "Address Management", description = "User address management endpoints")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @SaCheckLogin
    @PostMapping("/create")
    @Operation(summary = "Create address", description = "Create a new shipping address")
    public Result<Address> createAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Address address = addressService.createAddress(request, userId);
        return Result.success("Created successfully", address);
    }

    @SaCheckLogin
    @PutMapping("/update/{addressId}")
    @Operation(summary = "Update address", description = "Update specified shipping address")
    public Result<Address> updateAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Address address = addressService.updateAddress(addressId, request, userId);
        return Result.success("Updated successfully", address);
    }

    @SaCheckLogin
    @DeleteMapping("/delete/{addressId}")
    @Operation(summary = "Delete address", description = "Delete specified shipping address")
    public Result<Void> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressService.deleteAddress(addressId, userId);
        return Result.success("Deleted successfully", null);
    }

    @SaCheckLogin
    @GetMapping("/list")
    @Operation(summary = "Get address list", description = "Get all shipping addresses for current user")
    public Result<List<Address>> getUserAddresses() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Address> addresses = addressService.getUserAddresses(userId);
        return Result.success("Query successful", addresses);
    }

    @SaCheckLogin
    @GetMapping("/detail/{addressId}")
    @Operation(summary = "Get address detail", description = "Get specified shipping address details")
    public Result<Address> getAddressDetail(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Address address = addressService.getAddressDetail(addressId, userId);
        return Result.success("Query successful", address);
    }

    @SaCheckLogin
    @GetMapping("/{addressId}")
    @Operation(summary = "Get address detail", description = "Get specified shipping address details (compatible path)")
    public Result<Address> getAddressDetailCompat(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        return getAddressDetail(addressId);
    }

    @SaCheckLogin
    @PostMapping("/set-default/{addressId}")
    @Operation(summary = "Set default address", description = "Set specified address as default shipping address")
    public Result<Void> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressService.setDefaultAddress(addressId, userId);
        return Result.success("Set successfully", null);
    }

    @SaCheckLogin
    @GetMapping("/default")
    @Operation(summary = "Get default address", description = "Get current user's default shipping address")
    public Result<Address> getDefaultAddress() {
        Long userId = StpUtil.getLoginIdAsLong();
        Address address = addressService.getDefaultAddress(userId);
        return Result.success("Query successful", address);
    }
}
