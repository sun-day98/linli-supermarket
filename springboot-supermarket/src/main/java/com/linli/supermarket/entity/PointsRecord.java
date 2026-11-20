package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 积分流水。类型：1获取 2使用 3回滚。 */
@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;
    /** 变动值，回滚时为负 */
    private Integer points;
    /** 变动后余额 */
    private Integer balance;
    private String source;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
