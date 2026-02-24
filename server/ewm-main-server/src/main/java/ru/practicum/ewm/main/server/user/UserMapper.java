package ru.practicum.ewm.main.server.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest dto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    List<UserDto> toDtoList(List<User> users);

    List<UserShortDto> toShortDtoList(List<User> users);
}