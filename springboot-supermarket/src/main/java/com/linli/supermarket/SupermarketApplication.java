package com.linli.supermarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 邻里小超社区电商系统后端启动类。
 *
 * <p>MapperScan 统一扫描 mapper 包，避免每个 Mapper 重复添加 @Mapper。</p>
 */
@SpringBootApplication
@MapperScan("com.linli.supermarket.mapper")
public class SupermarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketApplication.class, args);
    }
}
