package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收货地址 Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 查询用户的收货地址列表
     */
    default List<Address> selectByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Address.class)
                .where("user_id = ? AND is_deleted = 0", userId)
                .orderBy("is_default DESC, create_time DESC");
        return selectListByQuery(queryWrapper);
    }

    /**
     * 查询用户的默认地址
     */
    default Address selectDefaultByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Address.class)
                .where("user_id = ? AND is_default = 1 AND is_deleted = 0", userId);
        return selectOneByQuery(queryWrapper);
    }

    /**
     * 取消用户的默认地址
     */
    default int cancelDefault(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Address.class)
                .where("user_id = ? AND is_default = 1", userId);

        Address update = new Address();
        update.setIsDefault(0);
        return updateByQuery(update, queryWrapper);
    }

    /**
     * 统计用户的地址数量
     */
    default Long countByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Address.class)
                .where("user_id = ? AND is_deleted = 0", userId);
        return selectCountByQuery(queryWrapper);
    }
}
