package com.xingchen.backend.service;

import java.util.List;
import java.util.Map;

/**
 * 商家统计服务接口
 */
public interface MerchantStatisticsService {

    /**
     * 获取商家统计数据
     */
    Map<String, Object> getStatistics(Long merchantId);

    /**
     * 获取营收趋势
     */
    List<Map<String, Object>> getRevenueTrend(Long merchantId, String range);
}
