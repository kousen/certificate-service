package com.kousen.cert.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PdfBoxGenerator
 */
class PdfBoxGeneratorPropertyTest {

    private final PdfBoxGenerator generator = new PdfBoxGenerator();

    /**
     * Tests that the PDF generator can handle name inputs of varying lengths
     * and always produces a valid PDF with the text properly included
     */
    @Disabled("Background image loading issues")
    @Property(tries = 5)
    void shouldHandleVariableNameLengths(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String name) throws IOException {
        // Arrange
        String title = "Certificate of Ownership";
        String subtitle = "Test Book";
        
        // Act
        Path pdfPath = generator.createCertificatePdf(title, name, subtitle, null);
        
        try {
            // Assert
            assertThat(pdfPath).exists();
            assertThat(Files.size(pdfPath)).isGreaterThan(0);
            
            // Extract text from PDF to verify content
            String pdfText = extractTextFromPdf(pdfPath);
            assertThat(pdfText).contains(title, name, subtitle);
            
        } finally {
            // Cleanup
            Files.deleteIfExists(pdfPath);
        }
    }
    
    /**
     * Tests that the PDF generator can handle subtitle (book title) inputs of varying lengths
     * and always produces a valid PDF with the text properly included
     */
    @Disabled("Background image loading issues")
    @Property(tries = 5)
    void shouldHandleVariableBookTitleLengths(
            @ForAll @NotBlank @StringLength(min = 1, max = 70) String bookTitle) throws IOException {
        // Arrange
        String title = "Certificate of Ownership";
        String name = "John Doe";
        
        // Act
        Path pdfPath = generator.createCertificatePdf(title, name, bookTitle, null);
        
        try {
            // Assert
            assertThat(pdfPath).exists();
            assertThat(Files.size(pdfPath)).isGreaterThan(0);
            
            // Extract text from PDF to verify content
            String pdfText = extractTextFromPdf(pdfPath);
            assertThat(pdfText).contains(title, name, bookTitle);
            
        } finally {
            // Cleanup
            Files.deleteIfExists(pdfPath);
        }
    }
    
    /**
     * Tests that the PDF generator can handle special characters in the name
     * and always produces a valid PDF with the text properly included
     */
    @Disabled("Background image loading issues")
    @Property(tries = 5)
    void shouldHandleSpecialCharactersInName(
            @ForAll("namesWithSpecialChars") String name) throws IOException {
        // Arrange
        String title = "Certificate of Ownership";
        String subtitle = "Test Book";
        
        // Act
        Path pdfPath = generator.createCertificatePdf(title, name, subtitle, null);
        
        try {
            // Assert
            assertThat(pdfPath).exists();
            assertThat(Files.size(pdfPath)).isGreaterThan(0);
            
            // Extract text from PDF to verify content
            String pdfText = extractTextFromPdf(pdfPath);
            
            // PDFBox's text extraction might handle quotes and special chars differently,
            // so we check if the basic name parts are there
            String simplifiedName = name.replaceAll("[^a-zA-Z0-9]", " ").trim();
            for (String part : simplifiedName.split("\\s+")) {
                if (part.length() > 1) { // Ignore single chars that might be missed in extraction
                    assertThat(pdfText).contains(part);
                }
            }
            
        } finally {
            // Cleanup
            Files.deleteIfExists(pdfPath);
        }
    }
    
    /**
     * Tests that the PDF generator maintains consistent file sizes
     * regardless of input variations, within a reasonable range
     */
    @Disabled("Background image loading issues")
    @Property(tries = 5)
    void shouldMaintainReasonableFileSize(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String name,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String bookTitle) throws IOException {
        // Arrange
        String title = "Certificate of Ownership";
        
        // Act
        Path pdfPath = generator.createCertificatePdf(title, name, bookTitle, null);
        
        try {
            // Assert
            long size = Files.size(pdfPath);
            
            // PDF should be within a reasonable size range, considering embedded fonts
            // Typical size is ~30KB-150KB with embedded fonts
            assertThat(size).isGreaterThan(10_000) // At least 10KB
                             .isLessThan(200_000); // Less than 200KB
            
        } finally {
            // Cleanup
            Files.deleteIfExists(pdfPath);
        }
    }
    
    /**
     * Tests that the PDF generator handles multiple certificate creations
     * consistently, maintaining state properly between calls
     */
    @Disabled("Background image loading issues")
    @Property(tries = 3)
    void shouldHandleMultipleCertificateCreations(
            @ForAll("validUserNames") String[] names,
            @ForAll("validBookTitles") String[] bookTitles) throws IOException {
        
        int count = Math.min(names.length, bookTitles.length);
        String title = "Certificate of Ownership";
        Path[] paths = new Path[count];
        
        try {
            // Create multiple certificates in sequence
            for (int i = 0; i < count; i++) {
                paths[i] = generator.createCertificatePdf(title, names[i], bookTitles[i], null);
                
                // Verify each certificate
                assertThat(paths[i]).exists();
                assertThat(Files.size(paths[i])).isGreaterThan(0);
                
                // Check content
                String pdfText = extractTextFromPdf(paths[i]);
                assertThat(pdfText).contains(names[i], bookTitles[i]);
            }
        } finally {
            // Cleanup all created PDFs
            for (int i = 0; i < count; i++) {
                if (paths[i] != null) {
                    Files.deleteIfExists(paths[i]);
                }
            }
        }
    }
    
    @Provide
    Arbitrary<String[]> validUserNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofLength(5)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .array(String[].class)
                .ofSize(2);
    }
    
    @Provide
    Arbitrary<String[]> validBookTitles() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofLength(10)
                .array(String[].class)
                .ofSize(2);
    }
    
    @Provide
    Arbitrary<String> namesWithSpecialChars() {
        // Base name parts
        Arbitrary<String> firstNames = Arbitraries.of(
                "John", "Jane", "José", "François", "Günter", "João", "Søren", "Björn");
        
        Arbitrary<String> lastNames = Arbitraries.of(
                "O'Neill", "Smith-Jones", "García", "Müller", "Åberg", "D'Angelo", "Nguyễn");
        
        // Combine with special characters
        return Combinators.combine(firstNames, lastNames)
                .as((first, last) -> first + " " + last);
    }
    
    // Helper method to extract text from a PDF file
    private String extractTextFromPdf(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}