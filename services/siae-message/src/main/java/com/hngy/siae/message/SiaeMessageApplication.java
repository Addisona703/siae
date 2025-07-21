package com.hngy.siae.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SiaeMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeMessageApplication.class, args);
    }

}
