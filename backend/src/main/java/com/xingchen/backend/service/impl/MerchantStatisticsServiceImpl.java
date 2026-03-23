package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.mapper.OrderItemMapper;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.service.MerchantStatisticsService;
import com.xingchen.backend.service.cache.MerchantCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户统计服务实现类 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantStatisticsServiceImpl implements MerchantStatisticsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantCacheService merchantCacheService;

    @Override
    public Map<String, Object> getStatistics(Long merchantId) {
        // 先尝试从缓存获取
        if (merchantCacheService != null) {
            try {
                Object cached = merchantCacheService.getCachedMerchantStats(merchantId);
                if (cached != null) {
                    return (Map<String, Object>) cached;
                }
            } catch (Exception e) {
                log.warn("获取商家统计缓存失败: {}", e.getMessage());
            }
        }

        Map<String, Object> stats = new HashMap<>();

        try {
            // 今日订单数
            long todayOrders = orderMapper.countTodayOrdersByMerchantId(merchantId);
            stats.put("todayOrders", todayOrders);
        } catch (Exception e) {
            stats.put("todayOrders", 0L);
            log.warn("获取今日订单数失败: {}", e.getMessage());
        }

        try {
            // 今日营收
            BigDecimal todayRevenue = orderMapper.sumTodayAmountByMerchantId(merchantId);
            stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            stats.put("todayRevenue", BigDecimal.ZERO);
            log.warn("获取今日营收失败: {}", e.getMessage());
        }

        try {
            // 累计订单数
            long totalOrders = orderMapper.countByMerchantId(merchantId);
            stats.put("totalOrders", totalOrders);
        } catch (Exception e) {
            stats.put("totalOrders", 0L);
            log.warn("获取累计订单数失败: {}", e.getMessage());
        }

        try {
            // 累计营收
            BigDecimal totalRevenue = orderMapper.sumAmountByMerchantId(merchantId);
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        } catch (Exception e) {
            stats.put("totalRevenue", BigDecimal.ZERO);
            log.warn("获取累计营收失败: {}", e.getMessage());
        }

        try {
            // 评分
            Merchant merchant = merchantMapper.selectOneById(merchantId);
            stats.put("rating", merchant != null && merchant.getRating() != null ? merchant.getRating() : 4.5);
        } catch (Exception e) {
            stats.put("rating", 4.5);
            log.warn("获取商家评分失败: {}", e.getMessage());
        }

        try {
            // 待处理订单数
            long pendingOrders = orderMapper.countPendingOrdersByMerchantId(merchantId);
            stats.put("pendingOrders", pendingOrders);
        } catch (Exception e) {
            stats.put("pendingOrders", 0L);
            log.warn("获取待处理订单数失败: {}", e.getMessage());
        }

        // 本周销售数据
        List<Map<String, Object>> weeklySales = getWeeklySales(merchantId);
        stats.put("weeklySales", weeklySales);

        // 热销菜品
        List<Map<String, Object>> topDishes = getTopDishes(merchantId);
        stats.put("topDishes", topDishes);

        // 缓存结果
        if (merchantCacheService != null) {
            try {
                merchantCacheService.cacheMerchantStats(merchantId, stats);
            } catch (Exception e) {
                log.warn("缓存商家统计失败: {}", e.getMessage());
            }
        }

        return stats;
    }

    /**
     * 获取本周销售数据
     */
    private List<Map<String, Object>> getWeeklySales(Long merchantId) {
        List<Map<String, Object>> weeklySales = new ArrayList<>();
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        try {
            List<Map<String, Object>> dbData = orderMapper.selectWeeklySalesByMerchantId(merchantId);

            Map<String, Object> dayDataMap = new HashMap<>();
            for (Map<String, Object> item : dbData) {
                String day = (String) item.get("day");
                Object amount = item.get("salesAmount");
                dayDataMap.put(day, amount);
            }

            for (String day : days) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("day", day);
                Object amount = dayDataMap.get(day);
                if (amount != null) {
                    dayData.put("salesAmount", amount);
                } else {
                    dayData.put("salesAmount", 0);
                }
                weeklySales.add(dayData);
            }
        } catch (Exception e) {
            log.warn("获取本周销售数据失败: {}", e.getMessage());
            for (String day : days) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("day", day);
                dayData.put("salesAmount", 0);
                weeklySales.add(dayData);
            }
        }

        return weeklySales;
    }

    /**
     * 获取热销菜品
     */
    private List<Map<String, Object>> getTopDishes(Long merchantId) {
        try {
            return orderItemMapper.selectTopDishesByMerchantId(merchantId, 5);
        } catch (Exception e) {
            log.warn("获取热销菜品失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getRevenueTrend(Long merchantId, String range) {
        List<Map<String, Object>> trend = new ArrayList<>();

        try {
            if ("week".equals(range)) {
                // 获取最近7天的数据
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate startDate = today.minusDays(6);
                
                List<Map<String, Object>> dbData = orderMapper.selectRevenueTrendByDay(
                        merchantId, 
                        startDate.toString(), 
                        today.plusDays(1).toString()
                );
                
                // 创建日期到数据的映射
                Map<String, Map<String, Object>> dataMap = new HashMap<>();
                for (Map<String, Object> item : dbData) {
                    String date = item.get("date").toString();
                    dataMap.put(date, item);
                }
                
                // 填充7天的数据（包括没有数据的日期）
                String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                for (int i = 0; i < 7; i++) {
                    java.time.LocalDate date = startDate.plusDays(i);
                    String dateStr = date.toString();
                    String dayName = dayNames[date.getDayOfWeek().getValue() - 1];
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", dayName);
                    item.put("fullDate", dateStr);
                    
                    if (dataMap.containsKey(dateStr)) {
                        Map<String, Object> dbItem = dataMap.get(dateStr);
                        item.put("amount", dbItem.get("revenue"));
                        item.put("orderCount", dbItem.get("orderCount"));
                    } else {
                        item.put("amount", 0);
                        item.put("orderCount", 0);
                    }
                    trend.add(item);
                }
            } else if ("month".equals(range)) {
                // 获取最近30天的数据
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate startDate = today.minusDays(29);
                
                List<Map<String, Object>> dbData = orderMapper.selectRevenueTrendByDay(
                        merchantId, 
                        startDate.toString(), 
                        today.plusDays(1).toString()
                );
                
                // 按周分组
                Map<Integer, BigDecimal> weekRevenue = new HashMap<>();
                Map<Integer, Integer> weekOrderCount = new HashMap<>();
                
                for (Map<String, Object> item : dbData) {
                    String dateStr = item.get("date").toString();
                    java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                    int weekOfMonth = (date.getDayOfMonth() - 1) / 7 + 1;
                    
                    BigDecimal revenue = new BigDecimal(item.get("revenue").toString());
                    int orderCount = Integer.parseInt(item.get("orderCount").toString());
                    
                    weekRevenue.merge(weekOfMonth, revenue, BigDecimal::add);
                    weekOrderCount.merge(weekOfMonth, orderCount, Integer::sum);
                }
                
                // 填充4周的数据
                for (int week = 1; week <= 4; week++) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", "第" + week + "周");
                    item.put("amount", weekRevenue.getOrDefault(week, BigDecimal.ZERO));
                    item.put("orderCount", weekOrderCount.getOrDefault(week, 0));
                    trend.add(item);
                }
            } else if ("quarter".equals(range)) {
                // 获取最近3个月的数据
                List<Map<String, Object>> dbData = orderMapper.selectRevenueTrendByMonth(merchantId, 3);
                
                for (Map<String, Object> dbItem : dbData) {
                    Map<String, Object> item = new HashMap<>();
                    String month = dbItem.get("month").toString();
                    item.put("date", month);
                    item.put("amount", dbItem.get("revenue"));
                    item.put("orderCount", dbItem.get("orderCount"));
                    trend.add(item);
                }
            }
        } catch (Exception e) {
            log.error("获取营收趋势失败: merchantId={}, range={}", merchantId, range, e);
            // 返回空数据而不是抛出异常
        }

        return trend;
    }
}
