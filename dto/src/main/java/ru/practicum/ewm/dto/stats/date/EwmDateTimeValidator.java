package ru.practicum.ewm.dto.stats.date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EwmDateTimeValidator implements ConstraintValidator<EwmDateTime, String> {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(DateTimeFormats.EWM_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            LocalDateTime.parse(value, FMT);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
