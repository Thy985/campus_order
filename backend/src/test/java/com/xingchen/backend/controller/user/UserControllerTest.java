package com.xingchen.backend.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingchen.backend.config.RateLimitConfig;
import com.xingchen.backend.dto.request.user.UpdateUserRequest;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "test-token-12345";
    private static final String TEST_PHONE = "13800138000";

    @BeforeEach
    void setUp() {
        // 模拟Sa-Token登录状态
        mockStatic(StpUtil.class);
        when(StpUtil.getLoginIdAsLong()).thenReturn(TEST_USER_ID);
        when(StpUtil.isLogin()).thenReturn(true);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_PHONE);
        user.setNickname("测试用户");
        user.setPhone(TEST_PHONE);
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setAvatar("http://example.com/avatar.jpg");
        user.setGender(1);
        user.setUserType(1);
        user.setStatus(1);
        user.setRegisterTime(LocalDateTime.now());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        return user;
    }

    /**
     * 测试获取用户资料接口 GET /api/user/profile
     */
    @Test
    void testGetProfile() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.username").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.avatar").value("http://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.data.gender").value(1))
                .andExpect(jsonPath("$.data.password").doesNotExist()); // 密码不应返回

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
    }

    /**
     * 测试获取用户资料接口 - 用户不存在
     */
    @Test
    void testGetProfile_UserNotFound() throws Exception {
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
    }

    /**
     * 测试更新用户资料接口 PUT /api/user/profile
     */
    @Test
    void testUpdateProfile() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("新昵称");
        request.setAvatar("http://example.com/new_avatar.jpg");
        request.setGender(2);

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatar").value("http://example.com/new_avatar.jpg"))
                .andExpect(jsonPath("$.data.gender").value(2))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).update(any(User.class));
    }

    /**
     * 测试更新用户资料接口 - 部分更新
     */
    @Test
    void testUpdateProfile_PartialUpdate() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(user);

        // 只更新昵称
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("只更新昵称");

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("只更新昵称"));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).update(any(User.class));
    }

    /**
     * 测试修改密码接口 PUT /api/user/password
     */
    @Test
    void testUpdatePassword() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.verifyPassword(eq("oldPassword123"), eq("encoded_password"))).thenReturn(true);
        when(userService.encodePassword(eq("newPassword123"))).thenReturn("new_encoded_password");
        when(userService.update(any(User.class))).thenReturn(user);

        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "oldPassword123");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).verifyPassword(eq("oldPassword123"), eq("encoded_password"));
        verify(userService, times(1)).encodePassword(eq("newPassword123"));
        verify(userService, times(1)).update(any(User.class));
    }

    /**
     * 测试修改密码接口 - 原密码错误
     */
    @Test
    void testUpdatePassword_WrongOldPassword() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.verifyPassword(eq("wrongPassword"), eq("encoded_password"))).thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "wrongPassword");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("原密码错误"));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).verifyPassword(eq("wrongPassword"), eq("encoded_password"));
        verify(userService, never()).update(any(User.class));
    }

    /**
     * 测试修改密码接口 - 缺少参数
     */
    @Test
    void testUpdatePassword_MissingParams() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "oldPassword123");
        // 缺少newPassword

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("原密码和新密码不能为空"));

        verify(userService, never()).findById(any());
    }

    /**
     * 测试修改手机号接口 PUT /api/user/phone
     */
    @Test
    void testUpdatePhone() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.findByPhone(eq("13900139000"))).thenReturn(Optional.empty());
        when(userService.update(any(User.class))).thenReturn(user);

        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:13900139000"))).thenReturn("123456");

        Map<String, String> request = new HashMap<>();
        request.put("phone", "13900139000");
        request.put("verifyCode", "123456");

        mockMvc.perform(put("/api/user/phone")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("手机号修改成功"));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).findByPhone(eq("13900139000"));
        verify(userService, times(1)).update(any(User.class));
        verify(redisTemplate, times(1)).delete(eq("verify_code:13900139000"));
    }

    /**
     * 测试修改手机号接口 - 验证码错误
     */
    @Test
    void testUpdatePhone_WrongVerifyCode() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:13900139000"))).thenReturn("123456");

        Map<String, String> request = new HashMap<>();
        request.put("phone", "13900139000");
        request.put("verifyCode", "999999"); // 错误的验证码

        mockMvc.perform(put("/api/user/phone")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("验证码错误或已过期"));

        verify(userService, never()).findById(any());
    }

    /**
     * 测试修改手机号接口 - 手机号已被使用
     */
    @Test
    void testUpdatePhone_PhoneExists() throws Exception {
        // 模拟Redis操作
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("verify_code:13900139000"))).thenReturn("123456");

        // 手机号已被其他用户使用
        User existingUser = new User();
        existingUser.setId(999L);
        when(userService.findByPhone(eq("13900139000"))).thenReturn(Optional.of(existingUser));

        Map<String, String> request = new HashMap<>();
        request.put("phone", "13900139000");
        request.put("verifyCode", "123456");

        mockMvc.perform(put("/api/user/phone")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该手机号已被使用"));

        verify(userService, times(1)).findByPhone(eq("13900139000"));
        verify(userService, never()).findById(any());
    }

    /**
     * 测试获取用户信息接口 GET /api/user/info
     */
    @Test
    void testGetUserInfo() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/info")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.data.username").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.phone").value(TEST_PHONE))
                .andExpect(jsonPath("$.data.avatar").value("http://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.data.gender").value(1))
                .andExpect(jsonPath("$.data.userType").value(1))
                .andExpect(jsonPath("$.data.status").value(1));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
    }

    /**
     * 测试删除账户接口 DELETE /api/user/account
     */
    @Test
    void testDeleteAccount() throws Exception {
        User user = createTestUser();
        when(userService.findById(eq(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(user);

        mockMvc.perform(delete("/api/user/account")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("账户已删除"));

        verify(userService, times(1)).findById(eq(TEST_USER_ID));
        verify(userService, times(1)).update(any(User.class));
    }
}
