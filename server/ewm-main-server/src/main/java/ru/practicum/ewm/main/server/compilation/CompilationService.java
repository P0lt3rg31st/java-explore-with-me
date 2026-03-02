package ru.practicum.ewm.main.server.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;
import ru.practicum.ewm.main.server.event.model.Event;
import ru.practicum.ewm.main.server.event.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    // ===== Public =====

    public List<Compilation> getPublic(Boolean pinned, int from, int size) {
        List<Long> ids = compilationRepository.findIdsWithOffset(pinned, from, size);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Compilation> compilations = compilationRepository.findAllByIdInFetchEvents(ids);
        warmUpEventRefs(compilations);
        return compilations;
    }

    public Compilation getById(long compId) {
        Compilation compilation = getCompilationWithEventsOrThrow(compId);
        warmUpEventRefs(List.of(compilation));
        return compilation;
    }

    // ===== Admin =====

    @Transactional
    public Compilation create(Compilation compilation, Set<Long> eventIds) {
        setEventsIfProvided(compilation, eventIds);
        return saveReloadAndWarmUp(compilation);
    }

    @Transactional
    public Compilation update(long compId, String title, Boolean pinned, Set<Long> eventIds) {
        Compilation compilation = getCompilationOrThrow(compId);

        if (title != null) {
            compilation.setTitle(title);
        }
        if (pinned != null) {
            compilation.setPinned(pinned);
        }
        setEventsIfProvided(compilation, eventIds);

        return saveReloadAndWarmUp(compilation);
    }

    @Transactional
    public void delete(long compId) {
        ensureCompilationExists(compId);
        compilationRepository.deleteById(compId);
    }

    // ===== Helpers =====

    private Compilation saveReloadAndWarmUp(Compilation compilation) {
        Compilation saved = compilationRepository.save(compilation);

        Compilation result = getCompilationWithEventsOrThrow(saved.getId());
        warmUpEventRefs(List.of(result));

        return result;
    }

    private Compilation getCompilationOrThrow(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private Compilation getCompilationWithEventsOrThrow(long compId) {
        return compilationRepository.findByIdFetchEvents(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private void ensureCompilationExists(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
    }

    private void setEventsIfProvided(Compilation compilation, Set<Long> eventIds) {
        if (eventIds != null) {
            compilation.setEvents(fetchEventsOrThrow(eventIds));
        }
    }

    // avoid N + 1
    // avoid LazyInitializationException
    private void warmUpEventRefs(Collection<Compilation> compilations) {
        Set<Long> eventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .collect(Collectors.toSet());

        if (!eventIds.isEmpty()) {
            eventRepository.findAllByIdInFetchCategory(new ArrayList<>(eventIds));
        }
    }

    private Set<Event> fetchEventsOrThrow(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Long> ids = new ArrayList<>(eventIds);
        List<Event> events = eventRepository.findAllByIdInFetchCategory(ids);

        Set<Long> found = events.stream().map(Event::getId).collect(Collectors.toSet());
        Set<Long> missing = new HashSet<>(eventIds);
        missing.removeAll(found);

        if (!missing.isEmpty()) {
            throw new NotFoundException("Event(s) not found: " + missing);
        }

        events.sort(Comparator.comparingLong(Event::getId));
        return new LinkedHashSet<>(events);
    }
}