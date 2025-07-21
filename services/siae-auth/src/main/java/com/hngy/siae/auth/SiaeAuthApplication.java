package com.hngy.siae.auth;

import com.hngy.siae.common.feign.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Auth服务启动类
 * 
 * @author KEYKB
 */
@EnableFeignClients(basePackages = "com.hngy.siae", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SiaeAuthApplication.class, args);
    }
} 