package com.linli.supermarket.common;

/**
 * 全局统一错误码。三端共用同一数值口径。
 */
public enum ResultCode {

    /** 成功。 */
    SUCCESS(0, "ok"),
    /** 参数错误。 */
    PARAM_ERROR(400, "参数错误"),
    /** 未登录或登录失效。 */
    UNAUTHORIZED(401, "请先登录"),
    /** 库存不足。 */
    STOCK_NOT_ENOUGH(4001, "库存不足"),
    /** 优惠券无效或使用条件不满足。 */
    COUPON_INVALID(4002, "优惠券不可用"),
    /** 订单状态不允许当前操作。 */
    ORDER_STATUS_ERROR(4003, "订单状态不允许该操作"),
    /** 服务器内部错误。 */
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
