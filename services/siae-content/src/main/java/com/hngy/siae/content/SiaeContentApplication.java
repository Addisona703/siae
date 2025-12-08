package com.hngy.siae.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = "com.hngy.siae.api")
@EnableScheduling
@EnableAsync
// @EnableMethodSecurity(prePostEnabled = true)  // 开发环境禁用，由 siae.security.enabled 控制
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeContentApplication.class, args);
    }

}
