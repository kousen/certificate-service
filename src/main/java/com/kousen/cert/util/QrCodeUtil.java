package com.kousen.cert.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public final class QrCodeUtil {
    private QrCodeUtil() {}

    // Define default base URL and verification path
    private static final String DEFAULT_VERIFICATION_PATH = "/verify-certificate";
    
    // URLs will be dynamically constructed using server URL
    private static String serverBaseUrl = "https://certificate-service-997e5d9f565a.herokuapp.com";
    
    /**
     * Sets the server base URL for QR code generation
     * @param baseUrl the base URL of the server
     */
    public static void setServerBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            // Remove trailing slash if present
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            serverBaseUrl = baseUrl;
            System.out.println("QR Code base URL set to: " + serverBaseUrl);
        }
    }

    /**
     * Gets the verification URL for a specific certificate
     *
     * @param purchaserName the name of the certificate purchaser
     * @param bookTitle the title of the book
     * @return the verification URL
     */
    public static String getVerificationUrl(String purchaserName, String bookTitle) {
        // URL-encode the parameters
        String encodedName = URLEncoder.encode(purchaserName, StandardCharsets.UTF_8);
        String encodedTitle = URLEncoder.encode(bookTitle, StandardCharsets.UTF_8);

        // Build verification URL with parameters
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Construct absolute URL using server base URL
        return serverBaseUrl + DEFAULT_VERIFICATION_PATH +
                "?name=" + encodedName +
                "&book=" + encodedTitle +
                "&date=" + formattedDate;
    }

    /**
     * Generates a QR code as a data URI with the default verification URL
     *
     * @param sizePx the size of the QR code in pixels
     * @return a data URI containing the QR code image
     */
    public static String dataUri(int sizePx) {
        return dataUri(serverBaseUrl + DEFAULT_VERIFICATION_PATH, sizePx);
    }

    /**
     * Generates a QR code as a data URI with certificate-specific information
     *
     * @param purchaserName the name of the certificate purchaser
     * @param bookTitle the title of the book
     * @param sizePx the size of the QR code in pixels
     * @return a data URI containing the QR code image
     */
    public static String dataUri(String purchaserName, String bookTitle, int sizePx) {
        return dataUri(getVerificationUrl(purchaserName, bookTitle), sizePx);
    }

    /**
     * Generates a QR code as a data URI from a specific URL
     *
     * @param url the URL to encode in the QR code
     * @param sizePx the size of the QR code in pixels
     * @return a data URI containing the QR code image
     */
    public static String dataUri(String url, int sizePx) {
        try {
            var bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, sizePx, sizePx);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(img, "PNG", baos);
                var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                return "data:image/png;base64," + base64;
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
