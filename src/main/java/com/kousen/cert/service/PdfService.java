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
            
            // Register fonts using bytes from input streams to work with JAR resources
            try {
                // Load fonts directly from resources
                ClassPathResource cinzelFont = new ClassPathResource("/fonts/CinzelDecorative-Regular.ttf");
                ClassPathResource greatVibesFont = new ClassPathResource("/fonts/GreatVibes-Regular.ttf");
                
                // Add each font using byte arrays instead of file paths
                renderer.getFontResolver().addFont(
                    cinzelFont.getInputStream().readAllBytes(), 
                    "CinzelDecorative", 
                    0, 
                    true);
                renderer.getFontResolver().addFont(
                    greatVibesFont.getInputStream().readAllBytes(), 
                    "GreatVibes", 
                    0, 
                    true);
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