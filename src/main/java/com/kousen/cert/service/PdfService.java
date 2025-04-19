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
        // Get the HTML content
        String html = template.html(request);
        
        // Create a temporary file for the PDF output
        Path out = Files.createTempFile("cert-", ".pdf");

        try (OutputStream os = Files.newOutputStream(out)) {
            // Create the PDF renderer
            ITextRenderer renderer = new ITextRenderer();
            
            // 1. Configure the base URL for resource loading
            ClassPathResource rootResource = new ClassPathResource("/");
            String baseUrl = rootResource.getURL().toExternalForm();
            renderer.getSharedContext().setBaseURL(baseUrl);
            
            // 2. Register fonts - must be done before processing the document
            try {
                CustomFontResolver.registerFonts(renderer);
                System.out.println("Registered fonts using CustomFontResolver");
            } catch (Exception fontEx) {
                System.err.println("Error registering fonts: " + fontEx.getMessage());
                fontEx.printStackTrace();
            }
            
            // 3. Set the document content and generate the PDF
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(os);
            os.flush();
            
            // Log success
            System.out.println("PDF created at: " + out.toAbsolutePath());
        } catch (Exception e) {
            // Clean up on error
            Files.deleteIfExists(out);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return out;
    }

}
