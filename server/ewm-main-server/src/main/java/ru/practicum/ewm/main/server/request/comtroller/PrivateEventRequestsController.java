package ru.practicum.ewm.main.server.request.comtroller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.ParticipationRequestStatus;
import ru.practicum.ewm.main.server.request.ParticipationRequest;
import ru.practicum.ewm.main.server.request.ParticipationRequestMapper;
import ru.practicum.ewm.main.server.request.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateEventRequestsController {

    private final ParticipationRequestService requestService;
    private final ParticipationRequestMapper requestMapper;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive long userId,
                                                          @PathVariable @Positive long eventId) {
        List<ParticipationRequest> requests = requestService.getEventRequests(userId, eventId);
        return requestMapper.toDtoList(requests);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateStatuses(@PathVariable @Positive long userId,
                                                         @PathVariable @Positive long eventId,
                                                         @Valid @RequestBody EventRequestStatusUpdateRequest dto) {

        ParticipationRequestService.StatusUpdateResult result = requestService.updateRequestStatuses(
                userId,
                eventId,
                dto.requestIds(),
                ParticipationRequestStatus.valueOf(dto.status())
        );

        return new EventRequestStatusUpdateResult(
                requestMapper.toDtoList(result.confirmed()),
                requestMapper.toDtoList(result.rejected())
        );
    }
}