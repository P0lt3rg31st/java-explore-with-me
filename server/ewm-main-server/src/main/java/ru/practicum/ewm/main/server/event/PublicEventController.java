package ru.practicum.ewm.main.server.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.main.server.request.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.dto.stats.DateTimeFormats.EWM_PATTERN;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;
    private final StatsTracker statsTracker;
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<EventShortDto> getEvents(
            HttpServletRequest request,
            @RequestParam(required = false) String text,
            @RequestParam(required = false, name = "categories") List<Long> categoryIds,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = EWM_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = EWM_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        statsTracker.countHit(request);

        List<Event> events = eventService.searchPublished(
                text, categoryIds, paid, rangeStart, rangeEnd, from, size
        );

        List<Long> ids = events.stream().map(Event::getId).toList();

        Map<Long, Long> viewsById = statsTracker.viewsForEvents(ids);
        Map<Long, Long> confirmedById = requestService.getConfirmedCountsForEvents(ids);
        List<Event> prepared = eventService.applyPublicAvailabilityAndSort(
                events, onlyAvailable, sort, viewsById, confirmedById
        );

        return prepared.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        viewsById.getOrDefault(e.getId(), 0L),
                        confirmedById.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(HttpServletRequest request, @PathVariable long id) {
        statsTracker.countHit(request);

        Event event = eventService.getPublishedEvent(id);
        long views = statsTracker.viewsForEvent(id);
        long confirmed = requestService.getConfirmedCountForEvent(id);

        return eventMapper.toFullDto(event, views, confirmed);
    }
}