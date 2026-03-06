package ru.practicum.ewm.main.server.subscription;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;
import ru.practicum.ewm.main.server.user.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {

    SubscriptionDto toDto(Subscription subscription);

    List<SubscriptionDto> toDtoList(List<Subscription> subscriptions);
}