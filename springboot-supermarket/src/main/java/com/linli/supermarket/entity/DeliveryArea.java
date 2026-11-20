package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 配送区域（自关联）。parent_id=0 为城市，>0 为区县。 */
@Data
@TableName("delivery_area")
public class DeliveryArea {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    /** 1城市 2区县 */
    private Integer type;
    private Integer enabled;
    private Integer hot;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
