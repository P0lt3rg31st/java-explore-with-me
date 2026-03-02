package ru.practicum.ewm.main.server.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query(value = """
            select *
            from categories
            order by id asc
            limit :size offset :from
            """, nativeQuery = true)
    List<Category> findAllWithOffset(@Param("from") int from,
                                     @Param("size") int size);
}