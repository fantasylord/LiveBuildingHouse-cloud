package com.livehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动类
 */
@SpringBootApplication
@MapperScan("com.livehouse.mapper")
@EnableCaching
public class LiveHouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveHouseApplication.class, args);
    }
}