package ru.practicum.ewm.dto.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.common.Location;
import ru.practicum.ewm.dto.event.state.EventState;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

public record EventFullDto(
        Long id,
        String annotation,
        CategoryDto category,
        Long confirmedRequests,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdOn,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,
        UserShortDto initiator,
        Location location,
        Boolean paid,
        Integer participantLimit,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedOn,
        Boolean requestModeration,
        EventState state,
        String title,
        Long views
) {
}