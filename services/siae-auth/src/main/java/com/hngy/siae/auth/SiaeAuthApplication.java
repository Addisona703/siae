package com.hngy.siae.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Auth服务启动类
 * 
 * @author KEYKB
 */
@EnableAsync
@EnableFeignClients(basePackages = "com.hngy.siae.api")
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SiaeAuthApplication.class, args);
    }
} 