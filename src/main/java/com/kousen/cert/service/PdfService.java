package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.PdfTemplate;
import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

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
            
            // Very basic font loading approach
            try {
                // Set the base URL
                renderer.getSharedContext().setBaseURL(baseUrl);
                
                // Directly load fonts from resources to temporary files
                try {
                    ClassPathResource cinzelFont = new ClassPathResource("/fonts/CinzelDecorative-Regular.ttf");
                    ClassPathResource greatVibesFont = new ClassPathResource("/fonts/GreatVibes-Regular.ttf");
                    
                    // Create temp files
                    Path tempCinzel = Files.createTempFile("cinzel", ".ttf");
                    Path tempGreatVibes = Files.createTempFile("greatvibes", ".ttf");
                    
                    // Copy font data to temp files
                    Files.copy(cinzelFont.getInputStream(), tempCinzel, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(greatVibesFont.getInputStream(), tempGreatVibes, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Register fonts
                    renderer.getFontResolver().addFont(tempCinzel.toFile().getAbsolutePath(), true);
                    renderer.getFontResolver().addFont(tempGreatVibes.toFile().getAbsolutePath(), true);
                    
                    // Clean up temp files when JVM exits
                    tempCinzel.toFile().deleteOnExit();
                    tempGreatVibes.toFile().deleteOnExit();
                    
                    System.out.println("Loaded fonts directly: " + tempCinzel + ", " + tempGreatVibes);
                } catch (Exception fontEx) {
                    System.err.println("Error loading fonts directly: " + fontEx.getMessage());
                    fontEx.printStackTrace();
                }
                
            } catch (Exception e) {
                System.err.println("General font setup error: " + e.getMessage());
                e.printStackTrace();
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