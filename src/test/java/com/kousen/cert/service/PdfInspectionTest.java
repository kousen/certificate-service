package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test generates a PDF and saves it to a file for manual inspection.
 * It's useful for debugging font and image issues.
 */
class PdfInspectionTest {

    @Test
    void generatePdfForInspection() throws Exception {
        // Given
        PdfService service = new PdfService();
        CertificateRequest request = new CertificateRequest(
                "Font Test User",
                "Modern Java Recipes",
                Optional.empty()
        );
        
        // When
        Path tempPdfPath = service.createPdf(new ElegantTemplate(), request);
        
        // Then
        assertThat(tempPdfPath).isNotNull();
        assertThat(Files.exists(tempPdfPath)).isTrue();
        
        // Save the PDF to a file in the project directory for manual inspection
        Path outputPath = Path.of("font-test.pdf");
        Files.copy(tempPdfPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("PDF saved to: " + outputPath.toAbsolutePath());
        System.out.println("Please open this file to manually check if fonts and background are visible.");
    }
}