package com.xingchen.backend.util.validator;

import cn.hutool.crypto.SecureUtil;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String rawPassword) {
        return SecureUtil.md5(rawPassword);
    }
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean validatePassword(String rawPassword, String encodedPassword) {
        return SecureUtil.md5(rawPassword).equals(encodedPassword);
    }
    
    /**
     * 检查密码强?     * @param password 密码
     * @return 密码强度?-弱，2-中，3-强）
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 1;
        }
        
        int strength = 1;
        
        // 包含数字
        if (password.matches(".*\\d.*")) {
            strength++;
        }
        
        // 包含字母
        if (password.matches(".*[a-zA-Z].*")) {
            strength++;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*(),.?<>].*")) {
            strength++;
        }
        
        // 长度大于8
        if (password.length() > 8) {
            strength++;
        }
        
        return Math.min(strength, 3);
    }
}
