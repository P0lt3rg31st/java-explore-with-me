package ru.practicum.ewm.main.server.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.server.compilation.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    // ===== Fetch helpers =====

    @Query("""
            select distinct c
            from Compilation c
            left join fetch c.events
            where c.id in :ids
            order by c.id asc
            """)
    List<Compilation> findAllByIdInFetchEvents(@Param("ids") List<Long> ids);

    @Query("""
            select distinct c
            from Compilation c
            left join fetch c.events
            where c.id = :id
            """)
    Optional<Compilation> findByIdFetchEvents(@Param("id") long id);

    // ===== Public =====

    @Query(
            value = """
                    select c.id
                    from compilations c
                    where (:pinned is null or c.pinned = :pinned)
                    order by c.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<Long> findIdsWithOffset(@Param("pinned") Boolean pinned,
                                 @Param("from") int from,
                                 @Param("size") int size);
}