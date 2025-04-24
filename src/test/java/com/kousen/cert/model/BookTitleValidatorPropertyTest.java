package com.kousen.cert.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for BookTitleValidator using jqwik
 */
class BookTitleValidatorPropertyTest {

    private final BookTitleValidator validator = new BookTitleValidator();
    
    // No mock needed since we're just testing validation logic
    // The ConstraintValidatorContext is not used in the validator implementation
    
    @Example
    void nullValueIsAlwaysValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
    
    @Property
    void allAllowedBookTitlesAreValid(
            @ForAll("allowedBookTitles") String bookTitle) {
        assertThat(validator.isValid(bookTitle, null)).isTrue();
    }
    
    @Provide
    Arbitrary<String> allowedBookTitles() {
        return Arbitraries.of(CertificateRequest.ALLOWED_BOOK_TITLES.toArray(new String[0]));
    }
    
    @Example
    void titlesMustBeExactlyAsSpecified() {
        // For each allowed book title, ensure that a slight variation (with a character added)
        // is not valid
        for (String allowedTitle : CertificateRequest.ALLOWED_BOOK_TITLES) {
            String modified = allowedTitle + "X";
            assertThat(validator.isValid(modified, null)).isFalse();
        }
    }
    
    @Property
    void randomStringsAreInvalidUnlessTheyMatchAllowedTitles(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String randomString) {
        boolean shouldBeValid = CertificateRequest.ALLOWED_BOOK_TITLES.contains(randomString);
        assertThat(validator.isValid(randomString, null)).isEqualTo(shouldBeValid);
    }
    
    @Property
    void caseSensitivityMatters(
            @ForAll("allowedBookTitles") String bookTitle) {
        // Skip titles that don't have any uppercase letters
        if (bookTitle.toLowerCase().equals(bookTitle)) {
            return;
        }
        
        String lowercaseTitle = bookTitle.toLowerCase();
        assertThat(validator.isValid(lowercaseTitle, null)).isFalse();
    }
    
    @Property
    void whitespaceMatters(
            @ForAll("allowedBookTitles") String bookTitle) {
        // Skip titles that don't have spaces
        if (!bookTitle.contains(" ")) {
            return;
        }
        
        String noSpaceTitle = bookTitle.replace(" ", "");
        assertThat(validator.isValid(noSpaceTitle, null)).isFalse();
    }
}