package ru.practicum.ewm.main.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EwmMainServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EwmMainServerApplication.class, args);
    }
}