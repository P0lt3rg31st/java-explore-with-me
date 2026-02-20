package ru.practicum.ewm.stats.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewStatsMapper {

    ViewStats toDto(StatsView view);

    List<ViewStats> toDtoList(List<StatsView> views);
}
