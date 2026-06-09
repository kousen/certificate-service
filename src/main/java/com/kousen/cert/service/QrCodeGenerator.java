package com.kousen.cert.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kousen.cert.config.ServerUrlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            var qrCodeWriter = new QRCodeWriter();
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
     * Generates a QR code as a PNG byte array without writing to disk.
     *
     * @param name       The name of the certificate holder
     * @param bookTitle  The book title
     * @param size       The size of the QR code in pixels
     * @return PNG image data as a byte array
     * @throws IOException If there's an error during QR code generation
     */
    public byte[] generateQrCodeData(String name, String bookTitle, int size) throws IOException {
        return generateQrCodeData(name, bookTitle, null, size);
    }

    /**
     * Generates a QR code as a PNG byte array whose verification URL includes the
     * certificate ID, allowing the verification page to confirm the certificate
     * against the issuance records.
     *
     * @param name          The name of the certificate holder
     * @param bookTitle     The book title
     * @param certificateId The unique ID assigned to the certificate (may be null)
     * @param size          The size of the QR code in pixels
     * @return PNG image data as a byte array
     * @throws IOException If there's an error during QR code generation
     */
    public byte[] generateQrCodeData(String name, String bookTitle, String certificateId, int size) throws IOException {
        String verificationUrl = buildVerificationUrl(name, bookTitle, certificateId);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, size, size);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Failed to generate QR code data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds a verification URL for the certificate
     */
    private String buildVerificationUrl(String name, String bookTitle) {
        return buildVerificationUrl(name, bookTitle, null);
    }

    private String buildVerificationUrl(String name, String bookTitle, String certificateId) {
        String baseUrl = serverConfig.getUrl();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // Create the verification URL with parameters
        String url = String.format(Locale.US,
                "%s/verify-certificate?name=%s&book=%s&date=%s",
                baseUrl,
                encodeUrlParam(name),
                encodeUrlParam(bookTitle),
                date);
        if (certificateId != null && !certificateId.isBlank()) {
            url += "&id=" + encodeUrlParam(certificateId);
        }
        return url;
    }

    /**
     * URL parameter encoding using percent-encoding for spaces so the result
     * is valid in both query strings and QR code payloads.
     */
    private String encodeUrlParam(String param) {
        if (param == null) return "";
        return URLEncoder.encode(param, StandardCharsets.UTF_8).replace("+", "%20");
    }
}