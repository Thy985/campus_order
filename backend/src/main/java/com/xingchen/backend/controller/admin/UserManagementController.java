package com.xingchen.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xingchen.backend.config.security.StpInterfaceImpl;
import com.xingchen.backend.dto.response.admin.UserListResponse;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.service.AdminService;
import com.xingchen.backend.service.UserService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 *
 * <p>提供管理员对用户的管理功能，包括：</p>
 * <ul>
 *   <li>用户列表查询 - 分页查询所有用户</li>
 *   <li>用户状态管理 - 启用/禁用用户账号</li>
 *   <li>用户类型管理 - 修改用户角色（普通用户/商户/管理员）</li>
 *   <li>用户详情查看 - 查看指定用户信息</li>
 * </ul>
 *
 * <p>权限控制：</p>
 * <ul>
 *   <li>仅管理员角色可访问（@SaCheckRole("admin")）</li>
 *   <li>修改用户类型后自动清除权限缓存</li>
 * </ul>
 *
 * @author xingchen
 * @since 1.0.0
 * @see AdminService
 * @see UserService
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin user management endpoints")
@SaCheckRole("admin")
public class UserManagementController {

    private final UserService userService;
    private final AdminService adminService;
    private final StpInterfaceImpl stpInterface;

    /**
     * 获取用户列表
     *
     * <p>支持分页、关键词搜索和状态筛选</p>
     *
     * @param page     页码（默认1）
     * @param pageSize 每页数量（默认20）
     * @param keyword  搜索关键词（用户名/昵称/手机号/邮箱，可选）
     * @param status   用户状态筛选（0-禁用，1-启用，可选）
     * @return 用户列表（包含分页信息）
     */
    @GetMapping("/list")
    @Operation(summary = "Get user list", description = "Paginated user list query")
    public Result<UserListResponse> getUserList(
            @Parameter(description = "Page number") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "Keyword") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Status") @RequestParam(value = "status", required = false) Integer status) {

        UserListResponse result = adminService.getUserList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 更新用户状态
     *
     * <p>启用或禁用用户账号，禁用后用户无法登录</p>
     *
     * @param userId 用户ID
     * @param status 状态（0-禁用，1-启用）
     * @return 操作结果
     * @throws BusinessException 用户不存在
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Enable or disable user")
    public Result<Void> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Status: 0-disabled, 1-enabled") @RequestParam Integer status) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User does not exist"));

        user.setStatus(status);
        userService.update(user);

        log.info("Admin updated user status: userId={}, status={}", userId, status);
        return Result.success(status == Constants.UserStatus.ENABLED ? "User enabled" : "User disabled");
    }

    /**
     * 更新用户类型
     *
     * <p>修改用户角色类型，修改后自动清除用户权限缓存</p>
     *
     * @param userId   用户ID
     * @param userType 用户类型（0-普通用户，1-商户，2-管理员）
     * @return 操作结果
     * @throws BusinessException 用户不存在
     */
    @PutMapping("/{userId}/type")
    @Operation(summary = "Update user type", description = "Change user role type")
    public Result<Void> updateUserType(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "User type: 0-normal, 1-merchant, 2-admin") @RequestParam Integer userType) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User does not exist"));

        user.setUserType(userType);
        userService.update(user);

        // 清除用户权限缓存，使新权限立即生效
        stpInterface.invalidateUserCache(userId);
        log.info("Admin updated user type: userId={}, userType={}", userId, userType);
        return Result.success("User type updated");
    }

    /**
     * 获取用户详情
     *
     * <p>查询指定用户的详细信息</p>
     *
     * @param userId 用户ID
     * @return 用户详情
     * @throws BusinessException 用户不存在
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user detail", description = "Get user detail by ID")
    public Result<User> getUserDetail(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User does not exist"));

        return Result.success(user);
    }
}
