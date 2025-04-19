package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.template.ElegantTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PdfSignerTest {

    @TempDir
    Path tempDir;

    @Test
    @Disabled("PDF generation needs fixing with PDFBox")
    void shouldSignPdfWithValidSignature() throws Exception {
        // Create keystore path
        Path keystorePath = tempDir.resolve(".cert_keystore.p12");
        
        // Given
        var provider = new KeyStoreProvider(keystorePath);
        var signer = new PdfSigner(provider);
        var qrCodeGenerator = MockQrCodeGenerator.createFullMock();
        var pdfService = new PdfService(qrCodeGenerator);

        // Create sample certificate PDF
        var request = new CertificateRequest(
                "John von Neumann",
                "Modern Java Recipes",
                Optional.of("john@example.com")
        );
        Path unsignedPdf = pdfService.createPdf(new ElegantTemplate(), request);
        
        // When
        Path signedPdf = signer.sign(unsignedPdf);
        
        // Then
        assertThat(signedPdf).isNotNull();
        assertThat(Files.exists(signedPdf)).isTrue();
        assertThat(Files.size(signedPdf)).isGreaterThan(Files.size(unsignedPdf));
        
        // Verify signature presence
        try (PDDocument document = Loader.loadPDF(signedPdf.toFile())) {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            
            // Verify at least one signature exists
            assertThat(signatures).isNotEmpty();
            
            // Verify signature properties
            PDSignature signature = signatures.getFirst();
            assertThat(signature.getSubFilter())
                    .isEqualTo(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED.getName());
            assertThat(signature.getFilter())
                    .isEqualTo(PDSignature.FILTER_ADOBE_PPKLITE.getName());
            assertThat(signature.getName()).isEqualTo("Ken Kousen");
            assertThat(signature.getSignDate()).isNotNull();
        }
        
        // Clean up
        Files.deleteIfExists(unsignedPdf);
        Files.deleteIfExists(signedPdf);
    }
    
    @Test
    @Disabled("PDF generation needs fixing with PDFBox")
    void shouldVerifyNonRepudiation() throws Exception {
        // Create keystore path
        Path keystorePath = tempDir.resolve(".cert_keystore.p12");
        
        // Given
        KeyStoreProvider provider = new KeyStoreProvider(keystorePath);
        PdfSigner signer = new PdfSigner(provider);
        QrCodeGenerator qrCodeGenerator = MockQrCodeGenerator.createFullMock();
        PdfService pdfService = new PdfService(qrCodeGenerator);

        // Create two identical content PDFs
        CertificateRequest request = new CertificateRequest(
                "Venkat Subramaniam",
                "Mockito Made Clear",
                Optional.empty()
        );
        
        Path pdf1 = pdfService.createPdf(new ElegantTemplate(), request);
        Path signedPdf1 = signer.sign(pdf1);
        
        Path pdf2 = pdfService.createPdf(new ElegantTemplate(), request);
        Path signedPdf2 = signer.sign(pdf2);
        
        // When - Extract signatures
        byte[] signature1;
        byte[] signature2;
        
        try (PDDocument doc1 = Loader.loadPDF(signedPdf1.toFile());
             PDDocument doc2 = Loader.loadPDF(signedPdf2.toFile())) {
            
            PDSignature sig1 = doc1.getSignatureDictionaries().getFirst();
            PDSignature sig2 = doc2.getSignatureDictionaries().getFirst();
            
            signature1 = sig1.getContents(Files.readAllBytes(signedPdf1));
            signature2 = sig2.getContents(Files.readAllBytes(signedPdf2));
        }
        
        // Then - Signatures should be different even for same content
        // (ensures non-repudiation - each signature is unique)
        assertThat(signature1).isNotEqualTo(signature2);
        
        // Clean up
        Files.deleteIfExists(pdf1);
        Files.deleteIfExists(pdf2);
        Files.deleteIfExists(signedPdf1);
        Files.deleteIfExists(signedPdf2);
    }
}