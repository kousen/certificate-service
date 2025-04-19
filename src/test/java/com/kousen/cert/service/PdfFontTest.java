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
import org.junit.jupiter.api.Test;

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
    void analyzePdfStructure() throws Exception {
        // Given
        PdfService service = new PdfService();
        CertificateRequest request = new CertificateRequest(
                "Font Test User",
                "Modern Java Recipes",
                Optional.empty()
        );
        
        // When
        Path pdfPath = service.createPdf(new ElegantTemplate(), request);
        
        // Then
        assertThat(pdfPath).isNotNull();
        assertThat(Files.exists(pdfPath)).isTrue();
        
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
        
        // Clean up
        Files.deleteIfExists(pdfPath);
    }
}