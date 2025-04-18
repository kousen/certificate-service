package com.kousen.cert.controller;

import com.kousen.cert.service.KeyStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;

@Controller
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final String certificateFingerprint;

    public VerificationController() {
        this("${certificate.keystore}");
    }
    
    public VerificationController(@Value("${certificate.keystore}") String keystorePath) {
        String fingerprint;
        try {
            fingerprint = generateCertificateFingerprint(keystorePath);
        } catch (Exception e) {
            logger.warn("Could not generate certificate fingerprint: {}", e.getMessage());
            fingerprint = "Certificate fingerprint not available during test";
        }
        this.certificateFingerprint = fingerprint;
    }

    @GetMapping("/verify-certificate")
    public String verifyPage(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "book", required = false) String bookTitle,
            @RequestParam(name = "date", required = false) String issueDate,
            Model model) {
        
        logger.info("Verification request received for: name={}, book={}, date={}", 
                name, bookTitle, issueDate);
        
        model.addAttribute("name", name != null ? name : "Not specified");
        model.addAttribute("bookTitle", bookTitle != null ? bookTitle : "Not specified");
        model.addAttribute("issueDate", issueDate != null ? issueDate : "Not specified");
        model.addAttribute("certificateFingerprint", certificateFingerprint);
        
        // Use "verification-page" as the view name to avoid circular view path error
        return "verification-page";
    }
    
    /**
     * Generate a SHA-256 fingerprint of the certificate in the keystore.
     * This is a secure way to identify the certificate without exposing the actual key.
     */
    protected String generateCertificateFingerprint(String keystorePath) {
        try {
            // Handle potential environment variable in the path
            if (keystorePath.startsWith("${") && keystorePath.endsWith("}")) {
                String envVarName = keystorePath.substring(2, keystorePath.length() - 1);
                String envValue = System.getenv(envVarName);
                if (envValue == null) {
                    // Extract default value if present (format: ${ENV_VAR:default})
                    if (envVarName.contains(":")) {
                        String[] parts = envVarName.split(":", 2);
                        String defaultValue = parts[1];
                        keystorePath = defaultValue;
                    } else {
                        logger.warn("Environment variable {} not found", envVarName);
                        return "Certificate fingerprint not available - keystore path not configured";
                    }
                } else {
                    keystorePath = envValue;
                }
            }
            
            logger.info("Loading keystore from: {}", keystorePath);
            KeyStoreProvider provider = new KeyStoreProvider(Paths.get(keystorePath));
            Certificate cert = provider.keyStore().getCertificate("authorKey");
            
            if (cert instanceof X509Certificate) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] der = cert.getEncoded();
                byte[] digest = md.digest(der);
                
                // Format the fingerprint as a colon-separated hex string
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    sb.append(String.format("%02X", digest[i]));
                    if (i < digest.length - 1) {
                        sb.append(":");
                    }
                }
                
                return sb.toString();
            }
            
            return "Certificate fingerprint not available";
        } catch (Exception e) {
            logger.error("Failed to generate certificate fingerprint: {}", e.getMessage(), e);
            return "Error generating certificate fingerprint";
        }
    }
}