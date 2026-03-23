package com.xingchen.backend.service;

import com.xingchen.backend.entity.User;

import java.util.Optional;

public interface UserService {
    
    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据ID查询用户
     */
    Optional<User> findById(Long id);
    
    /**
     * 创建用户
     */
    User create(User user);
    
    /**
     * 更新用户信息
     */
    User update(User user);
    
    /**
     * 验证密码
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 加密密码
     */
    String encodePassword(String password);

    /**
     * 根据邮箱更新密码
     */
    void updatePasswordByEmail(String email, String newPassword);
}
