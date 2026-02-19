package ru.practicum.ewm.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.stats.DateTimeFormats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public class DateTimeMapper {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(DateTimeFormats.EWM_PATTERN);

    @Named("toLocalDateTime")
    public LocalDateTime toLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, FMT);
    }

    @Named("fromLocalDateTime")
    public String fromLocalDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(FMT);
    }
}
