package ru.practicum.ewm.main.server.subscription;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/subscriptions")
public class SubscriptionPrivateController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto subscribe(@PathVariable long userId,
                                     @RequestParam @Positive long targetId) {
        Subscription subscription = subscriptionService.subscribe(userId, targetId);
        return subscriptionMapper.toDto(subscription);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable long userId,
                            @RequestParam @Positive long targetId) {
        subscriptionService.unsubscribe(userId, targetId);
    }

    @GetMapping
    public List<SubscriptionDto> getMySubscriptions(@PathVariable long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                    @RequestParam(defaultValue = "10") @Positive int size) {
        return subscriptionMapper.toDtoList(subscriptionService.findMySubscriptions(userId, from, size));
    }

    @GetMapping("/subscribers")
    public List<SubscriptionDto> getMySubscribers(@PathVariable long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        return subscriptionMapper.toDtoList(subscriptionService.findMySubscribers(userId, from, size));
    }
}