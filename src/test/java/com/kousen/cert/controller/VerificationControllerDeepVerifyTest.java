package com.kousen.cert.controller;

import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import com.kousen.cert.service.BlockchainService;
import com.kousen.cert.service.CertificateStorageService;
import com.kousen.cert.service.PdfVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationControllerDeepVerifyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CertificateMetadataService metadataService;

    @MockitoBean
    private PdfVerificationService pdfVerificationService;

    @MockitoBean
    private CertificateStorageService storageService;

    @MockitoBean
    private BlockchainService blockchainService;

    @Test
    @DisplayName("should perform deep verification successfully")
    void shouldPerformDeepVerificationSuccessfully() throws Exception {
        // Arrange
        String certId = "cert-123";
        CertificateMetadata metadata = new CertificateMetadata(certId, "test.pdf");
        metadata.setFileHash("hash1");
        metadata.setQuantumHash("qhash1");
        metadata.setMerkleProof("mproof1");
        
        when(metadataService.getCertificateMetadata(certId)).thenReturn(metadata);
        when(storageService.getCertificate(anyString())).thenReturn(Path.of("test.pdf"));
        when(pdfVerificationService.verifySignature(any())).thenReturn(true);
        when(pdfVerificationService.performBiometricAnalysis(any())).thenReturn(Map.of("status", "VALIDATED", "stylometricConfidence", 0.99));
        when(blockchainService.getNetworkStatus()).thenReturn("ACTIVE_TEST_NODE");

        // Act & Assert
        mockMvc.perform(get("/api/verify/deep/" + certId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.quantumHash").value("qhash1"))
                .andExpect(jsonPath("$.merkleProof").value("mproof1"))
                .andExpect(jsonPath("$.blockchainStatus").value("ACTIVE_TEST_NODE"))
                .andExpect(jsonPath("$.biometricAnalysis.status").value("VALIDATED"));
    }

    @Test
    @DisplayName("should return error when metadata is not found")
    void shouldReturnErrorWhenMetadataNotFound() throws Exception {
        // Arrange
        when(metadataService.getCertificateMetadata("unknown")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/verify/deep/unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Certificate metadata not found"));
    }
}
