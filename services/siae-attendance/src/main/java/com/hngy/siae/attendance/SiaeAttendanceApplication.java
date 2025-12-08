package com.hngy.siae.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 考勤服务启动类
 *
 * @author SIAE Team
 */
@EnableAsync
@EnableScheduling
@EnableFeignClients(basePackages = "com.hngy.siae.api")
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeAttendanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiaeAttendanceApplication.class, args);
    }
}
