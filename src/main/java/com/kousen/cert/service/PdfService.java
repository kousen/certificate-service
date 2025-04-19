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

            try {
                // Set the base URL
                renderer.getSharedContext().setBaseURL(baseUrl);

                // Use CustomFontResolver to register fonts
                try {
                    CustomFontResolver.registerFonts(renderer);
                    System.out.println("Registered fonts using CustomFontResolver");
                } catch (Exception fontEx) {
                    System.err.println("Error registering fonts: " + fontEx.getMessage());
                    fontEx.printStackTrace();
                }

            } catch (Exception e) {
                System.err.println("General font setup error: " + e.getMessage());
                e.printStackTrace();
            }

            // Render the PDF
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
