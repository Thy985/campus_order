package com.xingchen.backend.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.service.UserService;
import com.xingchen.backend.dto.request.user.UpdateUserRequest;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    
    @SaCheckLogin
    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(null);
        return Result.success(user);
    }
    
    @SaCheckLogin
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody UpdateUserRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        
        userService.update(user);
        user.setPassword(null);
        return Result.success(user);
    }
    
    @SaCheckLogin
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.badRequest("原密码和新密码不能为空");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!userService.verifyPassword(oldPassword, user.getPassword())) {
            return Result.badRequest("原密码错误");
        }
        
        user.setPassword(userService.encodePassword(newPassword));
        userService.update(user);
        
        return Result.success("密码修改成功");
    }
    
    @SaCheckLogin
    @PutMapping("/phone")
    public Result<Void> updatePhone(@RequestBody Map<String, String> request) {
        String newPhone = request.get("phone");
        String verifyCode = request.get("verifyCode");
        
        if (newPhone == null || verifyCode == null) {
            return Result.badRequest("手机号和验证码不能为空");
        }
        
        String codeKey = "verify_code:" + newPhone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null || !storedCode.equals(verifyCode)) {
            return Result.badRequest("验证码错误或已过期");
        }
        
        if (userService.findByPhone(newPhone).isPresent()) {
            return Result.badRequest("该手机号已被使用");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPhone(newPhone);
        user.setUsername(newPhone);
        userService.update(user);
        
        redisTemplate.delete(codeKey);
        
        return Result.success("手机号修改成功");
    }
    
    @SaCheckLogin
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        result.put("gender", user.getGender());
        result.put("userType", user.getUserType());
        result.put("status", user.getStatus());
        
        return Result.success(result);
    }
    
    @SaCheckLogin
    @DeleteMapping("/account")
    public Result<Void> deleteAccount() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setIsDeleted(1);
        user.setStatus(0);
        userService.update(user);
        
        return Result.success("账户已删除", null);
    }
}