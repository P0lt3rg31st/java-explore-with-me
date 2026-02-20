package ru.practicum.ewm.stats.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.dto.handler.GlobalExceptionHandler;

@Configuration
@Import(GlobalExceptionHandler.class)
public class ErrorHandlingConfig {
}