package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.MerchantCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商家分类 Mapper
 */
@Mapper
public interface MerchantCategoryMapper extends BaseMapper<MerchantCategory> {
    
    /**
     * 查询所有商家分类
     */
    default List<MerchantCategory> selectAll() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(MerchantCategory.class)
                .where("is_deleted = 0")
                .orderBy("sort_order DESC, create_time ASC");
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据名称查询商家分类
     */
    default MerchantCategory selectByName(String name) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(MerchantCategory.class)
                .where("name = ? AND is_deleted = 0", name);
        return selectOneByQuery(queryWrapper);
    }
}
