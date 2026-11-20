package com.linli.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linli.supermarket.entity.Coupon;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 优惠券定义 Mapper。 */
public interface CouponMapper extends BaseMapper<Coupon> {

    /** 领取时原子增加已发放数量，库存不足返回 0。 */
    @Update("UPDATE coupon SET issued = issued + 1 WHERE id = #{id} "
            + "AND (total IS NULL OR total = 0 OR issued < total)")
    int increaseIssued(@Param("id") Long id);
}
