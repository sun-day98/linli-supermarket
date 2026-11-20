package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.service.StatsService;
import com.linli.supermarket.vo.OrderVO;
import com.linli.supermarket.vo.StatsOverviewVO;
import com.linli.supermarket.vo.StatsTrendVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 经营统计接口（管理端）。 */
@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview() {
        return Result.ok(statsService.overview());
    }

    @GetMapping("/trend")
    public Result<StatsTrendVO> trend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        return Result.ok(statsService.trend(days));
    }

    @GetMapping("/latest-orders")
    public Result<List<OrderVO>> latestOrders(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return Result.ok(statsService.latestOrders(limit));
    }
}
