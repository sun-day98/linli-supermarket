package com.linli.supermarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linli.supermarket.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理操作审计切面：记录 actorId / 动作 / 参数摘要 / IP / 耗时 / 结果。
 * 敏感字段（password/token/code/secret）以 ****** 替代，杜绝密码与令牌落日志。
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private static final java.util.Set<String> SENSITIVE = java.util.Set.of(
            "password", "token", "code", "secret", "verifycode", "authorization");

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        String actor = resolveActor();
        String ip = resolveIp();
        String args = summarizeArgs(pjp);
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            Object result = pjp.proceed();
            success = true;
            return result;
        } finally {
            log.info("AUDIT actor={} ip={} action={} args={} success={} costMs={}",
                    actor, ip, auditLog.value().isEmpty()
                            ? ((MethodSignature) pjp.getSignature()).getMethod().getName() : auditLog.value(),
                    args, success, System.currentTimeMillis() - start);
        }
    }

    private String resolveActor() {
        try {
            return String.valueOf(UserContext.getUserId());
        } catch (RuntimeException ex) {
            return "anonymous";
        }
    }

    private String resolveIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            return ip == null || ip.isBlank() ? req.getRemoteAddr() : ip.split(",")[0].trim();
        }
        return "-";
    }

    /** 生成参数摘要：仅字段名 + 脱敏后的标量值，避免大对象与敏感信息入日志。 */
    private String summarizeArgs(ProceedingJoinPoint pjp) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String[] names = sig.getParameterNames();
        Object[] values = pjp.getArgs();
        Map<String, Object> summary = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            String name = i < names.length ? names[i] : "arg" + i;
            Object value = values[i];
            if (isSensitive(name) || value == null) {
                summary.put(name, value == null ? null : "******");
            } else if (isScalar(value)) {
                summary.put(name, value);
            } else if (value instanceof Iterable<?> it) {
                summary.put(name, "size=" + sizeOf(it));
            } else {
                // DTO/实体：尝试 Jackson 转 Map 后脱敏，失败则退化到类型名
                summary.put(name, safeBeanSummary(value));
            }
        }
        try {
            return objectMapper != null ? objectMapper.writeValueAsString(summary) : summary.toString();
        } catch (Exception ex) {
            return summary.toString();
        }
    }

    private boolean isSensitive(String name) {
        String lower = name.toLowerCase();
        return SENSITIVE.stream().anyMatch(lower::contains);
    }

    private boolean isScalar(Object value) {
        return value instanceof Number || value instanceof Boolean
                || value instanceof CharSequence || value instanceof java.time.temporal.Temporal;
    }

    private int sizeOf(Iterable<?> it) {
        int n = 0;
        for (Object ignored : it) {
            n++;
        }
        return n;
    }

    private Object safeBeanSummary(Object bean) {
        if (objectMapper == null) {
            return bean.getClass().getSimpleName();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(bean, Map.class);
            map.replaceAll((k, v) -> isSensitive(String.valueOf(k)) ? "******" : (isScalar(v) ? v : "..."));
            return map;
        } catch (Exception ex) {
            return bean.getClass().getSimpleName();
        }
    }
}
