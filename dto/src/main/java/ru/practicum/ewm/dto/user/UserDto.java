package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserDto(
        Long id,

        @NotBlank
        String name,

        @NotBlank
        String email
) {
}
