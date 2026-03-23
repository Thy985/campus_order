package com.xingchen.backend.util.validator;

import java.util.Random;

public class VerifyCodeUtil {
    
    /**
     * 生成指定长度的数字验证码
     * @param length 验证码长?     * @return 数字验证?     */
    public static String generateNumericCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 生成指定长度的字母数字混合验证码
     * @param length 验证码长?     * @return 字母数字混合验证?     */
    public static String generateMixedCode(int length) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * 验证验证码是否正?     * @param inputCode 用户输入的验证码
     * @param storedCode 存储的验证码
     * @return 是否正确
     */
    public static boolean validateCode(String inputCode, String storedCode) {
        if (inputCode == null || storedCode == null) {
            return false;
        }
        return inputCode.equals(storedCode);
    }
}
