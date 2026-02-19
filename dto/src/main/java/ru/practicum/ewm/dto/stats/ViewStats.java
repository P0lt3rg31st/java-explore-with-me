package ru.practicum.ewm.dto.stats;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ViewStats(
        @NotBlank String app,
        @NotBlank String uri,
        @NotNull @PositiveOrZero Long hits
) {
}
