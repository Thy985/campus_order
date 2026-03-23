package com.xingchen.backend.dto.response.admin;

import com.xingchen.backend.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 最近订单 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrdersDTO {
    private List<Order> orders;
}
