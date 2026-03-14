package com.kousen.cert.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfVerificationServiceTest {

    private PdfVerificationService pdfVerificationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfVerificationService = new PdfVerificationService();
    }

    @Test
    @DisplayName("should return false when verifying PDF without signatures")
    void shouldReturnFalseWhenVerifyingPdfWithoutSignatures() throws Exception {
        Path pdfPath = createSimplePdf();
        assertFalse(pdfVerificationService.verifySignature(pdfPath));
    }

    @Test
    @DisplayName("should perform biometric analysis on PDF with image")
    void shouldPerformBiometricAnalysisOnPdfWithImage() throws Exception {
        Path pdfPath = createPdfWithImage();
        Map<String, Object> analysis = pdfVerificationService.performBiometricAnalysis(pdfPath);

        assertEquals("VALIDATED", analysis.get("status"));
        assertTrue((Double) analysis.get("stylometricConfidence") > 0.98);
        assertNotNull(analysis.get("biometricId"));
        assertTrue(analysis.get("biometricId").toString().startsWith("STYL-"));
    }

    @Test
    @DisplayName("should return IMAGE_NOT_FOUND when performing biometric analysis on PDF without images")
    void shouldReturnImageNotFoundWhenPerformingBiometricAnalysisOnPdfWithoutImages() throws Exception {
        Path pdfPath = createSimplePdf();
        Map<String, Object> analysis = pdfVerificationService.performBiometricAnalysis(pdfPath);

        assertEquals("IMAGE_NOT_FOUND", analysis.get("status"));
        assertEquals(0.0, analysis.get("stylometricConfidence"));
    }

    private Path createSimplePdf() throws Exception {
        Path pdfPath = tempDir.resolve("simple.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdfPath.toFile());
        }
        return pdfPath;
    }

    private Path createPdfWithImage() throws Exception {
        Path pdfPath = tempDir.resolve("with_image.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // Create a small test image
            BufferedImage bimg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            PDImageXObject image = LosslessFactory.createFromImage(document, bimg);
            
            PDResources resources = page.getResources();
            if (resources == null) {
                resources = new PDResources();
                page.setResources(resources);
            }
            resources.add(image);
            
            document.save(pdfPath.toFile());
        }
        return pdfPath;
    }
}
