package com.kousen.cert.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kousen.cert.config.ServerUrlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generates QR codes for certificate verification
 */
@Component
public class QrCodeGenerator {

    private final ServerUrlConfig serverConfig;
    
    @Autowired
    public QrCodeGenerator(ServerUrlConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
    
    /**
     * Generates a QR code containing a URL for verifying the certificate
     * 
     * @param name The name of the certificate holder
     * @param bookTitle The book title
     * @param size The size of the QR code in pixels
     * @return Path to the generated QR code image file
     * @throws IOException If there's an error during QR code generation
     */
    public Path generateQrCode(String name, String bookTitle, int size) throws IOException {
        // Create verification URL
        String verificationUrl = buildVerificationUrl(name, bookTitle);
        
        // Create temporary file for the QR code
        Path qrCodePath = Files.createTempFile("qrcode-", ".png");
        
        try {
            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, size, size);
            
            // Write to file
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrCodePath);
            
            return qrCodePath;
        } catch (Exception e) {
            // Clean up if generation fails
            Files.deleteIfExists(qrCodePath);
            throw new IOException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds a verification URL for the certificate
     */
    private String buildVerificationUrl(String name, String bookTitle) {
        String baseUrl = serverConfig.getUrl();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        // Create the verification URL with parameters
        return String.format(Locale.US, 
                "%s/verify?name=%s&book=%s&date=%s",
                baseUrl,
                encodeUrlParam(name),
                encodeUrlParam(bookTitle),
                date);
    }
    
    /**
     * Simple URL parameter encoding
     */
    private String encodeUrlParam(String param) {
        if (param == null) return "";
        return param.replace(" ", "%20")
                  .replace("&", "%26")
                  .replace("=", "%3D")
                  .replace("?", "%3F");
    }
}