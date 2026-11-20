package com.linli.supermarket.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明管理端写接口：标注后仅 ADMIN 角色可访问。
 *
 * <p>与 {@code TokenInterceptor} 中的路径判定"叠加"（取或），作为新增管理接口的安全网：
 * 新增接口只要标注本注解即可自动纳入鉴权，无需修改拦截器的路径清单。放在
 * {@code /api/admin/**} 前缀下的接口本身已被路径规则覆盖，标注仅为显式声明。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
