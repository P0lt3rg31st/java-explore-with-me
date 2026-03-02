package ru.practicum.ewm.main.server.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("ru.practicum.ewm.client")
@EnableFeignClients(basePackages = "ru.practicum.ewm.client")
public class StatsClientConfig {
}