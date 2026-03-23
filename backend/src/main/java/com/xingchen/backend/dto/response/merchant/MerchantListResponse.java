package com.xingchen.backend.dto.response.merchant;

import com.xingchen.backend.entity.Merchant;
import lombok.Data;

import java.util.List;

@Data
public class MerchantListResponse {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 商家列表
     */
    private List<Merchant> merchantList;
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页?     */
    private Integer totalPages;
}
