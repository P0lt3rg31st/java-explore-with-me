package ru.practicum.ewm.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ParticipationRequestDto(
        @NotNull
        @Positive
        Long id,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime created,

        @NotNull
        @Positive
        Long event,

        @NotNull
        @Positive
        Long requester,

        @NotNull
        ParticipationRequestStatus status
) {
}