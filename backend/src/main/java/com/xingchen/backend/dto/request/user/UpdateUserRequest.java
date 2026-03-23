package com.xingchen.backend.dto.request.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    
    private String nickname;
    
    private String avatar;
    
    private Integer gender;
}
