package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PdfServiceTest {

    @Test
    void shouldCreatePdfWithCorrectContent() throws Exception {
        // Given
        PdfService service = new PdfService();
        CertificateRequest request = new CertificateRequest(
                "James Gosling",
                "Modern Java Recipes",
                Optional.of("john@example.com")
        );
        
        // When
        Path pdfPath = service.createPdf(new ElegantTemplate(), request);
        
        // Then
        assertThat(pdfPath).isNotNull();
        assertThat(Files.exists(pdfPath)).isTrue();
        assertThat(Files.size(pdfPath)).isGreaterThan(0);
        
        // Extract text from PDF
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify content
            assertThat(text).contains("Certificate of Ownership");
            assertThat(text).contains("James Goslin");
            assertThat(text).contains("Modern Java Recipes");
        }
        
        // Clean up
        Files.deleteIfExists(pdfPath);
    }
}