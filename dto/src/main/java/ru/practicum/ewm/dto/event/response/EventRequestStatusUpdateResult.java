package ru.practicum.ewm.dto.event.response;


import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

public record EventRequestStatusUpdateResult(
        List<ParticipationRequestDto> confirmedRequests,
        List<ParticipationRequestDto> rejectedRequests
) {
}