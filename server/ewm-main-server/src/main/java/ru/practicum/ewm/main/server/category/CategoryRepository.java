package ru.practicum.ewm.main.server.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    // ===== Fetch helpers =====

    @Query("""
                select c
                from Category c
                where c.id in :ids
                order by c.id asc
            """)
    List<Category> findAllByIdInOrdered(@Param("ids") List<Long> ids);

    // ===== Admin/Public =====

    @Query(
            value = """
                    select c.id
                    from categories c
                    order by c.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<Long> findIdsWithOffset(@Param("from") int from,
                                 @Param("size") int size);
}