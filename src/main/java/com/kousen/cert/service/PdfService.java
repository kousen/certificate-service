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
            
            // Use a different approach to embed fonts directly in the PDF
            try {
                // First try using font resolver
                ClassPathResource cinzelFont = new ClassPathResource("/fonts/CinzelDecorative-Regular.ttf");
                ClassPathResource greatVibesFont = new ClassPathResource("/fonts/GreatVibes-Regular.ttf");
                
                // Log available resources
                System.out.println("=== FONT DEBUG INFORMATION ===");
                System.out.println("Base URL: " + baseUrl);
                System.out.println("Cinzel font exists: " + cinzelFont.exists());
                System.out.println("GreatVibes font exists: " + greatVibesFont.exists());
                
                // Use a hybrid approach - first try a direct technique
                try {
                    Path tempDir = Files.createTempDirectory("fonts");
                    Path cinzelPath = tempDir.resolve("CinzelDecorative-Regular.ttf");
                    Path greatVibesPath = tempDir.resolve("GreatVibes-Regular.ttf");
                    
                    // Copy fonts to temporary directory
                    Files.copy(cinzelFont.getInputStream(), cinzelPath, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(greatVibesFont.getInputStream(), greatVibesPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    System.out.println("Created temp files at: " + tempDir);
                    System.out.println("Temp Cinzel exists: " + Files.exists(cinzelPath));
                    System.out.println("Temp GreatVibes exists: " + Files.exists(greatVibesPath));
                    
                    // Register font directory
                    renderer.getFontResolver().addFontDirectory(tempDir.toString(), true);
                    
                    // Add each font individually as well
                    renderer.getFontResolver().addFont(cinzelPath.toString(), true);
                    renderer.getFontResolver().addFont(greatVibesPath.toString(), true);
                    
                    // Set cleanup
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            Files.walk(tempDir)
                                 .sorted(Comparator.reverseOrder())
                                 .map(Path::toFile)
                                 .forEach(java.io.File::delete);
                        } catch (IOException e) {
                            System.err.println("Failed to delete temp directory: " + e.getMessage());
                        }
                    }));
                    
                    System.out.println("Successfully registered fonts via temp directory");
                } catch (Exception e) {
                    System.err.println("Failed with temp directory approach: " + e.getMessage());
                    
                    // Fallback to directly setting CSS resolver base URL
                    renderer.getSharedContext().setBaseURL(baseUrl);
                    renderer.getSharedContext().getCss().setDocumentURI(baseUrl);
                    System.out.println("Set document URI to: " + baseUrl);
                }
                
                // Log success
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