package ru.practicum.ewm.main.server.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.event.state.EventState;
import ru.practicum.ewm.main.server.event.model.Event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findAllByIdIn(Collection<Long> ids);

    default List<Event> findAllByIdInFetchCategory(List<Long> ids) {
        return findAllByIdIn(ids);
    }

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findById(Long id);

    default Optional<Event> findByIdFetchCategory(long id) {
        return findById(id);
    }

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndInitiator_Id(Long id, Long initiatorId);

    default Optional<Event> findByIdAndInitiatorIdFetchCategory(long id, long initiatorId) {
        return findByIdAndInitiator_Id(id, initiatorId);
    }

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    default Optional<Event> findByIdAndStateFetchCategory(long id, EventState state) {
        return findByIdAndState(id, state);
    }

    // ===== Private =====

    @Query(
            value = """
                    select e.id
                    from events e
                    where e.initiator_id = :userId
                    order by e.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<Long> findIdsByInitiatorIdWithOffset(@Param("userId") long userId,
                                              @Param("from") int from,
                                              @Param("size") int size);

    // ===== Public =====

    @Query(
            value = """
                    select e.id
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
    List<Long> searchPublishedIdsWithOffset(@Param("text") String text,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            @Param("categoriesApply") boolean categoriesApply,
                                            @Param("paid") Boolean paid,
                                            @Param("rangeStart") LocalDateTime rangeStart,
                                            @Param("rangeEnd") LocalDateTime rangeEnd,
                                            @Param("from") int from,
                                            @Param("size") int size);

    @Query(
            value = """
                    select e.id
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
                    """,
            nativeQuery = true
    )
    List<Long> searchPublishedIds(@Param("text") String text,
                                  @Param("categoryIds") List<Long> categoryIds,
                                  @Param("categoriesApply") boolean categoriesApply,
                                  @Param("paid") Boolean paid,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd);

    // ===== Admin =====

    @Query(
            value = """
                    select e.id
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
    List<Long> adminSearchIdsWithOffset(@Param("userIds") List<Long> userIds,
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