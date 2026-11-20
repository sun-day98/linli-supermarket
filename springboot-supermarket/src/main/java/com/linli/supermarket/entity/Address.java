package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 收货地址表。district 用于配送区域校验。 */
@Data
@TableName("address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    /** 1默认 0非默认 */
    private Integer isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
