package com.kousen.cert.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "certificate.keystore=classpath:test-keystore.p12"
})
class VerificationControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public VerificationController verificationController() {
            // Use a hardcoded certificate fingerprint for testing
            return new VerificationController() {
                @Override
                protected String generateCertificateFingerprint(String keystorePath) {
                    return "Test certificate fingerprint";
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

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
}