package ru.practicum.ewm.main.server.subscription;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query(value = """
            select s.id
            from subscriptions s
            where s.subscriber_id = :subscriberId
            order by s.id asc
            limit :size offset :from
            """, nativeQuery = true)
    List<Long> findIdsBySubscriberWithOffset(@Param("subscriberId") long subscriberId,
                                             @Param("from") int from,
                                             @Param("size") int size);

    @Query(value = """
            select s.id
            from subscriptions s
            where s.target_id = :targetId
            order by s.id asc
            limit :size offset :from
            """, nativeQuery = true)
    List<Long> findIdsByTargetWithOffset(@Param("targetId") long targetId,
                                         @Param("from") int from,
                                         @Param("size") int size);

    @EntityGraph(attributePaths = {"subscriber", "target"})
    List<Subscription> findAllByIdInOrderByIdAsc(List<Long> ids);

    int deleteBySubscriber_IdAndTarget_Id(long subscriberId, long targetId);
}