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
import java.util.List;
import java.util.Locale;

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
        return eventRepository.findAllByInitiatorIdWithOffset(user.getId(), from, size);
    }

    public Event getUserEvent(User user, long eventId) {
        validateNotNull(user);
        return eventRepository.findByIdAndInitiatorId(eventId, user.getId())
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

        return eventRepository.searchPublishedWithOffset(q, cats, categoriesApply, paid, start, rangeEnd, from, size);
    }

    public List<Event> searchPublishedWithinRadius(double centerLat, double centerLon, double radiusKm, int from, int size) {
        validatePagination(from, size);
        validateRadius(radiusKm);

        return eventRepository.searchPublishedWithinRadiusWithOffset(centerLat, centerLon, radiusKm, from, size);
    }

    public Event getPublishedEvent(long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event was not found."));
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

        return eventRepository.adminSearchWithOffset(
                usersParam, usersApply,
                statesParam, statesApply,
                catsParam, categoriesApply,
                rangeStart, rangeEnd,
                from, size
        );
    }

    @Transactional
    public Event adminUpdateEvent(long eventId, Event patch, AdminEventStateAction action) {
        Event existing = eventRepository.findById(eventId)
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
}