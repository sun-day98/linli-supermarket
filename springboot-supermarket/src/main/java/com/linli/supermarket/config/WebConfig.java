package com.linli.supermarket.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：Token 拦截器注册、跨域配置、统一日期与金额序列化。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // @Lazy 打断循环依赖：WebConfig → TokenInterceptor → TokenUtil → ObjectMapper → WebConfig。
    // ObjectMapper 的构建依赖本类的 jacksonCustomizer @Bean，若此处 eager 注入拦截器会成环。
    @Lazy
    @Autowired
    private TokenInterceptor tokenInterceptor;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/error");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins == null ? new String[]{"*"}
                : allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }
        CorsRegistration cors = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .maxAge(3600);
        boolean wildcard = origins.length == 1 && "*".equals(origins[0]);
        if (wildcard) {
            // 演示默认：放行任意来源，但关闭凭证携带（本系统用 Bearer Token，不依赖 Cookie）。
            // allowCredentials(true) 与通配来源组合会让任意站点携带凭证请求，生产必须收紧。
            cors.allowedOriginPatterns("*").allowCredentials(false);
        } else {
            cors.allowedOrigins(origins).allowCredentials(true);
        }
    }

    /**
     * 统一时间格式为 yyyy-MM-dd HH:mm:ss，金额固定保留两位小数（HALF_UP）。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.deserializers(new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
            builder.serializerByType(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                @Override
                public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else {
                        gen.writeNumber(value.setScale(2, RoundingMode.HALF_UP));
                    }
                }
            });
        };
    }
}
