package ru.practicum.ewm.stats.server;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.DateTimeFormats;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.server.mapper.HitMapper;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final HitMapper hitMapper;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHit dto) {
        Hit hit = hitMapper.toEntity(dto);
        statsService.saveHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam
            @DateTimeFormat(pattern = DateTimeFormats.EWM_PATTERN)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(pattern = DateTimeFormats.EWM_PATTERN)
            LocalDateTime end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }
}
