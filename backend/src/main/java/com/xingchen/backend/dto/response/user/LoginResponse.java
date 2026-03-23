package com.xingchen.backend.dto.response.user;

import com.xingchen.backend.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    
    private String token;
    
    private User user;
    
    private Long expireTime;
}
