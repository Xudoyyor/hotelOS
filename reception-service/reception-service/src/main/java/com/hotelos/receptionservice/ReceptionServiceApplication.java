package com.hotelos.receptionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReceptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceptionServiceApplication.class, args);
    }

}
