package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for creating PDF certificates
 */
@Service
public class PdfService {

    private final QrCodeGenerator qrCodeGenerator;
    private final PdfBoxGenerator pdfGenerator;
    
    @Autowired
    public PdfService(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfGenerator = new PdfBoxGenerator();
    }
    
    /**
     * Creates a PDF certificate from a certificate request
     * 
     * @param request The certificate request with recipient and book info
     * @return Path to the generated PDF file
     * @throws IOException If there's an error during PDF creation
     */
    public Path createPdf(CertificateRequest request) throws IOException {
        try {
            // Generate QR code with verification URL
            Path qrCodePath = qrCodeGenerator.generateQrCode(
                    request.purchaserName(), 
                    request.bookTitle(),
                    220);  // QR code size in pixels
            
            // Create PDF with PDFBox
            Path pdfPath = pdfGenerator.createCertificatePdf(
                    "Certificate of Ownership",
                    request.purchaserName(),
                    request.bookTitle(),
                    qrCodePath);
            
            // Clean up temporary QR code file
            Files.deleteIfExists(qrCodePath);
            
            return pdfPath;
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
