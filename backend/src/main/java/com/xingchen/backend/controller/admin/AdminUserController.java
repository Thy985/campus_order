package com.xingchen.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.service.AdminService;
import com.xingchen.backend.dto.response.admin.UserListResponse;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "Admin user management endpoints")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping
    @Operation(summary = "Get user list", description = "Get all users")
    public Result<UserListResponse> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        UserListResponse result = adminService.getUserList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Enable or disable user")
    public Result<Void> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam(value = "status") Integer status
    ) {
        log.info("Admin {} update user {} status to {}", StpUtil.getLoginId(), userId, status);

        boolean success = adminService.updateUserStatus(userId, status);

        if (success) {
            return Result.success("Operation successful", null);
        } else {
            return Result.error("Operation failed");
        }
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/{userId}")
    @Operation(summary = "Get user detail", description = "Get specified user details")
    public Result<User> getUserDetail(
            @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        User user = adminService.getUserDetail(userId);

        if (user == null) {
            return Result.error("User does not exist");
        }

        return Result.success(user);
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete specified user (soft delete)")
    public Result<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        log.info("Admin {} delete user {}", StpUtil.getLoginId(), userId);

        boolean success = adminService.deleteUser(userId);

        if (success) {
            return Result.success("Delete successful", null);
        } else {
            return Result.error("Delete failed");
        }
    }
}
