package com.hngy.siae.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 通知服务启动类
 *
 * @author KEYKB
 */
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
@EnableFeignClients(basePackages = "com.hngy.siae.notification")
public class SiaeNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeNotificationApplication.class, args);
    }

}