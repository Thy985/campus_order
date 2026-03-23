package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;



/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据手机号查询用户
     */
    default Optional<User> selectByPhone(String phone) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(User.class)
                .where("phone = ? AND is_deleted = 0", phone);
        return Optional.ofNullable(selectOneByQuery(queryWrapper));
    }

    /**
     * 根据邮箱查询用户
     */
    default Optional<User> selectByEmail(String email) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(User.class)
                .where("email = ? AND is_deleted = 0", email);
        return Optional.ofNullable(selectOneByQuery(queryWrapper));
    }

    /**
     * 根据用户名查询用户
     */
    default Optional<User> selectByUsername(String username) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(User.class)
                .where("username = ? AND is_deleted = 0", username);
        return Optional.ofNullable(selectOneByQuery(queryWrapper));
    }
    
    /**
     * 根据用户类型查询用户列表
     */
    default java.util.List<User> selectByUserType(Integer userType, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(User.class)
                .where("user_type = ? AND is_deleted = 0", userType)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }

    /**
     * 根据邮箱更新密码
     */
    default void updateByEmail(String email, String password) {
        User user = new User();
        user.setPassword(password);
        user.setUpdateTime(java.time.LocalDateTime.now());
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("email = ? AND is_deleted = 0", email);
        updateByQuery(user, queryWrapper);
    }
}
