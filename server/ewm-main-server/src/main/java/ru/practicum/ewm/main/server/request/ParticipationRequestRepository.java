package ru.practicum.ewm.main.server.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.request.ParticipationRequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // ===== Fetch helpers =====

    @Query("""
            select r
            from ParticipationRequest r
            join fetch r.event
            join fetch r.requester
            where r.requester.id = :userId
            order by r.id asc
            """)
    List<ParticipationRequest> findAllByRequesterIdFetchAll(@Param("userId") long userId);

    @Query("""
            select r
            from ParticipationRequest r
            join fetch r.event
            join fetch r.requester
            where r.id = :requestId
              and r.requester.id = :userId
            """)
    Optional<ParticipationRequest> findByIdAndRequesterIdFetchAll(@Param("requestId") long requestId,
                                                                  @Param("userId") long userId);

    @Query("""
            select r
            from ParticipationRequest r
            join fetch r.event
            join fetch r.requester
            where r.event.id = :eventId
            order by r.id asc
            """)
    List<ParticipationRequest> findAllByEventIdFetchAll(@Param("eventId") long eventId);

    @Query("""
            select r
            from ParticipationRequest r
            join fetch r.event
            join fetch r.requester
            where r.event.id = :eventId
              and r.id in :ids
            order by r.id asc
            """)
    List<ParticipationRequest> findAllByEventIdAndIdInFetchAll(@Param("eventId") long eventId,
                                                               @Param("ids") List<Long> ids);

    // ===== Derived helpers =====

    boolean existsByEvent_IdAndRequester_Id(long eventId, long requesterId);

    long countByEvent_IdAndStatus(long eventId, ParticipationRequestStatus status);

    @Query("""
            select r.event.id as eventId, count(r.id) as cnt
            from ParticipationRequest r
            where r.status = :status
              and r.event.id in :eventIds
            group by r.event.id
            """)
    List<ConfirmedCount> countByEventIdsGrouped(@Param("eventIds") List<Long> eventIds,
                                                @Param("status") ParticipationRequestStatus status);
}