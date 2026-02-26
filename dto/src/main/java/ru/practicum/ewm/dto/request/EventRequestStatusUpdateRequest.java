package ru.practicum.ewm.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EventRequestStatusUpdateRequest(
        @NotEmpty
        Set<Long> requestIds,
        @NotNull
        String status
) {
}