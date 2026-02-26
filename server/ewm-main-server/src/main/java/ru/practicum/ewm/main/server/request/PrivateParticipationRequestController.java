package ru.practicum.ewm.main.server.request;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateParticipationRequestController {

    private final ParticipationRequestService requestService;
    private final ParticipationRequestMapper requestMapper;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive long userId) {
        List<ParticipationRequest> requests = requestService.getUserRequests(userId);
        return requestMapper.toDtoList(requests);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable @Positive long userId,
                                              @RequestParam @Positive long eventId) {
        ParticipationRequest created = requestService.addParticipationRequest(userId, eventId);
        return requestMapper.toDto(created);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable @Positive long userId,
                                          @PathVariable @Positive long requestId) {
        ParticipationRequest canceled = requestService.cancelRequest(userId, requestId);
        return requestMapper.toDto(canceled);
    }
}