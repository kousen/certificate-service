package com.kousen.cert.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validator annotation for ensuring book titles are valid.
 * The groups and payload parameters are required by the Bean Validation spec,
 * though typically unused in basic validation scenarios.
 */
@Documented
@Constraint(validatedBy = BookTitleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookTitle {
    String message() default "Invalid book title";
    
    // Default empty arrays are required by Bean Validation spec
    @SuppressWarnings("unused")
    Class<?>[] groups() default {};
    
    @SuppressWarnings("unused")
    Class<? extends Payload>[] payload() default {};
}