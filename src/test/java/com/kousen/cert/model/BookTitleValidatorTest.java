package com.kousen.cert.model;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BookTitleValidatorTest {

    private BookTitleValidator validator;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new BookTitleValidator();
    }
    
    @Test
    void shouldValidateNullTitle() {
        // Null should be considered valid (rely on @NotBlank for this)
        assertThat(validator.isValid(null, context)).isTrue();
    }
    
    @ParameterizedTest
    @MethodSource("validBookTitles")
    void shouldValidateAllowedTitles(String bookTitle) {
        assertThat(validator.isValid(bookTitle, context)).isTrue();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Invalid Book Title",
        "Java For Dummies",
        "Unknown Book",
        "Random Title"
    })
    void shouldRejectInvalidTitles(String bookTitle) {
        assertThat(validator.isValid(bookTitle, context)).isFalse();
    }
    
    @Test
    void shouldMatchExactTitleWithCorrectCase() {
        // Case matters
        assertThat(validator.isValid("making java groovy", context)).isFalse();
        assertThat(validator.isValid("Making Java Groovy", context)).isTrue();
    }
    
    // Helper method to return the same titles defined in CertificateRequest
    static Stream<String> validBookTitles() {
        return CertificateRequest.ALLOWED_BOOK_TITLES.stream();
    }
}