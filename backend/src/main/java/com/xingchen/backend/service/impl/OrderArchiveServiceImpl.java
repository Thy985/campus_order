package com.xingchen.backend.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.dto.response.admin.ArchiveStatistics;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.service.OrderArchiveService;
import com.xingchen.backend.util.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单归档服务实现类 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderArchiveServiceImpl implements OrderArchiveService {

    private final OrderMapper orderMapper;

    private static final int DEFAULT_ARCHIVE_DAYS = 90;
    private static final int DEFAULT_CLEANUP_DAYS = 180;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveCompletedOrders(LocalDate beforeDate) {
        log.info("开始归档{}之前已完成订单", beforeDate);
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("status = " + Constants.OrderStatus.COMPLETED)
                .and("DATE(create_time) <= ?", beforeDate.toString())
                .and("is_deleted = 0")
                .and("(is_archived = 0 OR is_archived IS NULL)");
        
        List<Order> ordersToArchive = orderMapper.selectListByQuery(queryWrapper);
        
        if (ordersToArchive.isEmpty()) {
            log.info("没有需要归档的已完成订单");
            return 0;
        }
        
        int archivedCount = 0;
        LocalDateTime archiveTime = LocalDateTime.now();
        
        for (Order order : ordersToArchive) {
            try {
                order.setIsArchived(1);
                order.setArchiveTime(archiveTime);
                orderMapper.update(order);
                archivedCount++;
            } catch (Exception e) {
                log.error("归档订单失败, orderId: {}, error: {}", order.getId(), e.getMessage());
            }
        }
        
        log.info("成功归档{}个已完成订单", archivedCount);
        return archivedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveCancelledOrders(LocalDate beforeDate) {
        log.info("开始归档{}之前已取消订单", beforeDate);
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("status = " + Constants.OrderStatus.CANCELLED)
                .and("DATE(create_time) <= ?", beforeDate.toString())
                .and("is_deleted = 0")
                .and("(is_archived = 0 OR is_archived IS NULL)");
        
        List<Order> ordersToArchive = orderMapper.selectListByQuery(queryWrapper);
        
        if (ordersToArchive.isEmpty()) {
            log.info("没有需要归档的已取消订单");
            return 0;
        }
        
        int archivedCount = 0;
        LocalDateTime archiveTime = LocalDateTime.now();
        
        for (Order order : ordersToArchive) {
            try {
                order.setIsArchived(1);
                order.setArchiveTime(archiveTime);
                orderMapper.update(order);
                archivedCount++;
            } catch (Exception e) {
                log.error("归档订单失败, orderId: {}, error: {}", order.getId(), e.getMessage());
            }
        }
        
        log.info("成功归档{}个已取消订单", archivedCount);
        return archivedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveRefusedOrders(LocalDate beforeDate) {
        log.info("开始归档{}之前已拒绝订单", beforeDate);
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("status = " + Constants.OrderStatus.REFUSED)
                .and("DATE(create_time) <= ?", beforeDate.toString())
                .and("is_deleted = 0")
                .and("(is_archived = 0 OR is_archived IS NULL)");
        
        List<Order> ordersToArchive = orderMapper.selectListByQuery(queryWrapper);
        
        if (ordersToArchive.isEmpty()) {
            log.info("没有需要归档的已拒绝订单");
            return 0;
        }
        
        int archivedCount = 0;
        LocalDateTime archiveTime = LocalDateTime.now();
        
        for (Order order : ordersToArchive) {
            try {
                order.setIsArchived(1);
                order.setArchiveTime(archiveTime);
                orderMapper.update(order);
                archivedCount++;
            } catch (Exception e) {
                log.error("归档订单失败, orderId: {}, error: {}", order.getId(), e.getMessage());
            }
        }
        
        log.info("成功归档{}个已拒绝订单", archivedCount);
        return archivedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveOldOrders() {
        LocalDate beforeDate = LocalDate.now().minusDays(DEFAULT_ARCHIVE_DAYS);
        log.info("开始归档{}天前的旧订单", DEFAULT_ARCHIVE_DAYS);
        
        int completedCount = archiveCompletedOrders(beforeDate);
        int cancelledCount = archiveCancelledOrders(beforeDate);
        int refusedCount = archiveRefusedOrders(beforeDate);
        
        log.info("归档旧订单完成: 已完成={}, 已取消={}, 已拒绝={}", 
            completedCount, cancelledCount, refusedCount);
        return completedCount + cancelledCount + refusedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredArchives() {
        LocalDate beforeDate = LocalDate.now().minusDays(DEFAULT_CLEANUP_DAYS);
        log.info("开始清理{}天前的过期归档数据", DEFAULT_CLEANUP_DAYS);
        return cleanupArchivedData(beforeDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupArchivedData(LocalDate beforeDate) {
        log.info("开始清理{}之前已归档数据", beforeDate);
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Order.class)
                .where("is_archived = 1")
                .and("DATE(archive_time) <= ?", beforeDate.toString())
                .and("is_deleted = 0");
        
        List<Order> ordersToCleanup = orderMapper.selectListByQuery(queryWrapper);
        
        if (ordersToCleanup.isEmpty()) {
            log.info("没有需要清理的已归档数据");
            return 0;
        }
        
        int cleanedCount = 0;
        
        for (Order order : ordersToCleanup) {
            try {
                order.setIsDeleted(1);
                orderMapper.update(order);
                cleanedCount++;
            } catch (Exception e) {
                log.error("清理归档订单失败, orderId: {}, error: {}", order.getId(), e.getMessage());
            }
        }
        
        log.info("成功清理{}个已归档订单", cleanedCount);
        return cleanedCount;
    }

    @Override
    public ArchiveStatistics getArchiveStatistics() {
        ArchiveStatistics statistics = new ArchiveStatistics();
        
        long totalOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create().from(Order.class).where("is_deleted = 0")
        );
        statistics.setTotalOrders(totalOrders);
        
        long archivedOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(Order.class)
                        .where("is_archived = 1")
                        .and("is_deleted = 0")
        );
        statistics.setArchivedOrders(archivedOrders);
        
        long pendingArchiveOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(Order.class)
                        .where("status IN (" + Constants.OrderStatus.COMPLETED + ", " + Constants.OrderStatus.CANCELLED + ")")
                        .and("(is_archived = 0 OR is_archived IS NULL)")
                        .and("is_deleted = 0")
        );
        statistics.setPendingArchiveOrders(pendingArchiveOrders);
        
        QueryWrapper lastArchiveQuery = QueryWrapper.create()
                .select("MAX(archive_time) as last_archive_time")
                .from(Order.class)
                .where("is_archived = 1")
                .and("is_deleted = 0");
        
        List<Order> lastArchivedOrders = orderMapper.selectListByQuery(lastArchiveQuery);
        if (!lastArchivedOrders.isEmpty() && lastArchivedOrders.get(0).getArchiveTime() != null) {
            LocalDateTime lastArchiveTime = lastArchivedOrders.get(0).getArchiveTime();
            statistics.setLastArchiveDate(lastArchiveTime.toLocalDate());
        }
        
        return statistics;
    }
}
