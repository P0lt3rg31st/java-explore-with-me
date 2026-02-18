package ru.practicum.ewm.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.server.Hit;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface HitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "toLocalDateTime")
    Hit toEntity(EndpointHit dto);
}
