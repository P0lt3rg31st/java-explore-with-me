package ru.practicum.ewm.main.server.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.main.server.category.Category;
import ru.practicum.ewm.main.server.category.CategoryService;
import ru.practicum.ewm.main.server.request.ParticipationRequestService;
import ru.practicum.ewm.main.server.user.User;
import ru.practicum.ewm.main.server.user.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    private final UserService userService;
    private final CategoryService categoryService;

    private final StatsTracker statsTracker;
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<EventShortDto> getUserEvents(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = userService.getById(userId);
        List<Event> events = eventService.getUserEvents(user, from, size);

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> viewsById = statsTracker.viewsForEvents(ids);
        Map<Long, Long> confirmedById = requestService.getConfirmedCountsForEvents(ids);

        return events.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        viewsById.getOrDefault(e.getId(), 0L),
                        confirmedById.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable long userId,
            @RequestBody @Valid NewEventDto dto
    ) {
        User initiator = userService.getById(userId);
        Category category = categoryService.findById(dto.category());

        Event event = eventMapper.toEntity(dto, category, initiator);
        Event created = eventService.create(event, initiator, category);

        return eventMapper.toFullDto(created, 0L, 0L);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable long userId,
            @PathVariable long eventId
    ) {
        User user = userService.getById(userId);
        Event event = eventService.getUserEvent(user, eventId);

        long views = statsTracker.viewsForEvent(event.getId());
        long confirmed = requestService.getConfirmedCountForEvent(event.getId());

        return eventMapper.toFullDto(event, views, confirmed);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(
            @PathVariable long userId,
            @PathVariable long eventId,
            @RequestBody @Valid UpdateEventUserRequest dto
    ) {
        User user = userService.getById(userId);

        Category category = (dto.category() == null)
                ? null
                : categoryService.findById(dto.category());

        Event patch = new Event();
        eventMapper.patchFromUser(dto, patch, category);

        Event updated = eventService.updateUserEvent(user, eventId, patch, dto.stateAction());

        long views = statsTracker.viewsForEvent(updated.getId());
        long confirmed = requestService.getConfirmedCountForEvent(updated.getId());

        return eventMapper.toFullDto(updated, views, confirmed);
    }
}