package ru.practicum.ewm.dto.event.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.practicum.ewm.dto.event.state.RequestUpdateStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        @NotNull
        @NotEmpty
        List<@NotNull @Positive Long> requestIds,

        @NotNull
        RequestUpdateStatus status
) {
}