package com.linli.supermarket.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理操作审计标记：被标注的方法由 {@link AuditAspect} 记录操作人、动作、参数摘要与 IP。
 * 日志中会自动脱敏 password/token/code 等敏感字段。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 操作描述，如"改价""发券""发货"。 */
    String value() default "";
}
