package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test analyzes the PDF structure to check for fonts and images.
 */
class PdfFontTest {

    @Test
    @Disabled("PDF generation needs fixing with PDFBox")
    void analyzePdfStructure() throws Exception {
        // Given
        QrCodeGenerator qrCodeGenerator = MockQrCodeGenerator.createFullMock();
        PdfService service = new PdfService(qrCodeGenerator);
        CertificateRequest request = new CertificateRequest(
                "Font Test User",
                "Modern Java Recipes",
                Optional.empty()
        );
        
        // Let's print the HTML that will be rendered to PDF for debugging
        ElegantTemplate template = new ElegantTemplate();
        String html = template.html(request);
        System.out.println("\n----- HTML TO RENDER -----\n");
        System.out.println(html.substring(0, Math.min(html.length(), 500)) + "...");
        System.out.println("\n----- END HTML -----\n");
        
        // When
        Path pdfPath = service.createPdf(template, request);
        
        // Then
        assertThat(pdfPath).isNotNull();
        assertThat(Files.exists(pdfPath)).isTrue();
        
        System.out.println("[DEBUG_LOG] Created PDF at: " + pdfPath.toAbsolutePath());
        
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            // Analyze fonts
            Set<String> fontNames = new HashSet<>();
            Set<String> fontSubtypes = new HashSet<>();
            
            // Analyze images
            int imageCount = 0;
            
            // Process each page
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;
                
                // Extract font information
                for (COSName name : resources.getFontNames()) {
                    PDFont font = resources.getFont(name);
                    if (font != null) {
                        fontNames.add(font.getName());
                        fontSubtypes.add(font.getSubType());
                        System.out.println("[DEBUG_LOG] Font: " + font.getName() + " (Subtype: " + font.getSubType() + ")");
                    }
                }
                
                // Count images
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject) {
                        imageCount++;
                        System.out.println("[DEBUG_LOG] Image found: " + name.getName());
                    }
                }
            }
            
            System.out.println("[DEBUG_LOG] Total fonts found: " + fontNames.size());
            System.out.println("[DEBUG_LOG] Font names: " + fontNames);
            System.out.println("[DEBUG_LOG] Font subtypes: " + fontSubtypes);
            System.out.println("[DEBUG_LOG] Total images found: " + imageCount);
            
            // We expect at least one image (QR code)
            assertThat(imageCount).isGreaterThanOrEqualTo(1);
        }
        
        // Debug only - don't delete the PDF to allow manual inspection
        System.out.println("[DEBUG_LOG] PDF available for inspection at: " + pdfPath.toAbsolutePath());
    }
}