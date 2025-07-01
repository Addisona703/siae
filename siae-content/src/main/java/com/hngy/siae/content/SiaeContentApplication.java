package com.hngy.siae.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SiaeContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeContentApplication.class, args);
    }

}
