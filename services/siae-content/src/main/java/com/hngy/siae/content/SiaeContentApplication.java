package com.hngy.siae.content;

import com.hngy.siae.web.feign.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableFeignClients(basePackages = "com.hngy.siae.content", defaultConfiguration = DefaultFeignConfig.class)
@EnableScheduling
@EnableMethodSecurity(prePostEnabled = true)
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
public class SiaeContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeContentApplication.class, args);
    }

}
