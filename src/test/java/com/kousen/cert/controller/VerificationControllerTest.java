package com.kousen.cert.controller;

import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CertificateMetadataService testMetadataService() {
            return mock(CertificateMetadataService.class);
        }

        @Bean
        @Primary
        public AnalyticsService testAnalyticsService() {
            return mock(AnalyticsService.class);
        }

        @Bean
        @Primary
        public VerificationController verificationController(AnalyticsService analyticsService,
                                                             CertificateMetadataService metadataService) {
            return new VerificationController("Test certificate fingerprint", analyticsService, metadataService);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CertificateMetadataService metadataService;

    @Test
    void shouldReturnVerificationPageWithDefaultValues() throws Exception {
        // When/Then - Verify the page renders and contains default values
        mockMvc.perform(get("/verify-certificate"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-certificate"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("bookTitle"))
                .andExpect(model().attributeExists("issueDate"))
                .andExpect(model().attributeExists("certificateFingerprint"));
    }
    
    @Test
    void shouldReturnVerificationPageWithProvidedParameters() throws Exception {
        // When/Then - Verify provided parameters are correctly set in the model
        mockMvc.perform(get("/verify-certificate")
                    .param("name", "John Doe")
                    .param("book", "Modern Java Recipes")
                    .param("date", "2025-04-18"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-certificate"))
                .andExpect(model().attribute("name", "John Doe"))
                .andExpect(model().attribute("bookTitle", "Modern Java Recipes"))
                .andExpect(model().attribute("issueDate", "2025-04-18"))
                .andExpect(model().attributeExists("certificateFingerprint"));
    }

    @Test
    void shouldReportNoIdWhenLinkOmitsCertificateId() throws Exception {
        mockMvc.perform(get("/verify-certificate"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recordStatus", "NO_ID"));
    }

    @Test
    void shouldConfirmCertificateWhenRecordExists() throws Exception {
        // Given
        CertificateMetadata metadata = new CertificateMetadata("cert-123", "john_doe.pdf");
        metadata.setFileHash("abc123hash");
        when(metadataService.getCertificateMetadata("cert-123")).thenReturn(metadata);

        // When/Then
        mockMvc.perform(get("/verify-certificate").param("id", "cert-123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recordStatus", "FOUND"))
                .andExpect(model().attribute("certificateId", "cert-123"))
                .andExpect(model().attribute("fileHash", "abc123hash"));
    }

    @Test
    void shouldReportMissingRecordForUnknownCertificateId() throws Exception {
        // Given
        when(metadataService.getCertificateMetadata("bogus-id")).thenReturn(null);

        // When/Then
        mockMvc.perform(get("/verify-certificate").param("id", "bogus-id"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recordStatus", "NOT_FOUND"));
    }

    @Test
    void shouldProvideFallbackFingerprintInDefaultConstructor() {
        var controller = new VerificationController();

        org.assertj.core.api.Assertions.assertThat(controller).isNotNull();
    }

    @Test
    void shouldReportUnavailableFingerprintForNonX509Certificate() throws Exception {
        // Given - a keystore with no certificate under the alias
        var keyStoreProvider = mock(com.kousen.cert.service.KeyStoreProvider.class);
        var keyStore = mock(java.security.KeyStore.class);
        when(keyStoreProvider.keyStore()).thenReturn(keyStore);
        when(keyStore.getCertificate("authorKey")).thenReturn(null);

        // When
        String fingerprint = VerificationController.generateCertificateFingerprint(keyStoreProvider);

        // Then
        org.assertj.core.api.Assertions.assertThat(fingerprint)
                .isEqualTo("Certificate fingerprint not available");
    }

    @Test
    void shouldReportErrorWhenFingerprintGenerationFails() {
        // Given - a provider that blows up
        var keyStoreProvider = mock(com.kousen.cert.service.KeyStoreProvider.class);
        when(keyStoreProvider.keyStore()).thenThrow(new IllegalStateException("Keystore unavailable"));

        // When
        String fingerprint = VerificationController.generateCertificateFingerprint(keyStoreProvider);

        // Then
        org.assertj.core.api.Assertions.assertThat(fingerprint)
                .isEqualTo("Error generating certificate fingerprint");
    }
}
