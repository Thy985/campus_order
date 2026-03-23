package com.xingchen.backend.service;

import com.xingchen.backend.dto.response.admin.ArchiveStatistics;

import java.time.LocalDate;

/**
 * 订单归档服务
 * 处理订单的历史归档和清理
 */
public interface OrderArchiveService {
    
    /**
     * 归档完成的订单
     * @param beforeDate 截止日期
     * @return 归档的订单数量
     */
    int archiveCompletedOrders(LocalDate beforeDate);
    
    /**
     * 归档已取消的订单
     * @param beforeDate 截止日期
     * @return 归档的订单数量
     */
    int archiveCancelledOrders(LocalDate beforeDate);
    
    /**
     * 归档已拒绝的订单
     * @param beforeDate 截止日期
     * @return 归档的订单数量
     */
    int archiveRefusedOrders(LocalDate beforeDate);
    
    /**
     * 清理已归档的旧数据（按默认日期）
     * @return 归档的订单数量
     */
    int archiveOldOrders();
    
    /**
     * 清理过期的归档数据（按默认日期）
     * @return 清理的数据条数
     */
    int cleanupExpiredArchives();
    
    /**
     * 清理归档数据
     * @param beforeDate 截止日期
     * @return 清理的数据条数
     */
    int cleanupArchivedData(LocalDate beforeDate);
    
    /**
     * 获取归档统计信息
     * @return 归档统计信息
     */
    ArchiveStatistics getArchiveStatistics();
}
