package com.linli.supermarket.service;

import com.linli.supermarket.vo.OrderVO;
import com.linli.supermarket.vo.StatsOverviewVO;
import com.linli.supermarket.vo.StatsTrendVO;
import java.util.List;

/** 运营统计服务。 */
public interface StatsService {

    StatsOverviewVO overview();

    StatsTrendVO trend(Integer days);

    List<OrderVO> latestOrders(Integer limit);
}
