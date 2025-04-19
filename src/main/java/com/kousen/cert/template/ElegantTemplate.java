package com.kousen.cert.template;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.util.QrCodeUtil;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public record ElegantTemplate() implements PdfTemplate {

    // Cache for resource data URIs
    private static String cinzelFontDataUri;
    private static String greatVibesFontDataUri;
    private static String backgroundImageDataUri;

    static {
        try {
            // Load resources as data URIs
            cinzelFontDataUri = resourceToDataUri("fonts/CinzelDecorative-Regular.ttf", "font/ttf");
            greatVibesFontDataUri = resourceToDataUri("fonts/GreatVibes-Regular.ttf", "font/ttf");
            backgroundImageDataUri = resourceToDataUri("images/certificate-bg.png", "image/png");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load embedded resources", e);
        }
    }

    @Override
    public String html(CertificateRequest r) {
        try {
            // Read template file using InputStream to work with JAR resources
            var resource = new ClassPathResource("templates/elegant-certificate.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Generate QR code with certificate-specific verification URL
            String qrCode = QrCodeUtil.dataUri(r.purchaserName(), r.bookTitle(), 220);

            // XML encode the values to avoid parsing issues
            String purchaserName = encodeXml(r.purchaserName());
            String bookTitle = encodeXml(r.bookTitle());

            // Replace font and image URLs with data URIs
            template = template.replace("url('classpath:/fonts/CinzelDecorative-Regular.ttf')", "url('" + cinzelFontDataUri + "')");
            template = template.replace("url('classpath:/fonts/GreatVibes-Regular.ttf')", "url('" + greatVibesFontDataUri + "')");
            template = template.replace("url('classpath:/images/certificate-bg.png')", "url('" + backgroundImageDataUri + "')");

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

    /**
     * Convert a classpath resource to a data URI
     */
    private static String resourceToDataUri(String resourcePath, String mimeType) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        byte[] bytes = resource.getInputStream().readAllBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }
}
