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
            
            // Directly use the CSS @font-face approach and let Flying Saucer handle it
            try {
                // Set the source root URI for CSS to find resources relatively
                renderer.getSharedContext().setBaseURL(baseUrl);
                
                // Log font loading attempt
                System.out.println("Loading fonts from base URL: " + baseUrl);
                System.out.println("Font directories available: " + new ClassPathResource("/fonts").getURL());
                
                // Register basic font directory
                String fontPath = new ClassPathResource("/fonts").getURL().toString();
                renderer.getFontResolver().addFontDirectory(fontPath, true);
                
                // Log successful font registration
                System.out.println("Successfully registered font directory: " + fontPath);
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