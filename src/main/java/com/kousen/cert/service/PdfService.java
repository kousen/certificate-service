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
            
            // Custom font handling with improved reliability
            try {
                // Set the base URL so the XML renderer can find resources
                renderer.getSharedContext().setBaseURL(baseUrl);
                
                // Register custom fonts with our font resolver helper
                CustomFontResolver.registerFonts(renderer);
                
                System.out.println("Custom fonts registered successfully");
                
            } catch (Exception e) {
                System.err.println("Font setup error: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("Failed to set up fonts", e);
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