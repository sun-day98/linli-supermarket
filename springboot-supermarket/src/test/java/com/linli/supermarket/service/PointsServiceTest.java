package com.linli.supermarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linli.supermarket.entity.PointsRecord;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.mapper.PointsRecordMapper;
import com.linli.supermarket.mapper.UserMapper;
import com.linli.supermarket.service.impl.PointsServiceImpl;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** 积分测试：实付金额向下取整、按 orderId 幂等发放与回滚。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PointsServiceTest {

    private static final Long USER_ID = 1001L;
    private static final Long ORDER_ID = 3001L;

    @Mock private PointsRecordMapper pointsRecordMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks private PointsServiceImpl pointsService;

    @Test
    @DisplayName("发放：实付 36.99 元按 DOWN 取整为 36 积分，1元=1积分")
    void award_floorsPayAmount() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(0L);
        User user = new User();
        user.setPoints(164);
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        pointsService.award(USER_ID, new BigDecimal("36.99"), ORDER_ID);

        verify(userMapper).addPoints(USER_ID, 36);
        ArgumentCaptor<PointsRecord> captor = ArgumentCaptor.forClass(PointsRecord.class);
        verify(pointsRecordMapper).insert(captor.capture());
        assertEquals(36, captor.getValue().getPoints());
        assertEquals(1, captor.getValue().getType());
        assertEquals(ORDER_ID, captor.getValue().getOrderId());
        assertEquals(164, captor.getValue().getBalance());
    }

    @Test
    @DisplayName("发放：实付 0.99 元不足 1 元不发积分")
    void award_belowOneYuanSkips() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(0L);

        pointsService.award(USER_ID, new BigDecimal("0.99"), ORDER_ID);

        verify(userMapper, never()).addPoints(anyLong(), anyInt());
        verify(pointsRecordMapper, never()).insert(any(PointsRecord.class));
    }

    @Test
    @DisplayName("发放：payAmount 为 null 安全跳过")
    void award_nullAmountSkips() {
        pointsService.award(USER_ID, null, ORDER_ID);

        verify(pointsRecordMapper, never()).selectCount(any());
        verify(userMapper, never()).addPoints(anyLong(), anyInt());
    }

    @Test
    @DisplayName("发放幂等：同一订单已有发放记录时不重复加积分")
    void award_whenAlreadyAwarded_skips() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(1L);

        pointsService.award(USER_ID, new BigDecimal("99.00"), ORDER_ID);

        verify(userMapper, never()).addPoints(anyLong(), anyInt());
        verify(pointsRecordMapper, never()).insert(any(PointsRecord.class));
    }

    @Test
    @DisplayName("回滚：撤销该订单全部已发积分（多条记录求和）")
    void rollback_deductsAllAwardedPoints() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(0L);
        PointsRecord first = new PointsRecord();
        first.setPoints(36);
        PointsRecord second = new PointsRecord();
        second.setPoints(5);
        when(pointsRecordMapper.selectList(any())).thenReturn(Arrays.asList(first, second));
        User user = new User();
        user.setPoints(123);
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        pointsService.rollback(USER_ID, ORDER_ID);

        verify(userMapper).addPoints(USER_ID, -41);
        ArgumentCaptor<PointsRecord> captor = ArgumentCaptor.forClass(PointsRecord.class);
        verify(pointsRecordMapper).insert(captor.capture());
        assertEquals(-41, captor.getValue().getPoints());
        assertEquals(3, captor.getValue().getType());
        assertEquals(ORDER_ID, captor.getValue().getOrderId());
    }

    @Test
    @DisplayName("回滚幂等：已有回滚记录时不重复扣积分")
    void rollback_whenAlreadyRolledBack_skips() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(1L);

        pointsService.rollback(USER_ID, ORDER_ID);

        verify(userMapper, never()).addPoints(anyLong(), anyInt());
        verify(pointsRecordMapper, never()).insert(any(PointsRecord.class));
    }

    @Test
    @DisplayName("回滚：无发放记录时不产生负积分")
    void rollback_whenNoAwardRecords_skips() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(0L);
        when(pointsRecordMapper.selectList(any())).thenReturn(Collections.emptyList());

        pointsService.rollback(USER_ID, ORDER_ID);

        verify(userMapper, never()).addPoints(anyLong(), anyInt());
        verify(pointsRecordMapper, never()).insert(any(PointsRecord.class));
    }

    @Test
    @DisplayName("发放后回滚：净积分变化为 0（同一订单 36.99 元）")
    void awardThenRollback_isNetZero() {
        when(pointsRecordMapper.selectCount(any())).thenReturn(0L);
        when(pointsRecordMapper.selectList(any())).thenReturn(Collections.emptyList());

        pointsService.award(USER_ID, new BigDecimal("36.99"), ORDER_ID);
        // 模拟发放记录已落库
        PointsRecord awarded = new PointsRecord();
        awarded.setPoints(36);
        when(pointsRecordMapper.selectList(any())).thenReturn(Collections.singletonList(awarded));
        pointsService.rollback(USER_ID, ORDER_ID);

        verify(userMapper).addPoints(USER_ID, 36);
        verify(userMapper).addPoints(USER_ID, -36);
    }
}
