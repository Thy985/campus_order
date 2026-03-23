package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xingchen.backend.entity.BusinessHours;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 营业时间 Mapper
 */
@Mapper
public interface BusinessHoursMapper extends BaseMapper<BusinessHours> {
    
    /**
     * 根据商家ID查询营业时间
     */
    @Select("SELECT id, merchant_id, day_of_week, meal_type, start_time, end_time, is_open, create_time, update_time FROM merchant_business_hours WHERE merchant_id = #{merchantId} ORDER BY day_of_week ASC, meal_type ASC")
    List<BusinessHours> selectByMerchantId(Long merchantId);
    
    /**
     * 根据商家ID和星期查询营业时间
     */
    @Select("SELECT id, merchant_id, day_of_week, meal_type, start_time, end_time, is_open, create_time, update_time FROM merchant_business_hours WHERE merchant_id = #{merchantId} AND day_of_week = #{dayOfWeek} ORDER BY meal_type ASC")
    List<BusinessHours> selectByMerchantIdAndDayOfWeek(Long merchantId, Integer dayOfWeek);
    
    /**
     * 删除商家的所有营业时间
     */
    @Delete("DELETE FROM merchant_business_hours WHERE merchant_id = #{merchantId}")
    int deleteByMerchantId(Long merchantId);
}
