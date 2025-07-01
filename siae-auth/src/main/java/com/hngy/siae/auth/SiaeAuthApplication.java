package com.hngy.siae.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Auth服务启动类
 * 
 * @author KEYKB
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.hngy.siae.auth.mapper")
public class SiaeAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SiaeAuthApplication.class, args);
    }
} 