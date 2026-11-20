package com.linli.supermarket.dto;

import lombok.Data;

/** 订单列表查询条件。 */
@Data
public class OrderQueryDTO {

    /** 订单状态：0待付 1待配送 2配送中 3完成 4取消；null 表示全部。 */
    private Integer status;

    /** 订单号 / 收货人 / 手机号关键词。 */
    private String keyword;

    private Integer current = 1;
    private Integer size = 10;

    /** 下单时间起始（yyyy-MM-dd HH:mm:ss），可选。 */
    private String startTime;

    /** 下单时间截止，可选。 */
    private String endTime;
}
