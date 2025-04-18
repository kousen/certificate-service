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
        
        return "verify-certificate";
    }
    
    /**
     * Generate a SHA-256 fingerprint of the certificate in the keystore.
     * This is a secure way to identify the certificate without exposing the actual key.
     */
    protected String generateCertificateFingerprint(String keystorePath) {
        try {
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
            logger.error("Failed to generate certificate fingerprint", e);
            return "Error generating certificate fingerprint";
        }
    }
}