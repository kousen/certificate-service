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
    
    /**
     * Default constructor: uses a new PdfBoxGenerator instance.
     */
    public PdfService(QrCodeGenerator qrCodeGenerator) {
        this(qrCodeGenerator, new PdfBoxGenerator());
    }
    
    /**
     * Constructor for dependency injection of PdfBoxGenerator.
     */
    @Autowired
    public PdfService(QrCodeGenerator qrCodeGenerator, PdfBoxGenerator pdfGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfGenerator = pdfGenerator;
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
            // Generate QR code in-memory
            byte[] qrCodeData = qrCodeGenerator.generateQrCodeData(
                    request.purchaserName(),
                    request.bookTitle(),
                    220);

            // Create PDF with PDFBox using in-memory QR code
            Path pdfPath = pdfGenerator.createCertificatePdfWithQrData(
                    "Certificate of Ownership",
                    request.purchaserName(),
                    request.bookTitle(),
                    qrCodeData);

            return pdfPath;
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
