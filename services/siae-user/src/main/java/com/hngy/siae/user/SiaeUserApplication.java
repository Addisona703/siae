package com.hngy.siae.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients(basePackages = {"com.hngy.siae.api", "com.hngy.siae.user"})
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiaeUserApplication.class, args);
    }
}
