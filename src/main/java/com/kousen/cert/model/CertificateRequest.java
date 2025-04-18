package com.kousen.cert.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

public record CertificateRequest(
        @NotBlank(message = "Purchaser name is required")
        String purchaserName,
        
        @NotBlank(message = "Book title is required")
        @ValidBookTitle(message = "Invalid book title")
        String bookTitle,
        
        Optional<@Email(message = "Invalid email format") String> purchaserEmail
) {
    // List of allowed book titles
    public static final List<String> ALLOWED_BOOK_TITLES = List.of(
            "Making Java Groovy",
            "Gradle Recipes for Android",
            "Modern Java Recipes",
            "Mockito Made Clear", 
            "Help Your Boss Help You",
            "Kotlin Cookbook"
    );
    
    // Custom validation exception that tracks the field name
    // Keeping this for backward compatibility with existing code
    public static class ValidationException extends IllegalArgumentException {
        private final String field;
        
        public ValidationException(String field, String message) {
            super(message);
            this.field = field;
        }
        
        public String getField() {
            return field;
        }
    }
}
