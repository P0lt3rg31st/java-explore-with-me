package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NewEventDto(
        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,

        @NotNull
        @Positive
        Long category,

        @NotBlank
        @Size(min = 20, max = 7000)
        String description,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        @NotNull
        @Valid
        Location location,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank
        @Size(min = 3, max = 120)
        String title
) {
}