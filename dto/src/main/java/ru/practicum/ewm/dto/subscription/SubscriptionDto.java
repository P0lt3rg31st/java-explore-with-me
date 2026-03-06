package ru.practicum.ewm.dto.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

public record SubscriptionDto(
        Long id,

        UserShortDto subscriber,
        UserShortDto target,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdOn
) {
}
