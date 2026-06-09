package com.kousen.cert.controller;

import com.kousen.cert.model.CertificateRequest.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldBuildProblemDetailForMethodArgumentNotValid() {
        // Given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "certificateRequest");
        bindingResult.addError(new FieldError(
                "certificateRequest", "bookTitle", "Book title must be one of the allowed titles"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ProblemDetail detail = handler.handleValidationExceptions(ex);

        // Then
        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getTitle()).isEqualTo("Validation Error");
        assertThat(detail.getProperties()).containsKey("timestamp");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) detail.getProperties().get("errors");
        assertThat(errors).containsEntry("bookTitle", "Book title must be one of the allowed titles");
    }

    @Test
    void shouldBuildProblemDetailForConstraintViolation() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = new Path() {
            @Override
            public Iterator<Node> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public String toString() {
                return "create.request.purchaserName";
            }
        };
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");
        var ex = new ConstraintViolationException("Validation failed", Set.of(violation));

        // When
        ProblemDetail detail = handler.handleConstraintViolation(ex);

        // Then
        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getTitle()).isEqualTo("Validation Error");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) detail.getProperties().get("errors");
        assertThat(errors).containsEntry("purchaserName", "must not be blank");
    }

    @Test
    void shouldBuildProblemDetailForCustomValidationException() {
        // Given
        var ex = new ValidationException("bookTitle", "Unknown book title");

        // When
        ProblemDetail detail = handler.handleValidationException(ex);

        // Then
        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getTitle()).isEqualTo("Validation Error");
        assertThat(detail.getDetail()).isEqualTo("Unknown book title");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) detail.getProperties().get("errors");
        assertThat(errors).containsEntry("bookTitle", "Unknown book title");
    }

    @Test
    void shouldBuildProblemDetailForIllegalArgument() {
        // Given
        var ex = new IllegalArgumentException("Something was wrong with the request");

        // When
        ProblemDetail detail = handler.handleIllegalArgumentException(ex);

        // Then
        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getTitle()).isEqualTo("Bad Request");
        assertThat(detail.getDetail()).isEqualTo("Something was wrong with the request");
        assertThat(detail.getProperties()).containsKey("timestamp");
    }
}
