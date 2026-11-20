package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 门店设置（单行，id 固定为 1）。 */
@Data
@TableName("store_setting")
public class StoreSetting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String storeName;
    private String slogan;
    private String notice;
    private BigDecimal deliveryFee;
    private BigDecimal freeDeliveryThreshold;
    private String servicePhone;
    private String logo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
