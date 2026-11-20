package com.linli.supermarket.controller;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.CheckoutDTO;
import com.linli.supermarket.dto.OrderQueryDTO;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.service.OrderService;
import com.linli.supermarket.util.UserContext;
import com.linli.supermarket.vo.CheckoutPreviewVO;
import com.linli.supermarket.vo.OrderVO;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 订单与履约接口。用户端与管理端接口集中在同一 Controller，路径各自独立。 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 金额试算。 */
    @PostMapping("/api/orders/preview")
    public Result<CheckoutPreviewVO> preview(@Valid @RequestBody CheckoutDTO dto) {
        return Result.ok(orderService.preview(dto));
    }

    /** 提交订单。 */
    @PostMapping("/api/orders")
    public Result<Orders> create(@Valid @RequestBody CheckoutDTO dto) {
        return Result.ok(orderService.createOrder(UserContext.getUserId(), dto));
    }

    /** 我的订单。 */
    @GetMapping("/api/orders")
    public Result<PageResult<OrderVO>> myOrders(OrderQueryDTO query) {
        return Result.ok(orderService.pageMine(UserContext.getUserId(), query));
    }

    /** 订单详情（校验归属）。 */
    @GetMapping("/api/orders/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detailForUser(UserContext.getUserId(), id));
    }

    /** 模拟支付（幂等）。 */
    @PostMapping("/api/orders/{id}/pay")
    public Result<Orders> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        // 归属校验：仅订单所有者可支付，避免越权操作他人订单（IDOR）。
        orderService.assertOwned(UserContext.getUserId(), id);
        String payType = body == null ? null : (String) body.get("payType");
        return Result.ok(orderService.pay(id, payType));
    }

    /** 后台发货。 */
    @PostMapping("/api/orders/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long deliverymanId = null;
        if (body != null && body.get("deliverymanId") != null) {
            deliverymanId = Long.valueOf(body.get("deliverymanId").toString());
        }
        orderService.deliver(id, deliverymanId);
        return Result.ok();
    }

    /** 确认收货（发放积分）。 */
    @PostMapping("/api/orders/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        // 归属校验：仅订单所有者可确认收货。
        orderService.assertOwned(UserContext.getUserId(), id);
        orderService.finish(id);
        return Result.ok();
    }

    /** 取消订单（回滚库存/券/积分）。 */
    @PostMapping("/api/orders/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        // 归属校验：仅订单所有者可取消订单。
        orderService.assertOwned(UserContext.getUserId(), id);
        orderService.cancel(id);
        return Result.ok();
    }

    /** 管理端全量订单。 */
    @GetMapping("/api/admin/orders")
    public Result<PageResult<OrderVO>> adminOrders(OrderQueryDTO query) {
        return Result.ok(orderService.pageAdmin(query));
    }

    /** 管理端订单详情。 */
    @GetMapping("/api/admin/orders/{id}")
    public Result<OrderVO> adminDetail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }
}
