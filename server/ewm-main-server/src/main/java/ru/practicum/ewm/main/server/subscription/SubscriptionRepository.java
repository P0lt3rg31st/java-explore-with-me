package ru.practicum.ewm.main.server.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @EntityGraph(attributePaths = {"subscriber", "target"})
    Page<Subscription> findAllBySubscriber_Id(long subscriberId, Pageable pageable);

    @EntityGraph(attributePaths = {"subscriber", "target"})
    Page<Subscription> findAllByTarget_Id(long targetId, Pageable pageable);

    int deleteBySubscriber_IdAndTarget_Id(long subscriberId, long targetId);
}