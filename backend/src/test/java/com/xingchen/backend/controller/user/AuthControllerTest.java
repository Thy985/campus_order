package com.xingchen.backend.controller.user;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingchen.backend.config.RateLimitConfig;
import com.xingchen.backend.dto.request.user.LoginRequest;
import com.xingchen.backend.dto.request.user.RegisterRequest;
import com.xingchen.backend.dto.request.user.VerifyCodeRequest;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.exception.BusinessException;
import com.xingchen.backend.service.UserService;
import com.xingchen.backend.util.constant.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private Environment environment;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test-token-12345";

    @BeforeEach
    void setUp() {
        // 模拟环境配置
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername(TEST_PHONE);
        user.setNickname("测试用户");
        user.setEmail(TEST_EMAIL);
        user.setPassword("encoded_password");
        user.setUserType(1);
        user.setStatus(1);
        user.setRegisterTime(LocalDateTime.now());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        return user;
    }

    /**
     * 测试发送验证码接口 POST /api/auth/verify-code
     */
    @Test
    void testGetVerifyCode() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码已发送（开发环境）"))
                .andExpect(jsonPath("$.data.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.verifyCode").exists())
                .andExpect(jsonPath("$.data.expireSeconds").value(300));

        verify(redisTemplate.opsForValue(), times(1)).set(
                eq("verify_code:" + TEST_PHONE),
                anyString(),
                eq(300L),
                eq(java.util.concurrent.TimeUnit.SECONDS)
        );
    }

    /**
     * 测试发送验证码接口 - 生产环境
     */
    @Test
    void testGetVerifyCode_Production() throws Exception {
        // 模拟生产环境
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码已发送，请注意查收"))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.verifyCode").doesNotExist()); // 生产环境不返回验证码
    }

    /**
     * 测试发送验证码接口 - 手机号格式错误
     */
    @Test
    void testGetVerifyCode_InvalidPhone() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("12345678901"); // 无效手机号格式

        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试注册接口 POST /api/auth/register
     */
    @Test
    void testRegister() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:" + TEST_PHONE))).thenReturn("123456");

        // 手机号未被注册
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.empty());

        // 模拟创建用户
        User user = createTestUser();
        when(userService.create(any(User.class))).thenReturn(user);

        // 模拟StpUtil登录
        mockStatic(StpUtil.class);
        SaTokenInfo tokenInfo = mock(SaTokenInfo.class);
        when(tokenInfo.getTokenValue()).thenReturn(TEST_TOKEN);
        when(StpUtil.getTokenInfo()).thenReturn(tokenInfo);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setVerifyCode("123456");
        request.setNickname("测试用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.data.user.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.expireTime").value(86400));

        verify(userService, times(1)).findByPhone(eq(TEST_PHONE));
        verify(userService, times(1)).create(any(User.class));
        verify(redisTemplate, times(1)).delete(eq("verify_code:" + TEST_PHONE));
    }

    /**
     * 测试注册接口 - 验证码错误
     */
    @Test
    void testRegister_WrongVerifyCode() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:" + TEST_PHONE))).thenReturn("123456");

        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setVerifyCode("999999"); // 错误的验证码
        request.setNickname("测试用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_VERIFY_CODE_ERROR))
                .andExpect(jsonPath("$.message").value("验证码错误"));

        verify(userService, never()).create(any(User.class));
    }

    /**
     * 测试注册接口 - 验证码过期
     */
    @Test
    void testRegister_VerifyCodeExpired() throws Exception {
        // 模拟Redis操作 - 验证码不存在（已过期）
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:" + TEST_EMAIL))).thenReturn(null);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setVerifyCode("123456");
        request.setNickname("测试用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_VERIFY_CODE_EXPIRED))
                .andExpect(jsonPath("$.message").value("验证码已过期"));

        verify(userService, never()).create(any(User.class));
    }

    /**
     * 测试注册接口 - 手机号已注册
     */
    @Test
    void testRegister_PhoneExists() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:" + TEST_PHONE))).thenReturn("123456");

        // 手机号已注册
        User existingUser = createTestUser();
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.of(existingUser));

        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setVerifyCode("123456");
        request.setNickname("测试用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_PHONE_EXIST))
                .andExpect(jsonPath("$.message").value("手机号已注册"));

        verify(userService, never()).create(any(User.class));
    }

    /**
     * 测试登录接口 POST /api/auth/login
     */
    @Test
    void testLogin() throws Exception {
        User user = createTestUser();
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.of(user));
        when(userService.verifyPassword(eq(TEST_PASSWORD), eq("encoded_password"))).thenReturn(true);

        // 模拟StpUtil登录
        mockStatic(StpUtil.class);
        SaTokenInfo tokenInfo = mock(SaTokenInfo.class);
        when(tokenInfo.getTokenValue()).thenReturn(TEST_TOKEN);
        when(StpUtil.getTokenInfo()).thenReturn(tokenInfo);

        LoginRequest request = new LoginRequest();
        request.setPhone(TEST_PHONE);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.data.user.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.expireTime").value(86400));

        verify(userService, times(1)).findByPhone(eq(TEST_PHONE));
        verify(userService, times(1)).verifyPassword(eq(TEST_PASSWORD), eq("encoded_password"));
    }

    /**
     * 测试登录接口 - 用户不存在
     */
    @Test
    void testLogin_UserNotExist() throws Exception {
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setPhone(TEST_PHONE);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_EXIST))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    /**
     * 测试登录接口 - 密码错误
     */
    @Test
    void testLogin_WrongPassword() throws Exception {
        User user = createTestUser();
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.of(user));
        when(userService.verifyPassword(eq("wrongPassword"), eq("encoded_password"))).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setPhone(TEST_PHONE);
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_PASSWORD_ERROR))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    /**
     * 测试登录接口 - 账号被禁用
     */
    @Test
    void testLogin_UserDisabled() throws Exception {
        User user = createTestUser();
        user.setStatus(0); // 禁用状态
        when(userService.findByPhone(eq(TEST_PHONE))).thenReturn(Optional.of(user));
        when(userService.verifyPassword(eq(TEST_PASSWORD), eq("encoded_password"))).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setPhone(TEST_PHONE);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_DISABLED))
                .andExpect(jsonPath("$.message").value("账号已被禁用"));
    }

    /**
     * 测试登出接口 POST /api/auth/logout
     */
    @Test
    void testLogout() throws Exception {
        mockStatic(StpUtil.class);
        when(StpUtil.isLogin()).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("退出登录成功"));

        verify(StpUtil.class, times(1));
        StpUtil.logout();
    }

    /**
     * 测试刷新Token接口 GET /api/auth/refresh
     */
    @Test
    void testRefreshToken() throws Exception {
        mockStatic(StpUtil.class);
        when(StpUtil.isLogin()).thenReturn(true);

        SaTokenInfo tokenInfo = mock(SaTokenInfo.class);
        when(tokenInfo.getTokenValue()).thenReturn(TEST_TOKEN);
        when(StpUtil.getTokenInfo()).thenReturn(tokenInfo);

        mockMvc.perform(get("/api/auth/refresh")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Token刷新成功"))
                .andExpect(jsonPath("$.data.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.data.expireTime").value(86400));

        verify(StpUtil.class, times(1));
        StpUtil.renewTimeout(24 * 60 * 60);
    }

    /**
     * 测试获取当前用户信息接口 GET /api/auth/me
     */
    @Test
    void testGetCurrentUser() throws Exception {
        mockStatic(StpUtil.class);
        when(StpUtil.isLogin()).thenReturn(true);
        when(StpUtil.getLoginIdAsLong()).thenReturn(1L);

        User user = createTestUser();
        when(userService.findById(eq(1L))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        verify(userService, times(1)).findById(eq(1L));
    }

    /**
     * 测试检查登录状态接口 GET /api/auth/check
     */
    @Test
    void testCheckLogin() throws Exception {
        mockStatic(StpUtil.class);
        when(StpUtil.isLogin()).thenReturn(true);
        when(StpUtil.getLoginIdAsLong()).thenReturn(1L);

        mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isLogin").value(true))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    /**
     * 测试检查登录状态接口 - 未登录
     */
    @Test
    void testCheckLogin_NotLoggedIn() throws Exception {
        mockStatic(StpUtil.class);
        when(StpUtil.isLogin()).thenReturn(false);

        mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isLogin").value(false))
                .andExpect(jsonPath("$.data.userId").doesNotExist());
    }
}
