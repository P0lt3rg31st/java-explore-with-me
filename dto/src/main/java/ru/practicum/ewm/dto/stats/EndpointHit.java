package ru.practicum.ewm.dto.stats;

import jakarta.validation.constraints.NotBlank;
import ru.practicum.ewm.dto.stats.date.EwmDateTime;

public record EndpointHit(
        @NotBlank String app,
        @NotBlank String uri,
        @NotBlank String ip,
        @NotBlank @EwmDateTime String timestamp
) {
}