package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = StatsFeign.class)
class StatsClientFeignConfig { }