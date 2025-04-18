package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.PdfTemplate;
import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PdfService {

    public Path createPdf(PdfTemplate template, CertificateRequest request) throws IOException {
        String html = template.html(request);
        Path out = Files.createTempFile("cert-", ".pdf");
        
        try (OutputStream os = Files.newOutputStream(out)) {
            // Create a new ITextRenderer
            var renderer = new ITextRenderer();
            
            // Configure resources and fonts
            ClassPathResource rootResource = new ClassPathResource("/");
            String baseUrl = rootResource.getURL().toExternalForm();
            
            // Register fonts using temp files to work with JAR resources
            try {
                // Load fonts from resources and create temporary files
                ClassPathResource cinzelFont = new ClassPathResource("/fonts/CinzelDecorative-Regular.ttf");
                ClassPathResource greatVibesFont = new ClassPathResource("/fonts/GreatVibes-Regular.ttf");
                
                // Create temporary files for the fonts
                Path tempCinzelFont = Files.createTempFile("cinzel", ".ttf");
                Path tempGreatVibesFont = Files.createTempFile("greatvibes", ".ttf");
                
                // Copy font data to temp files
                Files.write(tempCinzelFont, cinzelFont.getInputStream().readAllBytes());
                Files.write(tempGreatVibesFont, greatVibesFont.getInputStream().readAllBytes());
                
                // Add fonts using file paths
                renderer.getFontResolver().addFont(
                    tempCinzelFont.toFile().getAbsolutePath(), 
                    true);
                renderer.getFontResolver().addFont(
                    tempGreatVibesFont.toFile().getAbsolutePath(), 
                    true);
                
                // Register cleanup to delete temp files when JVM exits
                tempCinzelFont.toFile().deleteOnExit();
                tempGreatVibesFont.toFile().deleteOnExit();
            } catch (Exception e) {
                throw new IOException("Failed to load fonts: " + e.getMessage(), e);
            }
            
            // Render the PDF - the classpath: prefix in the HTML/CSS will be resolved relative to the baseUrl
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(os);
            os.flush();
            
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
        
        return out;
    }
}