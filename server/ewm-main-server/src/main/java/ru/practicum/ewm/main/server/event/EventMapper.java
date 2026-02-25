package ru.practicum.ewm.main.server.event;

import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;



import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.main.server.category.Category;
import ru.practicum.ewm.main.server.user.User;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {

    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventFullDto toFullDto(Event event, long views, long confirmedRequests);

    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventShortDto toShortDto(Event event, long views, long confirmedRequests);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", constant = "PENDING")
    Event toEntity(NewEventDto dto, Category category, User initiator);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "category")
    void patchFromUser(UpdateEventUserRequest dto, @MappingTarget Event event, Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "category")
    void patchFromAdmin(UpdateEventAdminRequest dto, @MappingTarget Event event, Category category);

    CategoryDto toCategoryDto(Category category);

    UserShortDto toUserShortDto(User user);
}