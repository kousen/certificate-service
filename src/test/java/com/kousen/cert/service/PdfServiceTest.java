package com.kousen.cert.service;

import com.kousen.cert.config.ServerUrlConfig;
import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PdfServiceTest {

    private ServerUrlConfig mockConfig;
    private QrCodeGenerator qrCodeGenerator;
    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        // Mock the server config
        mockConfig = Mockito.mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://localhost:8080");
        
        // Create real QR code generator with mock config
        qrCodeGenerator = new QrCodeGenerator(mockConfig);
        
        // Create PDF service with real QR generator
        pdfService = new PdfService(qrCodeGenerator);
    }

    @Test
    @Disabled("PDF generation needs fixing with PDFBox")
    void shouldCreatePdfWithCorrectContent() throws Exception {
        // Given
        CertificateRequest request = new CertificateRequest(
                "James Gosling",
                "Modern Java Recipes",
                Optional.of("john@example.com")
        );
        
        // When
        Path pdfPath = pdfService.createPdf(new ElegantTemplate(), request);
        
        // Then
        assertThat(pdfPath).isNotNull();
        assertThat(Files.exists(pdfPath)).isTrue();
        assertThat(Files.size(pdfPath)).isGreaterThan(0);
        
        // Extract text and analyze PDF structure
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            // Check text content
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            System.out.println("Extracted PDF Text: " + text);
            
            // Verify content
            assertThat(text).contains("Certificate of Ownership");
            assertThat(text).contains("James Gosling");
            assertThat(text).contains("Modern Java Recipes");
            
            // Check for images and fonts
            int imageCount = countImages(document);
            Set<String> fontNames = extractFontNames(document);
            
            System.out.println("Found " + imageCount + " images in PDF");
            System.out.println("Found fonts: " + fontNames);
            
            // Verify we have at least one image (should be QR code)
            assertThat(imageCount).isGreaterThan(0);
            
            // Verify we have fonts
            assertThat(fontNames).isNotEmpty();
        }
        
        // Clean up
        Files.deleteIfExists(pdfPath);
    }
    
    // Helper methods to check PDF structure
    
    private Set<String> extractFontNames(PDDocument document) throws Exception {
        Set<String> fontNames = new HashSet<>();

        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;

            for (org.apache.pdfbox.cos.COSName name : resources.getFontNames()) {
                PDFont font = resources.getFont(name);
                if (font != null) {
                    fontNames.add(font.getName());
                }
            }
        }

        return fontNames;
    }

    private int countImages(PDDocument document) throws Exception {
        int imageCount = 0;

        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;

            for (org.apache.pdfbox.cos.COSName name : resources.getXObjectNames()) {
                PDXObject xobject = resources.getXObject(name);
                if (xobject instanceof PDImageXObject) {
                    imageCount++;
                }
            }
        }

        return imageCount;
    }
}