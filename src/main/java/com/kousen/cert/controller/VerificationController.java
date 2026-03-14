package com.kousen.cert.controller;

import com.kousen.cert.analytics.model.AnalyticsRequestContext;
import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import com.kousen.cert.service.CertificateStorageService;
import com.kousen.cert.service.PdfVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final String certificateFingerprint;
    private final AnalyticsService analyticsService;
    private final CertificateMetadataService metadataService;
    private final PdfVerificationService pdfVerificationService;
    private final CertificateStorageService storageService;
    private final com.kousen.cert.service.BlockchainService blockchainService;

    // Default constructor for tests
    public VerificationController() {
        this("Certificate fingerprint not available during test", null, null, null, null, null);
    }

    VerificationController(String certificateFingerprint, 
                           AnalyticsService analyticsService,
                           CertificateMetadataService metadataService,
                           PdfVerificationService pdfVerificationService,
                           CertificateStorageService storageService,
                           com.kousen.cert.service.BlockchainService blockchainService) {
        this.certificateFingerprint = certificateFingerprint;
        this.analyticsService = analyticsService;
        this.metadataService = metadataService;
        this.pdfVerificationService = pdfVerificationService;
        this.storageService = storageService;
        this.blockchainService = blockchainService;
    }

    @Autowired
    public VerificationController(com.kousen.cert.service.KeyStoreProvider keyStoreProvider, 
                                  AnalyticsService analyticsService,
                                  CertificateMetadataService metadataService,
                                  PdfVerificationService pdfVerificationService,
                                  CertificateStorageService storageService,
                                  com.kousen.cert.service.BlockchainService blockchainService) {
        this(generateCertificateFingerprint(keyStoreProvider), analyticsService, metadataService, pdfVerificationService, storageService, blockchainService);
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
        model.addAttribute("certificateId", certificateId);

        if (certificateId != null && !certificateId.isEmpty() && metadataService != null) {
            CertificateMetadata metadata = metadataService.getCertificateMetadata(certificateId);
            if (metadata != null) {
                model.addAttribute("quantumHash", metadata.getQuantumHash());
            }
        }

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

    @GetMapping("/api/verify/deep/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deepVerify(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            CertificateMetadata metadata = metadataService.getCertificateMetadata(id);
            if (metadata == null) {
                response.put("status", "error");
                response.put("message", "Certificate metadata not found");
                return ResponseEntity.badRequest().body(response);
            }

            Path pdfPath = storageService.getCertificate(metadata.getFilename());
            boolean isValid = pdfVerificationService.verifySignature(pdfPath);
            Map<String, Object> biometricAnalysis = pdfVerificationService.performBiometricAnalysis(pdfPath);

            response.put("status", "success");
            response.put("isValid", isValid);
            response.put("filename", metadata.getFilename());
            response.put("fileHash", metadata.getFileHash());
            response.put("quantumHash", metadata.getQuantumHash());
            response.put("merkleProof", metadata.getMerkleProof());
            response.put("blockchainStatus", blockchainService != null ? blockchainService.getNetworkStatus() : "OFFLINE");
            response.put("biometricAnalysis", biometricAnalysis);
            response.put("timestamp", java.time.Instant.now().toString());
            response.put("verificationMethod", "Server-side Cryptographic Signature Validation & Biometric Stylometry");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
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
