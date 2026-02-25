package ru.practicum.ewm.main.server.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ===== Private =====

    @Query(
            value = """
                    select *
                    from events e
                    where e.initiator_id = :userId
                    order by e.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<Event> findAllByInitiatorIdWithOffset(@Param("userId") long userId,
                                               @Param("from") int from,
                                               @Param("size") int size);

    Optional<Event> findByIdAndInitiatorId(long id, long initiatorId);

    // ===== Public=====
    @Query(
            value = """
                select *
                from events e
                where e.state = 'PUBLISHED'
                  and (:text is null
                       or (lower(e.annotation) like lower(concat('%', :text, '%'))
                           or lower(e.description) like lower(concat('%', :text, '%'))
                           or lower(e.title) like lower(concat('%', :text, '%'))))
                  and (:paid is null or e.paid = :paid)
                  and (e.event_date >= :rangeStart)
                  and (e.event_date <= coalesce(cast(:rangeEnd as timestamp), timestamp '9999-12-31 23:59:59'))
                  and (:categoriesApply = false or e.category_id in (:categoryIds))
                order by e.event_date asc
                limit :size offset :from
                """,
            nativeQuery = true
    )
    List<Event> searchPublishedWithOffset(@Param("text") String text,
                                          @Param("categoryIds") List<Long> categoryIds,
                                          @Param("categoriesApply") boolean categoriesApply,
                                          @Param("paid") Boolean paid,
                                          @Param("rangeStart") LocalDateTime rangeStart,
                                          @Param("rangeEnd") LocalDateTime rangeEnd,
                                          @Param("from") int from,
                                          @Param("size") int size);

    // ===== Public =====

    @Query(
            value = """
                    select *
                    from events e
                    where e.state = 'PUBLISHED'
                      and distance(e.lat, e.lon, :centerLat, :centerLon) <= :radiusKm
                    order by e.event_date asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<Event> searchPublishedWithinRadiusWithOffset(@Param("centerLat") double centerLat,
                                                      @Param("centerLon") double centerLon,
                                                      @Param("radiusKm") double radiusKm,
                                                      @Param("from") int from,
                                                      @Param("size") int size);

    Optional<Event> findByIdAndState(long id, EventState state);

    // ===== Admin =====
    @Query(
            value = """
                select *
                from events e
                where (:usersApply = false or e.initiator_id in (:userIds))
                  and (:statesApply = false or e.state in (:states))
                  and (:categoriesApply = false or e.category_id in (:categoryIds))
                  and (e.event_date >= coalesce(cast(:rangeStart as timestamp), timestamp '1970-01-01 00:00:00'))
                  and (e.event_date <= coalesce(cast(:rangeEnd   as timestamp), timestamp '9999-12-31 23:59:59'))
                order by e.id asc
                limit :size offset :from
                """,
            nativeQuery = true
    )
    List<Event> adminSearchWithOffset(@Param("userIds") List<Long> userIds,
                                      @Param("usersApply") boolean usersApply,
                                      @Param("states") List<String> states,
                                      @Param("statesApply") boolean statesApply,
                                      @Param("categoryIds") List<Long> categoryIds,
                                      @Param("categoriesApply") boolean categoriesApply,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd,
                                      @Param("from") int from,
                                      @Param("size") int size);
}