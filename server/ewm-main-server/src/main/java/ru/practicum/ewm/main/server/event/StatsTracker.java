package ru.practicum.ewm.main.server.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.stats.DateTimeFormats;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsTracker {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(DateTimeFormats.EWM_PATTERN);

    private final StatsClient statsClient;

    @Value("${app.name:ewm-main-service}")
    private String appName;

    public void hit(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        try {
            String ip = request.getRemoteAddr();
            String uri = request.getRequestURI();
            String timestamp = LocalDateTime.now().format(FMT);

            statsClient.saveHit(new EndpointHit(appName, uri, ip, timestamp));
        } catch (Exception ex) {
            log.warn("Stats hit failed: {}", ex.getMessage());
        }
    }

    public long viewsForEvent(long eventId) {
        return viewsForEvents(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    public Map<Long, Long> viewsForEvents(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = eventIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);

        List<String> uris = ids.stream()
                .map(this::eventUri)
                .toList();

        List<ViewStats> stats;
        try {
            stats = statsClient.getStats(start, end, uris, true);
        } catch (Exception ex) {
            log.warn("Stats getStats failed: {}", ex.getMessage());
            return ids.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }

        Map<String, Long> uriToHits = new HashMap<>();
        for (ViewStats vs : stats) {
            uriToHits.put(vs.uri(), vs.hits());
        }

        Map<Long, Long> result = new HashMap<>();
        for (Long id : ids) {
            result.put(id, uriToHits.getOrDefault(eventUri(id), 0L));
        }
        return result;
    }

    private String eventUri(long eventId) {
        return "/events/" + eventId;
    }
}