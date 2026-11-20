package com.linli.supermarket.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 近 N 日销售趋势，供 ECharts 折线图直接使用。 */
@Data
public class StatsTrendVO {

    /** yyyy-MM-dd 日期序列。 */
    private List<String> dates = new ArrayList<>();
    /** 与 dates 一一对应的销售额。 */
    private List<BigDecimal> sales = new ArrayList<>();
    /** 与 dates 一一对应的订单数。 */
    private List<Integer> orders = new ArrayList<>();
}
