package ru.practicum.ewm.main.server.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.AdminEventStateAction;
import ru.practicum.ewm.dto.event.EventState;
import ru.practicum.ewm.dto.event.UserEventStateAction;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.handler.exceptions.ConflictException;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;
import ru.practicum.ewm.main.server.category.Category;
import ru.practicum.ewm.main.server.user.User;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private static final int CREATE_MIN_HOURS = 2;
    private static final int PUBLISH_MIN_HOURS = 1;

    private final EventRepository eventRepository;

    // ===== Create =====

    @Transactional
    public Event create(Event event, User initiator, Category category) {
        validateNotNull(event, initiator, category);
        validateEventDateForCreateOrUserUpdate(event.getEventDate());

        setDefaultValues(event, initiator, category);

        return eventRepository.save(event);
    }

    // ===== Private =====

    public List<Event> getUserEvents(User user, int from, int size) {
        validateNotNull(user);
        validatePagination(from, size);

        List<Long> ids = eventRepository.findIdsByInitiatorIdWithOffset(user.getId(), from, size);
        return fetchWithCategoryPreserveOrder(ids);
    }

    public Event getUserEvent(User user, long eventId) {
        validateNotNull(user);

        return eventRepository.findByIdAndInitiatorIdFetchCategory(eventId, user.getId())
                .orElseThrow(() -> new NotFoundException("Event was not found."));
    }

    @Transactional
    public Event updateUserEvent(User user, long eventId, Event patch, UserEventStateAction action) {
        Event existing = getUserEvent(user, eventId);

        validateEventStateForUserUpdate(existing);
        applyPatch(existing, patch);

        if (patch != null && patch.getEventDate() != null) {
            validateEventDateForCreateOrUserUpdate(existing.getEventDate());
        }

        applyUserAction(existing, action);
        return eventRepository.save(existing);
    }

    // ===== Public =====

    public List<Event> searchPublished(String text, List<Long> categoryIds, Boolean paid,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        validatePagination(from, size);
        validateRange(rangeStart, rangeEnd);

        if (isFilterEmpty(categoryIds)) {
            return List.of();
        }

        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        String q = normalizeText(text);

        boolean categoriesApply = categoryIds != null;
        List<Long> cats = categoriesApply ? categoryIds : List.of(-1L);

        List<Long> ids = eventRepository.searchPublishedIdsWithOffset(
                q, cats, categoriesApply, paid, start, rangeEnd, from, size
        );

        return fetchWithCategoryPreserveOrder(ids);
    }

    public List<Event> searchPublishedWithinRadius(double centerLat, double centerLon, double radiusKm, int from, int size) {
        validatePagination(from, size);
        validateRadius(radiusKm);

        List<Long> ids = eventRepository.searchPublishedWithinRadiusIdsWithOffset(centerLat, centerLon, radiusKm, from, size);
        return fetchWithCategoryPreserveOrder(ids);
    }

    public Event getPublishedEvent(long eventId) {
        return eventRepository.findByIdAndStateFetchCategory(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event was not found."));
    }

    public List<Event> applyPublicAvailabilityAndSort(List<Event> events,
                                                      Boolean onlyAvailable,
                                                      String sort,
                                                      Map<Long, Long> viewsById,
                                                      Map<Long, Long> confirmedById) {
        validateNotNull(events, viewsById, confirmedById);
        validatePublicSort(sort);

        var stream = events.stream();

        if (Boolean.TRUE.equals(onlyAvailable)) {
            stream = stream.filter(e -> isAvailable(e, confirmedById));
        }

        if ("VIEWS".equalsIgnoreCase(sort)) {
            stream = stream.sorted(
                    Comparator.comparingLong((Event e) -> viewsById.getOrDefault(e.getId(), 0L)).reversed()
            );
        }

        if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparing(Event::getEventDate));
        }

        return stream.toList();
    }

    // ===== Admin =====

    public List<Event> adminSearch(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        validatePagination(from, size);
        validateRange(rangeStart, rangeEnd);

        if (isFilterEmpty(userIds) || isFilterEmpty(categoryIds) || isFilterEmpty(states)) {
            return List.of();
        }

        boolean usersApply = userIds != null;
        boolean categoriesApply = categoryIds != null;
        boolean statesApply = states != null;

        List<Long> usersParam = usersApply ? userIds : List.of(-1L);
        List<Long> catsParam = categoriesApply ? categoryIds : List.of(-1L);
        List<String> statesParam = prepareStatesParam(states, statesApply);

        if (statesApply && statesParam.isEmpty()) {
            return List.of();
        }

        List<Long> ids = eventRepository.adminSearchIdsWithOffset(
                usersParam, usersApply,
                statesParam, statesApply,
                catsParam, categoriesApply,
                rangeStart, rangeEnd,
                from, size
        );

        return fetchWithCategoryPreserveOrder(ids);
    }

    @Transactional
    public Event adminUpdateEvent(long eventId, Event patch, AdminEventStateAction action) {
        Event existing = eventRepository.findByIdFetchCategory(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found."));

        applyPatch(existing, patch);

        if (patch != null && patch.getEventDate() != null) {
            validateEventDateForCreateOrUserUpdate(existing.getEventDate());
        }

        applyAdminAction(existing, action);
        return eventRepository.save(existing);
    }
    // ===== Validation Helpers =====

    private void validateNotNull(Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                throw new BadRequestException("Incorrectly made request.");
            }
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }

    private void validateRadius(double radiusKm) {
        if (radiusKm < 0) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }

    private void validateEventDateForCreateOrUserUpdate(LocalDateTime eventDate) {
        if (eventDate == null || eventDate.isBefore(LocalDateTime.now().plusHours(CREATE_MIN_HOURS))) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }

    private void validateEventDateForPublish(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new ConflictException("Event date is null.");
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(PUBLISH_MIN_HOURS))) {
            throw new ConflictException("Event date must be at least 1 hour in the future to publish.");
        }
    }

    private void validateEventStateForUserUpdate(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed.");
        }
    }

    // ===== Logic Helpers =====

    private void setDefaultValues(Event event, User initiator, Category category) {
        event.setInitiator(initiator);
        event.setCategory(category);
        if (event.getState() == null) {
            event.setState(EventState.PENDING);
        }
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (event.getCreatedOn() == null) {
            event.setCreatedOn(LocalDateTime.now());
        }
    }

    private boolean isFilterEmpty(List<?> list) {
        return list != null && list.isEmpty();
    }

    private List<String> prepareStatesParam(List<String> states, boolean statesApply) {
        if (!statesApply) {
            return List.of("___");
        }
        return states.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .toList();
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.isBlank() ? null : t;
    }

    private void applyPatch(Event target, Event patch) {
        if (patch == null) {
            return;
        }
        if (patch.getAnnotation() != null) {
            target.setAnnotation(patch.getAnnotation());
        }
        if (patch.getDescription() != null) {
            target.setDescription(patch.getDescription());
        }
        if (patch.getTitle() != null) {
            target.setTitle(patch.getTitle());
        }
        if (patch.getEventDate() != null) {
            target.setEventDate(patch.getEventDate());
        }
        if (patch.getPaid() != null) {
            target.setPaid(patch.getPaid());
        }
        if (patch.getParticipantLimit() != null) {
            target.setParticipantLimit(patch.getParticipantLimit());
        }
        if (patch.getRequestModeration() != null) {
            target.setRequestModeration(patch.getRequestModeration());
        }
        if (patch.getCategory() != null) {
            target.setCategory(patch.getCategory());
        }
        if (patch.getLocation() != null) {
            target.setLocation(patch.getLocation());
        }
    }

    private void applyUserAction(Event event, UserEventStateAction action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
        }
    }

    private void applyAdminAction(Event event, AdminEventStateAction action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case PUBLISH_EVENT -> publish(event);
            case REJECT_EVENT -> reject(event);
        }
    }

    private void publish(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Event must be pending to be published.");
        }
        validateEventDateForPublish(event.getEventDate());
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void reject(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Published event cannot be rejected.");
        }
        event.setState(EventState.CANCELED);
    }

    private List<Event> fetchWithCategoryPreserveOrder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventRepository.findAllByIdInFetchCategory(ids);

        Map<Long, Integer> pos = new HashMap<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            pos.put(ids.get(i), i);
        }
        events.sort(Comparator.comparingInt(e -> pos.getOrDefault(e.getId(), Integer.MAX_VALUE)));
        return events;
    }

    private boolean isAvailable(Event e, Map<Long, Long> confirmedById) {
        int limit = e.getParticipantLimit();
        if (limit == 0) return true;
        long confirmed = confirmedById.getOrDefault(e.getId(), 0L);
        return confirmed < limit;
    }

    private void validatePublicSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return;
        }
        if (!"VIEWS".equalsIgnoreCase(sort) && !"EVENT_DATE".equalsIgnoreCase(sort)) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }
}