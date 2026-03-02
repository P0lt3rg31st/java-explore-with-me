package ru.practicum.ewm.dto.stats.date;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EwmDateTimeValidator.class)
public @interface EwmDateTime {
    String message() default "Invalid timestamp format. Expected: " + DateTimeFormats.EWM_PATTERN;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
