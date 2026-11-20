package com.linli.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linli.supermarket.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 会员 Mapper。 */
public interface UserMapper extends BaseMapper<User> {

    /** 积分增减（delta 可正可负）。 */
    @Update("UPDATE `user` SET points = points + #{delta} WHERE id = #{id}")
    int addPoints(@Param("id") Long id, @Param("delta") Integer delta);
}
