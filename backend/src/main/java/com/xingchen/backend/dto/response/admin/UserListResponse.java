package com.xingchen.backend.dto.response.admin;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户列表响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<User> list;
}
