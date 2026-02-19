package ru.practicum.ewm.stats.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    public void saveHit(Hit hit) {
        validateHit(hit);
        statsRepository.save(hit);
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {

        validateRange(start, end);
        validateUris(uris);

        if (unique) {
            return fetchUnique(start, end, uris);
        }
        return fetchAll(start, end, uris);
    }

    // ===== helpers =====

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new BadRequestException("start must not be null");
        }
        if (end == null) {
            throw new BadRequestException("end must not be null");
        }
        if (start.isAfter(end)) {
            throw new BadRequestException("start must be <= end");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("end must be >= start");
        }
    }

    private void validateUris(List<String> uris) {
        if (uris == null) {
            return;
        }
        boolean hasInvalid = uris.stream().anyMatch(u -> u == null || u.isBlank());
        if (hasInvalid) {
            throw new BadRequestException("uris must not contain null or blank values");
        }
    }

    private void validateHit(Hit hit) {
        if (hit == null) {
            throw new BadRequestException("hit must not be null");
        }
        if (hit.getTimestamp() == null) {
            throw new BadRequestException("timestamp must not be null");
        }
        if (hit.getApp() == null || hit.getApp().isBlank()) {
            throw new BadRequestException("app must not be blank");
        }
        if (hit.getUri() == null || hit.getUri().isBlank()) {
            throw new BadRequestException("uri must not be blank");
        }
        if (hit.getIp() == null || hit.getIp().isBlank()) {
            throw new BadRequestException("ip must not be blank");
        }
    }

    private boolean hasUris(List<String> uris) {
        return uris != null && !uris.isEmpty();
    }

    private List<ViewStats> fetchUnique(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return hasUris(uris)
                ? statsRepository.findStatsUniqueByUris(start, end, uris)
                : statsRepository.findStatsUnique(start, end);
    }

    private List<ViewStats> fetchAll(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return hasUris(uris)
                ? statsRepository.findStatsByUris(start, end, uris)
                : statsRepository.findStats(start, end);
    }
}
