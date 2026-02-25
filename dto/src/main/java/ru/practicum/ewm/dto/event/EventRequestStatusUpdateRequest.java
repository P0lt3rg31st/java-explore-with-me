package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        @NotNull
        @NotEmpty
        List<@NotNull @Positive Long> requestIds,

        @NotNull
        RequestUpdateStatus status
) {
}