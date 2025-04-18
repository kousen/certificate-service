package com.kousen.cert.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookTitleValidator implements ConstraintValidator<ValidBookTitle, String> {

    @Override
    public boolean isValid(String bookTitle, ConstraintValidatorContext context) {
        if (bookTitle == null) {
            return true; // Let @NotBlank handle null validation
        }
        
        return CertificateRequest.ALLOWED_BOOK_TITLES.contains(bookTitle);
    }
}