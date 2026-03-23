package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.MerchantCategory;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.mapper.MerchantCategoryMapper;
import com.xingchen.backend.service.MerchantService;
import com.xingchen.backend.dto.request.merchant.MerchantListRequest;
import com.xingchen.backend.dto.response.merchant.MerchantDetailResponse;
import com.xingchen.backend.dto.response.merchant.MerchantListResponse;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {
    
    private final MerchantMapper merchantMapper;
    private final MerchantCategoryMapper merchantCategoryMapper;
    
    @Override
    public MerchantListResponse getMerchantList(MerchantListRequest request) {
        // 计算分页参数
        int limit = request.getPageSize();
        int offset = (request.getPage() - 1) * request.getPageSize();

        // 查询商家列表（支持关键词搜索）
        List<Merchant> merchantList = merchantMapper.selectMerchantList(
                request.getCategoryId(),
                null,
                request.getKeyword(),
                limit,
                offset
        );

        // 查询总记录数（支持关键词搜索）
        QueryWrapper countQuery = QueryWrapper.create()
                .from(Merchant.class)
                .where("is_deleted = 0");

        if (request.getCategoryId() != null) {
            countQuery.and("category_id = ?", request.getCategoryId());
        }

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            countQuery.and("(name LIKE ? OR description LIKE ?)",
                    "%" + request.getKeyword().trim() + "%",
                    "%" + request.getKeyword().trim() + "%");
        }

        long total = merchantMapper.selectCountByQuery(countQuery);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / request.getPageSize());

        // 构建响应
        MerchantListResponse response = new MerchantListResponse();
        response.setTotal(total);
        response.setMerchantList(merchantList);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setTotalPages(totalPages);

        return response;
    }
    
    @Override
    public MerchantDetailResponse getMerchantDetail(Long id) {
        // 查询商家详情
        Merchant merchant = merchantMapper.selectOneById(id);
        if (merchant == null) {
            return null;
        }
        
        // 查询商家分类
        MerchantCategory category = null;
        if (merchant.getCategoryId() != null) {
            category = merchantCategoryMapper.selectOneById(merchant.getCategoryId());
        }
        
        // 构建响应
        MerchantDetailResponse response = new MerchantDetailResponse();
        response.setMerchant(merchant);
        response.setCategory(category);
        
        return response;
    }
    
    @Override
    public List<MerchantCategory> getMerchantCategoryList() {
        return merchantCategoryMapper.selectAll();
    }
    
    @Override
    public Merchant getMerchantById(Long id) {
        return merchantMapper.selectOneById(id);
    }
    
    @Override
    public Merchant createMerchant(Merchant merchant) {
        // 设置默认值
        if (merchant.getStatus() == null) {
            merchant.setStatus(1);
        }
        if (merchant.getIsDeleted() == null) {
            merchant.setIsDeleted(0);
        }
        if (merchant.getSortOrder() == null) {
            merchant.setSortOrder(0);
        }
        if (merchant.getSalesVolume() == null) {
            merchant.setSalesVolume(0);
        }
        
        // 保存商家
        merchantMapper.insert(merchant);
        return merchant;
    }
    
    @Override
    public Merchant updateMerchant(Merchant merchant) {
        merchantMapper.update(merchant);
        return merchant;
    }
    
    @Override
    public void deleteMerchant(Long id) {
        merchantMapper.deleteById(id);
    }
}
