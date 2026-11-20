package com.linli.supermarket.vo;

import java.math.BigDecimal;
import lombok.Data;

/** 管理后台经营概览指标。 */
@Data
public class StatsOverviewVO {

    private BigDecimal todaySales = BigDecimal.ZERO.setScale(2);
    private Integer todayOrders = 0;
    private BigDecimal avgOrderAmount = BigDecimal.ZERO.setScale(2);
    private Integer newMembers = 0;
    private Integer pendingPayOrders = 0;
    private Integer deliveringOrders = 0;
    private Integer lowStockProducts = 0;
    private Long totalMembers = 0L;
    private Long totalOrders = 0L;
    private BigDecimal totalSales = BigDecimal.ZERO.setScale(2);
}
