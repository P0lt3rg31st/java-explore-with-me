package ru.practicum.ewm.main.server.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", source = "status")
    ParticipationRequestDto toDto(ParticipationRequest request);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests);
}