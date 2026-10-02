package com.kousen.cert.controller;

import com.kousen.cert.analytics.model.AnalyticsRequestContext;
import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Controller
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final String certificateFingerprint;
    private final AnalyticsService analyticsService;
    private final CertificateMetadataService metadataService;

    // Default constructor for tests
    public VerificationController() {
        this("Certificate fingerprint not available during test", null);
    }

    VerificationController(String certificateFingerprint, AnalyticsService analyticsService) {
        this(certificateFingerprint, analyticsService, null);
    }

    VerificationController(String certificateFingerprint,
                           AnalyticsService analyticsService,
                           CertificateMetadataService metadataService) {
        this.certificateFingerprint = certificateFingerprint;
        this.analyticsService = analyticsService;
        this.metadataService = metadataService;
    }

    @Autowired
    public VerificationController(com.kousen.cert.service.KeyStoreProvider keyStoreProvider,
                                  AnalyticsService analyticsService,
                                  CertificateMetadataService metadataService) {
        this(generateCertificateFingerprint(keyStoreProvider), analyticsService, metadataService);
    }

    @GetMapping("/verify-certificate")
    public String verifyPage(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "book", required = false) String bookTitle,
            @RequestParam(name = "date", required = false) String issueDate,
            @RequestParam(name = "id", required = false) String certificateId,
            Model model,
            HttpServletRequest request) {

        logger.info("Verification request received for: name={}, book={}, date={}, id={}", 
                name, bookTitle, issueDate, certificateId);

        model.addAttribute("name", name != null ? name : "Not specified");
        model.addAttribute("bookTitle", bookTitle != null ? bookTitle : "Not specified");
        model.addAttribute("issueDate", issueDate != null ? issueDate : "Not specified");
        model.addAttribute("certificateFingerprint", certificateFingerprint);

        // Check the certificate ID against the issuance records so the page can
        // report a real verification result instead of echoing the URL parameters
        String recordStatus = "NO_ID";
        if (certificateId != null && !certificateId.isEmpty()) {
            recordStatus = "UNAVAILABLE";
            if (metadataService != null) {
                CertificateMetadata metadata = metadataService.getCertificateMetadata(certificateId);
                if (metadata != null) {
                    recordStatus = "FOUND";
                    model.addAttribute("certificateId", certificateId);
                    model.addAttribute("issuedAt", metadata.getCreatedAt());
                    model.addAttribute("fileHash", metadata.getFileHash());
                } else {
                    recordStatus = "NOT_FOUND";
                    model.addAttribute("certificateId", certificateId);
                }
            }
        }
        model.addAttribute("recordStatus", recordStatus);

        // Track verification event if analytics service is available and certificate ID is provided
        if (analyticsService != null && certificateId != null && !certificateId.isEmpty()) {
            try {
                analyticsService.trackCertificateVerified(certificateId, AnalyticsRequestContext.from(request));
                logger.info("Tracked verification event for certificate ID: {}", certificateId);
            } catch (Exception e) {
                logger.warn("Failed to track verification event: {}", e.getMessage());
            }
        }

        // Using the verification template
        return "verify-certificate";
    }

    /**
     * Generate a SHA-256 fingerprint of the certificate in the keystore.
     * This is a secure way to identify the certificate without exposing the actual key.
     */
    protected static String generateCertificateFingerprint(com.kousen.cert.service.KeyStoreProvider keyStoreProvider) {
        try {
            Certificate cert = keyStoreProvider.keyStore().getCertificate("authorKey");

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
            logger.warn("Could not generate certificate fingerprint: {}", e.getMessage());
            return "Error generating certificate fingerprint";
        }
    }
}
