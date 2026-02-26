package ru.practicum.ewm.dto.compilation;

import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.List;

public record CompilationDto(
        long id,
        String title,
        boolean pinned,
        List<EventShortDto> events
) {
}