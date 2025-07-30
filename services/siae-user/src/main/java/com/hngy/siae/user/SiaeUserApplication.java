package com.hngy.siae.user;

import com.hngy.siae.web.feign.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.hngy.siae", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiaeUserApplication.class, args);
    }
}
