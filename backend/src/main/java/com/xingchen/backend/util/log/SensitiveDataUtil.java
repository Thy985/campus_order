package com.xingchen.backend.util.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 */
public class SensitiveDataUtil {
    
    /**
     * 手机号脱敏
     * 13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 身份证号脱敏
     * 110101199001011234 -> 110101********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
    
    /**
     * 姓名脱敏
     * 张三 -> 张*
     * 张三丰 -> 张*丰
     */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }
    
    /**
     * 邮箱脱敏
     * example@gmail.com -> e***@gmail.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 1) {
            return email;
        }
        
        String maskedLocal = localPart.charAt(0) + "***";
        return maskedLocal + "@" + domainPart;
    }
    
    /**
     * 银行卡号脱敏
     * 6222021234567890123 -> 622202*********0123
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        int length = bankCard.length();
        int maskLength = length - 8;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }
        return bankCard.substring(0, 4) + mask + bankCard.substring(length - 4);
    }
    
    /**
     * 地址脱敏
     * 保留省市区，详细地址脱敏
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }
    
    /**
     * 密码脱敏
     * 统一显示 ******
     */
    public static String maskPassword(String password) {
        return password == null ? null : "******";
    }
    
    /**
     * 通用脱敏处理
     * 根据内容自动识别并脱敏
     */
    public static String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        String result = data;
        
        // 手机号脱敏
        result = maskPattern(result, Pattern.compile("1[3-9]\\d{9}"), SensitiveDataUtil::maskPhone);
        
        // 身份证号脱敏
        result = maskPattern(result, Pattern.compile("\\d{17}[\\dXx]"), SensitiveDataUtil::maskIdCard);
        
        // 邮箱脱敏
        result = maskPattern(result, Pattern.compile("[\\w.-]+@[\\w.-]+\\.\\w+"), SensitiveDataUtil::maskEmail);
        
        return result;
    }
    
    /**
     * 正则匹配并脱敏
     */
    private static String maskPattern(String input, Pattern pattern, java.util.function.Function<String, String> maskFunction) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, maskFunction.apply(matcher.group()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
