package com.xingchen.backend.dto.request.user;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    
    private String phone;
    
    private String email;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}
