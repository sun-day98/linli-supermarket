package com.linli.supermarket.service;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.entity.PointsRecord;
import java.math.BigDecimal;

/** 积分服务：实付金额向下取整，1 元 = 1 积分。 */
public interface PointsService {

    /** 确认收货时发放积分并写流水。 */
    void award(Long userId, BigDecimal payAmount, Long orderId);

    /** 取消已完成订单时回滚积分。 */
    void rollback(Long userId, Long orderId);

    PageResult<PointsRecord> records(Long userId, Integer current, Integer size);
}
