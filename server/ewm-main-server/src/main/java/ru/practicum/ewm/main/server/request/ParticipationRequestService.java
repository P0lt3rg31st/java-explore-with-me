package ru.practicum.ewm.main.server.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.state.EventState;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.handler.exceptions.ConflictException;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;
import ru.practicum.ewm.dto.request.ParticipationRequestStatus;
import ru.practicum.ewm.main.server.event.EventRepository;
import ru.practicum.ewm.main.server.event.model.Event;
import ru.practicum.ewm.main.server.user.User;
import ru.practicum.ewm.main.server.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<ParticipationRequest> getUserRequests(long userId) {
        ensureUserExists(userId);
        return requestRepository.findAllByRequesterIdFetchAll(userId);
    }

    // ===== Private =====

    @Transactional
    public ParticipationRequest addParticipationRequest(long userId, long eventId) {
        ensureUserExists(userId);

        Event event = eventRepository.findByIdFetchCategory(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("Initiator cannot add request to participate in own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event must be published");
        }
        if (requestRepository.existsByEvent_IdAndRequester_Id(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        int limit = event.getParticipantLimit();
        if (limit > 0) {
            long confirmed = requestRepository.countByEvent_IdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            if (confirmed >= limit) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        User requester = userRepository.getReferenceById(userId);

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(initialStatus(event))
                .build();

        return requestRepository.save(request);
    }

    @Transactional
    public ParticipationRequest cancelRequest(long userId, long requestId) {
        ensureUserExists(userId);

        ParticipationRequest request = requestRepository.findByIdAndRequesterIdFetchAll(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        request.setStatus(ParticipationRequestStatus.CANCELED);
        return requestRepository.save(request);
    }

    public List<ParticipationRequest> getEventRequests(long userId, long eventId) {
        ensureUserExists(userId);

        Event event = getOwnedEventOrThrow(userId, eventId);
        return requestRepository.findAllByEventIdFetchAll(event.getId());
    }

    // ===== Private =====

    @Transactional
    public StatusUpdateResult updateRequestStatuses(long userId,
                                                    long eventId,
                                                    Set<Long> requestIds,
                                                    ParticipationRequestStatus targetStatus) {
        ensureUserExists(userId);

        if (targetStatus != ParticipationRequestStatus.CONFIRMED && targetStatus != ParticipationRequestStatus.REJECTED) {
            throw new BadRequestException("Unsupported status: " + targetStatus);
        }

        Event event = getOwnedEventOrThrow(userId, eventId);

        List<Long> ids = new ArrayList<>(requestIds);
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdInFetchAll(eventId, ids);

        Set<Long> found = requests.stream().map(ParticipationRequest::getId).collect(Collectors.toSet());
        Set<Long> missing = new HashSet<>(requestIds);
        missing.removeAll(found);
        if (!missing.isEmpty()) {
            throw new NotFoundException("Request(s) not found: " + missing);
        }

        boolean hasNotPending = requests.stream().anyMatch(r -> r.getStatus() != ParticipationRequestStatus.PENDING);
        if (hasNotPending) {
            throw new ConflictException("Request must have status PENDING");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        if (targetStatus == ParticipationRequestStatus.REJECTED) {
            for (ParticipationRequest r : requests) {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(r);
            }
            requestRepository.saveAll(requests);
            return new StatusUpdateResult(confirmed, rejected);
        }

        int limit = event.getParticipantLimit();
        boolean moderationOff = !event.getRequestModeration();

        if (limit == 0 || moderationOff) {
            for (ParticipationRequest r : requests) {
                r.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmed.add(r);
            }
            requestRepository.saveAll(requests);
            return new StatusUpdateResult(confirmed, rejected);
        }

        long alreadyConfirmed = requestRepository.countByEvent_IdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        long available = limit - alreadyConfirmed;
        if (available <= 0) {
            throw new ConflictException("The participant limit has been reached");
        }

        for (ParticipationRequest r : requests) {
            if (available > 0) {
                r.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmed.add(r);
                available--;
            } else {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        requestRepository.saveAll(requests);
        return new StatusUpdateResult(confirmed, rejected);
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    // ===== Helpers =====

    private Event getOwnedEventOrThrow(long userId, long eventId) {
        Event event = eventRepository.findByIdFetchCategory(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId() != userId) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return event;
    }

    private ParticipationRequestStatus initialStatus(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return ParticipationRequestStatus.CONFIRMED;
        }
        return ParticipationRequestStatus.PENDING;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedCountsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return requestRepository.countByEventIdsGrouped(eventIds, ParticipationRequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        ConfirmedCount::getEventId,
                        ConfirmedCount::getCnt
                ));
    }

    @Transactional(readOnly = true)
    public long getConfirmedCountForEvent(long eventId) {
        return requestRepository.countByEvent_IdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
    }

    public record StatusUpdateResult(List<ParticipationRequest> confirmed,
                                     List<ParticipationRequest> rejected) {
    }
}