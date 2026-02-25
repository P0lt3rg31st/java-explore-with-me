package ru.practicum.ewm.main.server.event;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.main.server.category.Category;
import ru.practicum.ewm.main.server.category.CategoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.dto.stats.DateTimeFormats.EWM_PATTERN;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    private final CategoryService categoryService;
    private final StatsTracker statsTracker;

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false, name = "users") List<Long> userIds,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false, name = "categories") List<Long> categoryIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = EWM_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = EWM_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Event> events = eventService.adminSearch(
                userIds, states, categoryIds, rangeStart, rangeEnd, from, size
        );

        Map<Long, Long> viewsById = statsTracker.viewsForEvents(events.stream().map(Event::getId).toList());

        return events.stream()
                .map(e -> eventMapper.toFullDto(e, viewsById.getOrDefault(e.getId(), 0L), 0L))
                .toList();
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable long eventId,
            @RequestBody UpdateEventAdminRequest dto
    ) {
        Category category = (dto.category() == null)
                ? null
                : categoryService.findById(dto.category());

        Event patch = new Event();
        eventMapper.patchFromAdmin(dto, patch, category);

        Event updated = eventService.adminUpdateEvent(eventId, patch, dto.stateAction());

        long views = statsTracker.viewsForEvent(updated.getId());
        return eventMapper.toFullDto(updated, views, 0L);
    }
}