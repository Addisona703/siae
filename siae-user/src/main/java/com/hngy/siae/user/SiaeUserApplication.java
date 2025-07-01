package com.hngy.siae.user;

import com.hngy.siae.common.feign.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
public class SiaeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiaeUserApplication.class, args);
    }
}
