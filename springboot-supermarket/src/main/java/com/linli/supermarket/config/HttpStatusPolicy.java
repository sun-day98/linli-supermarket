package com.linli.supermarket.config;

import com.linli.supermarket.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HTTP 状态码策略开关。
 *
 * <p>演示环境默认 {@code false}：所有响应 HTTP 200，业务结果由 body.code 表达，兼容三端前端
 * （axios 只处理 2xx、按 body.code===401 跳登录）。生产环境可将
 * {@code APP_STRICT_HTTP_STATUS=true} 开启标准语义：401/403/400/500 映射到对应 HTTP 状态，
 * 便于网关/监控按状态码识别。开启时需前端同步改造（axios 增加 validateStatus 放行 4xx 再读 body.code）。</p>
 */
@Component
public class HttpStatusPolicy {

    private final boolean strict;

    public HttpStatusPolicy(@Value("${app.api.strict-http-status:false}") boolean strict) {
        this.strict = strict;
    }

    public boolean isStrict() {
        return strict;
    }

    /** 将业务错误码映射为 HTTP 状态码；非严格模式一律返回 200。 */
    public int httpStatus(ResultCode code) {
        return code == null ? 200 : httpStatus(code.getCode());
    }

    /** 按业务错误码数值映射（兼容 BusinessException 的 Integer code）。 */
    public int httpStatus(Integer code) {
        if (!strict || code == null) {
            return 200;
        }
        return switch (code) {
            case 401 -> 401;
            case 400, 4001, 4002, 4003 -> 400;
            case 500 -> 500;
            default -> 200;
        };
    }
}
