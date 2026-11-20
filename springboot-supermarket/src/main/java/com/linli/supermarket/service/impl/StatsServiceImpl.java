package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.service.OrderService;
import com.linli.supermarket.service.StatsService;
import com.linli.supermarket.vo.OrderVO;
import com.linli.supermarket.vo.StatsOverviewVO;
import com.linli.supermarket.vo.StatsTrendVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 运营统计服务实现：聚合查询全部基于时间参数，兼容 MySQL 与 H2。 */
@Service
public class StatsServiceImpl implements StatsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderService orderService;

    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Timestamp today = Timestamp.valueOf(todayStart);

        BigDecimal todaySales = querySum(
                "SELECT COALESCE(SUM(pay_amount),0) FROM orders WHERE status IN (1,2,3) AND created_at >= ?", today);
        long todayOrders = queryCount(
                "SELECT COUNT(*) FROM orders WHERE created_at >= ?", today);
        long newMembers = queryCount(
                "SELECT COUNT(*) FROM `user` WHERE created_at >= ?", today);
        long pendingPay = queryCount(
                "SELECT COUNT(*) FROM orders WHERE status = 0");
        long delivering = queryCount(
                "SELECT COUNT(*) FROM orders WHERE status = 2");
        long lowStock = queryCount(
                "SELECT COUNT(*) FROM product WHERE status = 1 AND stock <= stock_alert");
        long totalMembers = queryCount("SELECT COUNT(*) FROM `user`");
        long totalOrders = queryCount("SELECT COUNT(*) FROM orders");
        BigDecimal totalSales = querySum(
                "SELECT COALESCE(SUM(pay_amount),0) FROM orders WHERE status IN (1,2,3)");

        vo.setTodaySales(todaySales);
        vo.setTodayOrders((int) todayOrders);
        vo.setAvgOrderAmount(todayOrders == 0 ? ZERO
                : todaySales.divide(BigDecimal.valueOf(todayOrders), 2, RoundingMode.HALF_UP));
        vo.setNewMembers((int) newMembers);
        vo.setPendingPayOrders((int) pendingPay);
        vo.setDeliveringOrders((int) delivering);
        vo.setLowStockProducts((int) lowStock);
        vo.setTotalMembers(totalMembers);
        vo.setTotalOrders(totalOrders);
        vo.setTotalSales(totalSales);
        return vo;
    }

    @Override
    public StatsTrendVO trend(Integer days) {
        int range = days == null || days <= 0 ? 7 : Math.min(days, 90);
        LocalDate start = LocalDate.now().minusDays(range - 1L);
        List<Object[]> rows = jdbcTemplate.query(
                "SELECT created_at, pay_amount FROM orders WHERE status IN (1,2,3) AND created_at >= ?",
                (rs, rowNum) -> new Object[]{rs.getTimestamp("created_at"), rs.getBigDecimal("pay_amount")},
                Timestamp.valueOf(start.atStartOfDay()));

        Map<LocalDate, BigDecimal> salesByDay = new HashMap<>();
        Map<LocalDate, Integer> ordersByDay = new HashMap<>();
        for (Object[] row : rows) {
            Timestamp ts = (Timestamp) row[0];
            if (ts == null) {
                continue;
            }
            LocalDate day = ts.toLocalDateTime().toLocalDate();
            BigDecimal amount = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            salesByDay.merge(day, amount, BigDecimal::add);
            ordersByDay.merge(day, 1, Integer::sum);
        }

        StatsTrendVO vo = new StatsTrendVO();
        for (int i = 0; i < range; i++) {
            LocalDate day = start.plusDays(i);
            vo.getDates().add(day.toString());
            BigDecimal amount = salesByDay.getOrDefault(day, BigDecimal.ZERO);
            vo.getSales().add(amount.setScale(2, RoundingMode.HALF_UP));
            vo.getOrders().add(ordersByDay.getOrDefault(day, 0));
        }
        return vo;
    }

    @Override
    public List<OrderVO> latestOrders(Integer limit) {
        int size = limit == null || limit <= 0 ? 5 : Math.min(limit, 50);
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .orderByDesc(Orders::getCreatedAt)
                .last("LIMIT " + size));
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量装配明细，避免逐单 detail() 造成 2N 查询；再按原时间倒序还原顺序。
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        Map<Long, OrderVO> byId = orderService.detailBatch(orderIds).stream()
                .collect(Collectors.toMap(OrderVO::getId, Function.identity(), (a, b) -> a));
        List<OrderVO> result = new ArrayList<>();
        for (Orders order : orders) {
            OrderVO vo = byId.get(order.getId());
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private BigDecimal querySum(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private long queryCount(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
