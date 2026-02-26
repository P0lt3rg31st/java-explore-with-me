package ru.practicum.ewm.main.server.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.server.event.EventMapper;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "pinned", source = "pinned", defaultValue = "false")
    Compilation toEntity(NewCompilationDto dto);
}