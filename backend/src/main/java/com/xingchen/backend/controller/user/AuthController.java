package com.xingchen.backend.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import com.xingchen.backend.dto.request.user.ForgotPasswordRequest;
import com.xingchen.backend.dto.request.user.LoginRequest;
import com.xingchen.backend.dto.request.user.RegisterRequest;
import com.xingchen.backend.dto.request.user.VerifyCodeRequest;
import com.xingchen.backend.dto.response.user.LoginResponse;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.service.UserService;
import com.xingchen.backend.service.notification.EmailService;
import com.xingchen.backend.service.sms.SmsService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证控制器
 *
 * <p>提供用户认证相关的REST API接口，包括：</p>
 * <ul>
 *   <li>用户注册 - 支持手机号或邮箱注册</li>
 *   <li>用户登录 - 支持手机号或邮箱登录</li>
 *   <li>验证码管理 - 发送和验证短信/邮箱验证码</li>
 *   <li>密码管理 - 忘记密码、重置密码</li>
 *   <li>Token管理 - 刷新Token、获取当前用户信息</li>
 * </ul>
 *
 * <p>安全特性：</p>
 * <ul>
 *   <li>验证码有效期5分钟，存储于Redis</li>
 *   <li>密码使用BCrypt加密存储</li>
 *   <li>登录接口限流防护（每IP每分钟20次）</li>
 *   <li>开发环境直接返回验证码便于测试</li>
 * </ul>
 *
 * @author xingchen
 * @since 1.0.0
 * @see UserService
 * @see SmsService
 * @see EmailService
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final SmsService smsService;
    private final Environment environment;

    /**
     * 安全随机数生成器（用于生成验证码）
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 用户注册
     *
     * <p>支持手机号或邮箱注册，流程：</p>
     * <ol>
     *   <li>验证验证码（从Redis获取并校验）</li>
     *   <li>检查手机号/邮箱是否已注册</li>
     *   <li>创建用户并设置默认昵称</li>
     *   <li>自动登录并返回Token</li>
     *   <li>删除已使用的验证码</li>
     * </ol>
     *
     * @param request 注册请求（包含手机号/邮箱、密码、验证码）
     * @return 登录响应（包含Token和用户信息）
     * @throws BusinessException 验证码过期/错误、用户已存在
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 获取验证目标（手机号或邮箱）
        String verifyTarget = request.getVerifyTarget();
        if (verifyTarget == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }

        // 验证验证码
        String codeKey = Constants.RedisKey.VERIFY_CODE + verifyTarget;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.USER_VERIFY_CODE_EXPIRED, "验证码已过期");
        }

        if (!storedCode.equals(request.getVerifyCode())) {
            throw new BusinessException(ErrorCode.USER_VERIFY_CODE_ERROR, "验证码错误");
        }

        // 检查手机号/邮箱是否已注册
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userService.findByPhone(request.getPhone()).isPresent()) {
                throw new BusinessException(ErrorCode.USER_PHONE_EXIST, "手机号已注册");
            }
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userService.findByEmail(request.getEmail()).isPresent()) {
                throw new BusinessException(ErrorCode.USER_EMAIL_EXIST, "邮箱已注册");
            }
        }

        // 创建用户
        User user = new User();
        
        // 设置手机号和邮箱
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail()); // 可能为空
        } else {
            // 邮箱注册时，生成一个临时手机号（以999开头，避免与真实手机号冲突）
            String tempPhone = "999" + System.currentTimeMillis() % 100000000;
            user.setPhone(tempPhone);
            user.setEmail(request.getEmail());
        }
        
        user.setPassword(request.getPassword());

        // 设置默认昵称
        String defaultNickname;
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            defaultNickname = request.getNickname();
        } else if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            defaultNickname = "用户" + request.getPhone().substring(7);
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String emailPrefix = request.getEmail().substring(0, request.getEmail().indexOf('@'));
            defaultNickname = "用户" + emailPrefix;
        } else {
            defaultNickname = "用户" + System.currentTimeMillis() % 10000;
        }
        user.setNickname(defaultNickname);

        // 设置用户名（优先使用手机号，其次邮箱）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setUsername(request.getPhone());
        } else {
            user.setUsername(request.getEmail());
        }

        LocalDateTime now = LocalDateTime.now();
        user.setRegisterTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userService.create(user);

        // 删除验证码
        redisTemplate.delete(codeKey);

        // 自动登录 - 使用用户ID作为登录标识
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginResponse response = new LoginResponse();
        response.setToken(tokenInfo.getTokenValue());
        response.setUser(user);
        response.setExpireTime(86400L);

        return Result.success("注册成功", response);
    }

    /**
     * 用户登录
     *
     * <p>支持手机号或邮箱登录，流程：</p>
     * <ol>
     *   <li>根据手机号或邮箱查找用户</li>
     *   <li>验证密码（BCrypt比对）</li>
     *   <li>检查用户状态（是否被禁用）</li>
     *   <li>生成Sa-Token并返回</li>
     *   <li>更新最后登录时间</li>
     * </ol>
     *
     * @param request 登录请求（包含手机号/邮箱、密码）
     * @return 登录响应（包含Token和用户信息）
     * @throws BusinessException 用户不存在、密码错误、账号被禁用
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user;

        // 支持手机号或邮箱登录
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user = userService.findByPhone(request.getPhone())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在"));
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在"));
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }

        // 验证密码
        if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR, "密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.update(user);

        LoginResponse response = new LoginResponse();
        response.setToken(tokenInfo.getTokenValue());
        response.setUser(user);
        response.setExpireTime(86400L);

        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     *
     * <p>调用Sa-Token的logout方法，使当前Token失效</p>
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>从Sa-Token获取当前登录用户ID，查询并返回用户信息</p>
     *
     * @return 当前登录用户信息
     * @throws BusinessException 用户不存在
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        // 获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在"));
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息（兼容前端 /me 路径）
     *
     * <p>复用 {@link #getCurrentUser()} 方法逻辑</p>
     *
     * @return 当前登录用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUserMe() {
        // 复用 current 接口逻辑
        return getCurrentUser();
    }

    /**
     * 刷新Token
     *
     * <p>重新生成Sa-Token，延长登录有效期</p>
     *
     * @return 新的登录响应（包含新Token和用户信息）
     * @throws BusinessException 用户不存在
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken() {
        // 获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在"));

        // 刷新Token
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginResponse response = new LoginResponse();
        response.setToken(tokenInfo.getTokenValue());
        response.setUser(user);
        response.setExpireTime(86400L);

        return Result.success("Token刷新成功", response);
    }

    /**
     * 获取验证码
     *
     * <p>支持手机号短信验证码和邮箱验证码：</p>
     * <ul>
     *   <li>手机号：调用短信服务发送，开发环境直接返回验证码</li>
     *   <li>邮箱：调用邮件服务发送，开发环境也会返回验证码便于测试</li>
     * </ul>
     *
     * <p>验证码存储于Redis，有效期5分钟</p>
     *
     * @param request 验证码请求（包含手机号或邮箱）
     * @return 发送结果（开发环境包含验证码）
     */
    @PostMapping("/verify-code")
    public Result<Map<String, Object>> getVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String target = request.getEmail();
        String targetType = "邮箱";

        // 判断是手机号还是邮箱
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            target = request.getPhone();
            targetType = "手机号";
        } else if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return Result.error("邮箱或手机号不能同时为空");
        }

        // 使用安全的随机数生成器生成6位验证码
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        // 存储到Redis，有效期5分钟
        String codeKey = Constants.RedisKey.VERIFY_CODE + target;
        redisTemplate.opsForValue().set(codeKey, code, Constants.RedisExpire.VERIFY_CODE, TimeUnit.SECONDS);

        // 判断是否为开发环境
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        // 开发环境或手机号验证码直接返回
        boolean returnCodeDirectly = isDev || request.getPhone() != null;

        // 发送验证码
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            // 发送短信验证码
            smsService.sendVerifyCode(request.getPhone(), code);
            log.info("验证码：{}，手机号：{}，环境：{}", code, request.getPhone(), (isDev ? "开发" : "生产"));
        } else {
            // 发送邮箱验证码
            emailService.sendVerifyCodeEmail(request.getEmail(), code);
            log.info("验证码：{}，邮箱：{}，环境：{}", code, request.getEmail(), (isDev ? "开发" : "生产"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("target", target);
        data.put("targetType", targetType);

        // 开发环境或手机号返回验证码，方便测试
        if (returnCodeDirectly) {
            data.put("verifyCode", code);
            data.put("expireSeconds", Constants.RedisExpire.VERIFY_CODE);
            return Result.success("验证码已发送（开发环境/手机号直接返回）", data);
        }

        return Result.success("验证码已发送", data);
    }

    /**
     * 忘记密码 - 重置密码
     *
     * <p>通过邮箱验证码重置密码，流程：</p>
     * <ol>
     *   <li>验证邮箱验证码</li>
     *   <li>查找对应用户</li>
     *   <li>更新密码（BCrypt加密）</li>
     *   <li>删除已使用的验证码</li>
     * </ol>
     *
     * @param request 重置密码请求（包含邮箱、验证码、新密码）
     * @return 操作结果
     * @throws BusinessException 验证码过期/错误、邮箱未注册
     */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        String verifyCode = request.getVerifyCode();
        String newPassword = request.getNewPassword();

        // 1. 验证验证码
        String codeKey = Constants.RedisKey.VERIFY_CODE + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.USER_VERIFY_CODE_EXPIRED, "验证码已过期");
        }

        if (!storedCode.equals(verifyCode)) {
            throw new BusinessException(ErrorCode.USER_VERIFY_CODE_ERROR, "验证码错误");
        }

        // 2. 查找用户
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "该邮箱未注册"));

        // 3. 更新密码
        user.setPassword(userService.encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userService.update(user);

        // 4. 删除验证码（防止重复使用）
        redisTemplate.delete(codeKey);

        log.info("用户 {} 通过邮箱 {} 重置密码成功", user.getId(), email);

        return Result.success("密码重置成功", null);
    }
}
