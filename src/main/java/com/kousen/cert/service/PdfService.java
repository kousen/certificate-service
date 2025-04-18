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
            
            // Simplified font loading approach
            try {
                // Use a direct URL-based approach that works well with classpath resources in JAR
                renderer.getSharedContext().setBaseURL(baseUrl);
                
                // Log font loading
                System.out.println("=== FONT DEBUG INFORMATION ===");
                System.out.println("Base URL: " + baseUrl);
                
                // Register fonts directly and tell Flying Saucer to load them from classpath
                String fontCss = 
                      "@font-face { font-family: 'Times New Roman'; src: local('Times New Roman'); }\n"
                    + "@font-face { font-family: 'CinzelDecorative'; src: url('" + baseUrl + "fonts/CinzelDecorative-Regular.ttf'); }\n"
                    + "@font-face { font-family: 'GreatVibes'; src: url('" + baseUrl + "fonts/GreatVibes-Regular.ttf'); }\n";
                
                // Add a direct stylesheet with font declarations
                renderer.getSharedContext().getCss().parseAndApplyStylesheet(
                    null, // No existing stylesheet reference needed
                    fontCss, 
                    baseUrl);
                
                System.out.println("Registered fonts via CSS @font-face");
                System.out.println("=== END FONT DEBUG INFO ===");
            } catch (Exception e) {
                System.err.println("Font registration error: " + e.getMessage());
                e.printStackTrace();
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