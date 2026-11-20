package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.entity.PointsRecord;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.mapper.PointsRecordMapper;
import com.linli.supermarket.mapper.UserMapper;
import com.linli.supermarket.service.PointsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 积分服务实现：实付金额向下取整，1 元 = 1 积分。 */
@Service
public class PointsServiceImpl implements PointsService {

    private static final int TYPE_AWARD = 1;
    private static final int TYPE_ROLLBACK = 3;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void award(Long userId, BigDecimal payAmount, Long orderId) {
        if (payAmount == null) {
            return;
        }
        // 幂等：同一订单只发放一次
        Long exists = pointsRecordMapper.selectCount(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getOrderId, orderId)
                .eq(PointsRecord::getType, TYPE_AWARD));
        if (exists != null && exists > 0) {
            return;
        }
        int points = payAmount.setScale(0, RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return;
        }
        userMapper.addPoints(userId, points);
        User user = userMapper.selectById(userId);
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(TYPE_AWARD);
        record.setPoints(points);
        record.setBalance(user == null || user.getPoints() == null ? points : user.getPoints());
        record.setSource("订单完成奖励");
        record.setOrderId(orderId);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        pointsRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollback(Long userId, Long orderId) {
        Long rolledBack = pointsRecordMapper.selectCount(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getOrderId, orderId)
                .eq(PointsRecord::getType, TYPE_ROLLBACK));
        if (rolledBack != null && rolledBack > 0) {
            return;
        }
        List<PointsRecord> awarded = pointsRecordMapper.selectList(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getOrderId, orderId)
                .eq(PointsRecord::getType, TYPE_AWARD));
        int total = 0;
        for (PointsRecord record : awarded) {
            total += record.getPoints() == null ? 0 : record.getPoints();
        }
        if (total <= 0) {
            return;
        }
        userMapper.addPoints(userId, -total);
        User user = userMapper.selectById(userId);
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(TYPE_ROLLBACK);
        record.setPoints(-total);
        record.setBalance(user == null || user.getPoints() == null ? 0 : user.getPoints());
        record.setSource("订单取消回滚");
        record.setOrderId(orderId);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        pointsRecordMapper.insert(record);
    }

    @Override
    public PageResult<PointsRecord> records(Long userId, Integer current, Integer size) {
        IPage<PointsRecord> page = pointsRecordMapper.selectPage(
                new Page<>(current == null ? 1 : current, size == null ? 10 : size),
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .orderByDesc(PointsRecord::getCreatedAt));
        return PageResult.of(page);
    }
}
