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
            
            // Register fonts using iText's built-in font resolver for embedded applications
            try {
                // Use a more direct approach without relying on file access
                com.lowagie.text.pdf.BaseFont cinzelBaseFont = com.lowagie.text.pdf.BaseFont.createFont(
                    "fonts/CinzelDecorative-Regular.ttf", 
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H, 
                    com.lowagie.text.pdf.BaseFont.EMBEDDED,
                    true,
                    new ClassPathResource("/fonts/CinzelDecorative-Regular.ttf").getInputStream().readAllBytes(),
                    null
                );
                
                com.lowagie.text.pdf.BaseFont greatVibesBaseFont = com.lowagie.text.pdf.BaseFont.createFont(
                    "fonts/GreatVibes-Regular.ttf", 
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H, 
                    com.lowagie.text.pdf.BaseFont.EMBEDDED,
                    true,
                    new ClassPathResource("/fonts/GreatVibes-Regular.ttf").getInputStream().readAllBytes(),
                    null
                );
                
                // Register the fonts with explicit family names
                renderer.getFontResolver().addFont(cinzelBaseFont, "CinzelDecorative");
                renderer.getFontResolver().addFont(greatVibesBaseFont, "GreatVibes");
                
                // Log successful font registration
                System.out.println("Successfully registered fonts: CinzelDecorative and GreatVibes");
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