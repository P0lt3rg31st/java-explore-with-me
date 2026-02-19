package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.stats.DateTimeFormats;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(DateTimeFormats.EWM_PATTERN);

    private final StatsFeign feign;

    public void saveHit(EndpointHit hit) {
        if (hit == null) {
            throw new IllegalArgumentException("hit is null");
        }
        feign.saveHit(hit);
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start/end must not be null");
        }

        String startStr = start.format(FMT);
        String endStr = end.format(FMT);

        List<String> urisParam = (uris == null || uris.isEmpty()) ? null : uris;

        return feign.getStats(startStr, endStr, urisParam, unique);
    }
}