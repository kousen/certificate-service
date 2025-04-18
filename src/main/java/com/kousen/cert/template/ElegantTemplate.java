package com.kousen.cert.template;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.util.QrCodeUtil;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public record ElegantTemplate() implements PdfTemplate {

    @Override
    public String html(CertificateRequest r) {
        try {
            // Read template file
            var resource = new ClassPathResource("templates/elegant-certificate.html");
            String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            
            // Generate QR code with certificate-specific verification URL
            String qrCode = QrCodeUtil.dataUri(r.purchaserName(), r.bookTitle(), 220);
            
            // XML encode the values to avoid parsing issues
            String purchaserName = encodeXml(r.purchaserName());
            String bookTitle = encodeXml(r.bookTitle());
            
            // Replace placeholders with actual values
            return template
                    .replace("${purchaserName}", purchaserName)
                    .replace("${bookTitle}", bookTitle)
                    .replace("${qrCode}", qrCode);
                    
        } catch (IOException e) {
            throw new RuntimeException("Failed to load certificate template", e);
        }
    }
    
    /**
     * Encode special XML characters to prevent parsing issues
     */
    private String encodeXml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}