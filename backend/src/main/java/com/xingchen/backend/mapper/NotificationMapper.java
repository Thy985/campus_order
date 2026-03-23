package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.Notification;
import org.apache.ibatis.annotations.*;
import com.mybatisflex.core.BaseMapper;

import java.util.List;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据ID查询（带软删除检查）
     */
    @Select("SELECT id, user_id, type, title, content, extra_data, is_read, is_deleted, create_time FROM notification WHERE id = #{id} AND is_deleted = 0")
    Notification selectById(@Param("id") Long id);

    /**
     * 查询用户的消息列表
     */
    @Select("SELECT id, user_id, type, title, content, extra_data, is_read, is_deleted, create_time FROM notification WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<Notification> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询未读数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int markAsRead(@Param("id") Long id);

    /**
     * 标记所有为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 软删除通知（逻辑删除）
     * 注意：方法名改为 logicDeleteById 避免与 BaseMapper 的 deleteById 冲突
     */
    @Update("UPDATE notification SET is_deleted = 1, delete_time = NOW() WHERE id = #{id}")
    int logicDeleteById(@Param("id") Long id);
}
