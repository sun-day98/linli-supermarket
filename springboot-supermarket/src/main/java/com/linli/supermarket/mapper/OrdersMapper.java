package com.linli.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linli.supermarket.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 订单 Mapper，含状态流转 CAS 操作。 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /** 支付：待付款(0) → 待配送(1)，CAS 幂等。 */
    @Update("UPDATE orders SET status = 1, pay_type = #{payType}, pay_time = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int payCas(@Param("id") Long id, @Param("payType") String payType);

    /** 发货：待配送(1) → 配送中(2)。 */
    @Update("UPDATE orders SET status = 2, deliveryman_id = #{deliverymanId}, delivery_time = NOW() "
            + "WHERE id = #{id} AND status = 1")
    int deliverCas(@Param("id") Long id, @Param("deliverymanId") Long deliverymanId);

    /** 确认收货：配送中(2) → 已完成(3)。 */
    @Update("UPDATE orders SET status = 3, finish_time = NOW() WHERE id = #{id} AND status = 2")
    int finishCas(@Param("id") Long id);

    /** 取消：非取消状态 → 已取消(4)。 */
    @Update("UPDATE orders SET status = 4, cancel_time = NOW() WHERE id = #{id} AND status <> 4")
    int cancelCas(@Param("id") Long id);
}
