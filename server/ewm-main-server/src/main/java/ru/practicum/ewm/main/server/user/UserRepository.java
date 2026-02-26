package ru.practicum.ewm.main.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            value = """
                    select *
                    from users u
                    order by u.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<User> findAllWithOffset(@Param("from") int from,
                                 @Param("size") int size);

    @Query(
            value = """
                    select *
                    from users u
                    where u.id in (:ids)
                    order by u.id asc
                    limit :size offset :from
                    """,
            nativeQuery = true
    )
    List<User> findByIdsWithOffset(@Param("ids") List<Long> ids,
                                   @Param("from") int from,
                                   @Param("size") int size);
}