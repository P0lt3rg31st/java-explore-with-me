package ru.practicum.ewm.stats.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    // ====== ALL ======

    @Query("""
                select new ru.practicum.ewm.dto.stats.ViewStats(h.app, h.uri, count(h.id))
                from Hit h
                where h.timestamp >= :start and h.timestamp < :end
                group by h.app, h.uri
                order by count(h.id) desc
            """)
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("""
                select new ru.practicum.ewm.dto.stats.ViewStats(h.app, h.uri, count(distinct h.ip))
                from Hit h
                where h.timestamp >= :start and h.timestamp < :end
                group by h.app, h.uri
                order by count(distinct h.ip) desc
            """)
    List<ViewStats> findStatsUnique(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("""
                select new ru.practicum.ewm.dto.stats.ViewStats(h.app, h.uri, count(h.id))
                from Hit h
                where h.timestamp >= :start and h.timestamp < :end
                  and h.uri in :uris
                group by h.app, h.uri
                order by count(h.id) desc
            """)
    List<ViewStats> findStatsByUris(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    @Query("""
                select new ru.practicum.ewm.dto.stats.ViewStats(h.app, h.uri, count(distinct h.ip))
                from Hit h
                where h.timestamp >= :start and h.timestamp < :end
                  and h.uri in :uris
                group by h.app, h.uri
                order by count(distinct h.ip) desc
            """)
    List<ViewStats> findStatsUniqueByUris(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);
}