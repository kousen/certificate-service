package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PdfBoxGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateValidPdfCertificate() throws Exception {
        // Given
        PdfBoxGenerator generator = new PdfBoxGenerator();
        String title = "Certificate of Ownership";
        String name = "John Doe";
        String bookTitle = "Test Book Title";
        
        // Create a test QR code (just a sample image)
        Path qrCodePath = null;
        try {
            // Try to use one of the existing resources for testing
            var resource = new ClassPathResource("images/test-qr.png");
            if (resource.exists()) {
                qrCodePath = tempDir.resolve("test-qr.png");
                Files.copy(resource.getInputStream(), qrCodePath);
            }
        } catch (Exception e) {
            // If test resource doesn't exist, create a simple text file as placeholder
            qrCodePath = tempDir.resolve("test-qr.png");
            Files.writeString(qrCodePath, "Test QR placeholder");
        }
        
        // When
        Path pdfPath = generator.createCertificatePdf(title, name, bookTitle, qrCodePath);
        
        // Then
        assertThat(pdfPath).exists();
        assertThat(Files.size(pdfPath)).isGreaterThan(0);
        
        // Extract text to verify content
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            // Verify document properties
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            
            // Extract text
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify text content includes essential certificate elements
            assertThat(text).contains(title);
            assertThat(text).contains(name);
            assertThat(text).contains(bookTitle);
            assertThat(text).contains("This certifies that");
            assertThat(text).contains("is the proud owner of");
        }
        
        // Cleanup
        Files.deleteIfExists(pdfPath);
        Files.deleteIfExists(qrCodePath);
    }
    
    @Test
    void shouldHandleNullQrCode() throws Exception {
        // Given
        PdfBoxGenerator generator = new PdfBoxGenerator();
        String title = "Certificate of Ownership";
        String name = "Jane Smith";
        String bookTitle = "Another Test Book";
        
        // When - pass null QR code
        Path pdfPath = generator.createCertificatePdf(title, name, bookTitle, null);
        
        // Then - should still create a valid PDF
        assertThat(pdfPath).exists();
        assertThat(Files.size(pdfPath)).isGreaterThan(0);
        
        // Verify it's a valid PDF
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
        
        // Cleanup
        Files.deleteIfExists(pdfPath);
    }
}