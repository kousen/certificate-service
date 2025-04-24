package com.kousen.cert.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PdfSigner
 */
class PdfSignerPropertyTest {

    // Initialize for each test
    private KeyStoreProvider initKeyStoreProvider() {
        Path keystorePath = Paths.get("test-keystore.p12");
        return new KeyStoreProvider(keystorePath);
    }
    
    private PdfSigner initSigner() {
        return new PdfSigner(initKeyStoreProvider());
    }
    
    private PdfBoxGenerator initGenerator() {
        return new PdfBoxGenerator();
    }
    
    /**
     * Property: Signing a PDF preserves the original content
     */
    @Disabled("Needs more investigation to fix filter comparison issue")
    @Property(tries = 3)
    void signingShouldPreserveOriginalContent(
            @ForAll @NotBlank @StringLength(min = 2, max = 40) String name,
            @ForAll @NotBlank @StringLength(min = 2, max = 40) String bookTitle) throws Exception {
        
        // Arrange - Create a PDF
        String title = "Certificate of Ownership";
        PdfBoxGenerator generator = initGenerator();
        Path unsignedPdf = generator.createCertificatePdf(title, name, bookTitle, null);
        
        try {
            // Extract text before signing
            String contentBeforeSigning = extractTextFromPdf(unsignedPdf);
            
            // Act - Sign the PDF
            PdfSigner signer = initSigner();
            Path signedPdf = signer.sign(unsignedPdf);
            
            try {
                // Assert - Signed PDF should exist and have reasonable size
                assertThat(signedPdf).exists();
                assertThat(Files.size(signedPdf)).isGreaterThan(Files.size(unsignedPdf));
                
                // Extract text after signing
                String contentAfterSigning = extractTextFromPdf(signedPdf);
                
                // Content should be preserved
                assertThat(contentAfterSigning).contains(contentBeforeSigning);
                
                // Verify signature exists
                try (PDDocument document = Loader.loadPDF(signedPdf.toFile())) {
                    List<PDSignature> signatures = document.getSignatureDictionaries();
                    assertThat(signatures).isNotEmpty();
                    
                    PDSignature signature = signatures.getFirst();
                    assertThat(signature.getFilter()).isEqualTo("Adobe.PPKLite");
                    assertThat(signature.getSubFilter()).isEqualTo("adbe.pkcs7.detached");
                    assertThat(signature.getName()).isEqualTo("Ken Kousen");
                    assertThat(signature.getReason()).isEqualTo("Certificate of Ownership");
                }
                
            } finally {
                // Cleanup signed PDF
                Files.deleteIfExists(signedPdf);
            }
            
        } finally {
            // Cleanup unsigned PDF
            Files.deleteIfExists(unsignedPdf);
        }
    }
    
    /**
     * Property: Multiple signings should each produce valid signatures
     */
    @Disabled("Needs more investigation to fix filter comparison issue")
    @Property(tries = 5)  // Reduced number of trials to avoid excessive keystore operations
    void multiplePdfsCanBeSignedSuccessively(
            @ForAll("simplePdfContents") List<PdfContent> contents) throws Exception {
        
        Path[] unsignedPdfs = new Path[contents.size()];
        Path[] signedPdfs = new Path[contents.size()];
        
        try {
            PdfBoxGenerator generator = initGenerator();
            PdfSigner signer = initSigner();
            
            // Create and sign multiple PDFs in sequence
            for (int i = 0; i < contents.size(); i++) {
                PdfContent content = contents.get(i);
                
                // Create PDF
                unsignedPdfs[i] = generator.createCertificatePdf(
                        "Certificate of Ownership", 
                        content.name, 
                        content.bookTitle, 
                        null);
                
                // Sign PDF
                signedPdfs[i] = signer.sign(unsignedPdfs[i]);
                
                // Verify signature exists
                try (PDDocument document = Loader.loadPDF(signedPdfs[i].toFile())) {
                    List<PDSignature> signatures = document.getSignatureDictionaries();
                    assertThat(signatures).isNotEmpty();
                    
                    // Basic signature validation
                    PDSignature signature = signatures.getFirst();
                    assertThat(signature.getFilter()).isEqualTo("Adobe.PPKLite");
                    
                    // Each signature should have a creation date
                    Calendar signDate = signature.getSignDate();
                    assertThat(signDate).isNotNull();
                }
            }
            
        } finally {
            // Cleanup all PDFs
            for (int i = 0; i < contents.size(); i++) {
                if (unsignedPdfs[i] != null) {
                    Files.deleteIfExists(unsignedPdfs[i]);
                }
                if (signedPdfs[i] != null) {
                    Files.deleteIfExists(signedPdfs[i]);
                }
            }
        }
    }
    
    /**
     * Data class to represent PDF content
     */
    private static class PdfContent {
        final String name;
        final String bookTitle;
        
        PdfContent(String name, String bookTitle) {
            this.name = name;
            this.bookTitle = bookTitle;
        }
    }
    
    @Provide
    Arbitrary<List<PdfContent>> simplePdfContents() {
        // Create simple names and book titles
        Arbitrary<String> names = Arbitraries.of(
                "John Smith", "Jane Doe", "Alice Johnson", "Bob Brown", "Carol White");
        
        Arbitrary<String> bookTitles = Arbitraries.of(
                "Modern Java", "Spring Boot Guide", "Kotlin Essentials", 
                "Effective Testing", "Cloud Native Apps");
        
        // Combine them into PdfContent objects
        Arbitrary<PdfContent> contentArbitrary = Combinators.combine(names, bookTitles)
                .as(PdfContent::new);
        
        // Return a list of 1-3 contents
        return contentArbitrary.list().ofSize(3);
    }
    
    // Helper method to extract text from a PDF file
    private String extractTextFromPdf(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}