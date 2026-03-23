package com.xingchen.backend.dto.request.user;

import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
public class VerifyCodeRequest {
    
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
