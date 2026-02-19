package ru.practicum.ewm.dto.stats;

import jakarta.validation.constraints.NotBlank;

public record EndpointHit(
        @NotBlank String app,
        @NotBlank String uri,
        @NotBlank String ip,
        @NotBlank String timestamp
) {}