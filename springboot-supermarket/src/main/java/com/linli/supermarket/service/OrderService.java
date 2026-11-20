package com.linli.supermarket.service;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.dto.CheckoutDTO;
import com.linli.supermarket.dto.OrderQueryDTO;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.vo.CheckoutPreviewVO;
import com.linli.supermarket.vo.OrderVO;

/** 订单与履约服务：系统的交易一致性边界。 */
public interface OrderService {

    /** 金额试算（当前登录用户）。 */
    CheckoutPreviewVO preview(CheckoutDTO dto);

    /** 提交订单：二次校验价格/配送费 + 扣库存 + 锁券 + 校验配送区县。 */
    Orders createOrder(Long userId, CheckoutDTO dto);

    /** 模拟支付，CAS 幂等。 */
    Orders pay(Long orderId, String payType);

    /** 发货：1→2。 */
    void deliver(Long orderId, Long deliverymanId);

    /** 确认收货：2→3 并发放积分。 */
    void finish(Long orderId);

    /** 取消订单并回滚库存/券/积分。 */
    void cancel(Long orderId);

    PageResult<OrderVO> pageMine(Long userId, OrderQueryDTO dto);

    PageResult<OrderVO> pageAdmin(OrderQueryDTO dto);

    /** 管理端订单详情。 */
    OrderVO detail(Long orderId);

    /** 用户端订单详情（校验归属）。 */
    OrderVO detailForUser(Long userId, Long orderId);

    /** 轻量归属校验：订单不存在或不属于该用户时抛异常，不加载订单明细。 */
    void assertOwned(Long userId, Long orderId);

    /** 预留入口：取消超时未支付订单（30 分钟）。 */
    void cancelTimeoutOrders();
}
