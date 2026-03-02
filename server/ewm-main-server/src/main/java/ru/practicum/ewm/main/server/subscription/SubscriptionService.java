package ru.practicum.ewm.main.server.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.handler.exceptions.ConflictException;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;
import ru.practicum.ewm.main.server.user.User;
import ru.practicum.ewm.main.server.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    // ===== Create =====

    @Transactional
    public Subscription subscribe(long subscriberId, long targetId) {
        validateNotSelf(subscriberId, targetId);

        User subscriber = requireUser(subscriberId);
        User target = requireUser(targetId);

        Subscription subscription = Subscription.builder()
                .subscriber(subscriber)
                .target(target)
                .createdOn(LocalDateTime.now())
                .build();

        try {
            return subscriptionRepository.save(subscription);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Subscription already exists for subscriberId=" + subscriberId + " targetId=" + targetId
            );
        }
    }

    // ===== Delete =====

    @Transactional
    public void unsubscribe(long subscriberId, long targetId) {
        validateNotSelf(subscriberId, targetId);
        requireUser(subscriberId);
        requireUser(targetId);

        int deleted = subscriptionRepository.deleteBySubscriber_IdAndTarget_Id(subscriberId, targetId);
        if (deleted == 0) {
            throw new NotFoundException(
                    "Subscription subscriberId=" + subscriberId + " targetId=" + targetId + " was not found"
            );
        }
    }

    // ===== Read =====

    public List<Subscription> findMySubscriptions(long subscriberId, int from, int size) {
        validatePagination(from, size);
        requireUser(subscriberId);

        List<Long> ids = subscriptionRepository.findIdsBySubscriberWithOffset(subscriberId, from, size);
        if (ids.isEmpty()) {
            return List.of();
        }
        return subscriptionRepository.findAllByIdInOrderByIdAsc(ids);
    }

    public List<Subscription> findMySubscribers(long targetId, int from, int size) {
        validatePagination(from, size);
        requireUser(targetId);

        List<Long> ids = subscriptionRepository.findIdsByTargetWithOffset(targetId, from, size);
        if (ids.isEmpty()) {
            return List.of();
        }
        return subscriptionRepository.findAllByIdInOrderByIdAsc(ids);
    }

    // ===== Helpers =====

    private void validateNotSelf(long subscriberId, long targetId) {
        if (subscriberId == targetId) {
            throw new BadRequestException("Subscriber cannot subscribe to himself.");
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be >= 0.");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be > 0.");
        }
    }

    private User requireUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }
}