package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A simple test utility for checking if fonts are working correctly
 * Only runs when the "fonttest" profile is active
 */
@Component
@Profile("fonttest")
public class FontTester implements CommandLineRunner {

    private final PdfService pdfService;
    
    public FontTester(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("----------------------");
        System.out.println("FONT TEST MODE ACTIVE");
        System.out.println("----------------------");
        
        // Create a test certificate with a valid book title
        CertificateRequest request = new CertificateRequest(
            "Font Test User", 
            "Making Java Groovy",
            Optional.empty()  // No email
        );
        
        // Generate the PDF
        Path pdfPath = pdfService.createPdf(new ElegantTemplate(), request);
        
        // Output the result
        System.out.println("Test PDF created at: " + pdfPath.toAbsolutePath());
        System.out.println("Please check if the fonts appear correctly.");
        System.out.println("----------------------");
    }
}