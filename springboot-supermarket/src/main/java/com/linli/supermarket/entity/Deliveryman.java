package com.linli.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 配送员。 */
@Data
@TableName("deliveryman")
public class Deliveryman {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    /** 1在岗 0停用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
