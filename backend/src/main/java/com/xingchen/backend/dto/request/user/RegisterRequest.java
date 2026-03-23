package com.xingchen.backend.dto.request.user;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {

    /**
     * 邮箱（与手机号二选一）
     */
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（与邮箱二选一）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    private String nickname;

    /**
     * 获取验证目标（手机号或邮箱）
     * @return 手机号或邮箱
     */
    public String getVerifyTarget() {
        if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return null;
    }
}
