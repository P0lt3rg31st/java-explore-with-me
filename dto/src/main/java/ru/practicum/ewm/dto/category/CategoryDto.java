package ru.practicum.ewm.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDto(
        Long id,
        @NotBlank(message = "Category name must not be blank")
        @Size(max = 50, message = "Category name length must be <= 50")
        String name
) {
}