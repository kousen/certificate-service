package com.kousen.cert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import com.kousen.cert.config.SecurityConfig;
import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.service.CertificateStorageService;
import com.kousen.cert.service.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument; // Import PDFBox
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CertificateController.class)
@Import(SecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PdfService pdfService;
    
    @MockitoBean
    private CertificateStorageService storageService;
    
    @MockitoBean
    private AnalyticsService analyticsService;
    
    @MockitoBean
    private CertificateMetadataService metadataService;

    @Test
    void shouldCreateCertificateAndReturnPdf() throws Exception {
        // Given
        CertificateRequest request = new CertificateRequest(
                "Adm. Grace Hopper",
                "Making Java Groovy",
                Optional.of("grace@example.com")
        );

        // Create a temporary real PDF file for the mock using PDFBox
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(tempPdf.toFile());
        }

        // Store to a separate path so the controller's temp-file cleanup
        // doesn't delete the "stored" copy
        Path storedPdf = Files.createTempFile("test-cert-stored-", ".pdf");
        Files.copy(tempPdf, storedPdf, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        when(pdfService.createPdf(any(), any())).thenReturn(tempPdf);
        when(storageService.storeCertificate(any(), any())).thenReturn(storedPdf);

        // When/Then
        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        // Clean up
        Files.deleteIfExists(tempPdf);
        Files.deleteIfExists(storedPdf);
    }

    @Test
    void shouldReturnPublicCertificateAsPem() throws Exception {
        mockMvc.perform(get("/api/certificates/public-key"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("-----BEGIN CERTIFICATE-----")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("-----END CERTIFICATE-----")));
    }

    @Test
    void shouldReportMissingSignatureForUnsignedPdf() throws Exception {
        // Given - a valid but unsigned PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(baos);
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "unsigned.pdf", MediaType.APPLICATION_PDF_VALUE, baos.toByteArray());

        // When/Then
        mockMvc.perform(multipart("/api/certificates/verify").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signaturePresent").value(false))
                .andExpect(jsonPath("$.documentIntact").value(false));
    }

    @Test
    void shouldRejectEmptyUploadForVerification() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/certificates/verify").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnSignatureInfo() throws Exception {
        mockMvc.perform(get("/api/certificates/signature-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateType").value("Self-signed X.509"))
                .andExpect(jsonPath("$.signatureAlgorithm").value("SHA512withRSA"))
                .andExpect(jsonPath("$.keySize").value("4096 bits"));
    }

    @Test
    void shouldReturn404WhenStoredCertificateNotFound() throws Exception {
        when(storageService.getCertificate("missing.pdf"))
                .thenThrow(new java.io.IOException("Certificate not found: missing.pdf"));

        mockMvc.perform(get("/api/certificates/stored/missing.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500WhenListingFails() throws Exception {
        when(storageService.listAllCertificates())
                .thenThrow(new java.io.IOException("Disk on fire"));

        mockMvc.perform(get("/api/certificates/stored"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldTrackDownloadWhenMetadataExists() throws Exception {
        // Given
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try {
            Files.writeString(tempPdf, "Test PDF content");
            String filename = tempPdf.getFileName().toString();
            when(storageService.getCertificate(filename)).thenReturn(tempPdf);
            var metadata = new com.kousen.cert.analytics.model.CertificateMetadata("cert-9", filename);
            when(metadataService.getCertificateMetadataByFilename(filename)).thenReturn(metadata);

            // When/Then
            mockMvc.perform(get("/api/certificates/stored/" + filename))
                    .andExpect(status().isOk());

            org.mockito.Mockito.verify(analyticsService)
                    .trackCertificateDownloaded(org.mockito.ArgumentMatchers.eq("cert-9"), any());
        } finally {
            Files.deleteIfExists(tempPdf);
        }
    }

    @Test
    void shouldTrackErrorWhenGenerationFails() throws Exception {
        // Given
        CertificateRequest request = new CertificateRequest(
                "Failing User",
                "Modern Java Recipes",
                Optional.empty()
        );
        when(pdfService.createPdf(any(), any()))
                .thenThrow(new java.io.IOException("Font exploded"));

        // When - the exception propagates out of the controller
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                mockMvc.perform(post("/api/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .hasMessageContaining("Font exploded");

        // Then - the failure was tracked
        org.mockito.Mockito.verify(analyticsService)
                .trackCertificateError(org.mockito.ArgumentMatchers.contains("Font exploded"), any());
    }
    
    @Test
    void shouldRejectInvalidBookTitle() throws Exception {
        // Given
        String requestJson = """
                {
                    "purchaserName": "Test User",
                    "bookTitle": "Invalid Book Title",
                    "purchaserEmail": "test@example.com"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.bookTitle").exists());
    }
    
    @Test
    void shouldReturnAvailableBooks() throws Exception {
        mockMvc.perform(get("/api/certificates/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBooks").isArray())
                .andExpect(jsonPath("$.availableBooks.length()").value(6));
    }
    
    @Test
    void shouldListStoredCertificates() throws Exception {
        // Given
        List<Path> mockCertificates = List.of(
            Paths.get("/test/cert1.pdf"),
            Paths.get("/test/cert2.pdf")
        );
        when(storageService.listAllCertificates()).thenReturn(mockCertificates);
        when(storageService.getStoragePath()).thenReturn(Paths.get("/test"));
        
        // When/Then
        mockMvc.perform(get("/api/certificates/stored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificates").isArray())
                .andExpect(jsonPath("$.count").value(2));
    }
    
    @Test
    void shouldGetStoredCertificate() throws Exception {
        // Given - create a temporary real file for testing
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try {
            // Create a simple PDF file
            Files.writeString(tempPdf, "Test PDF content");
            
            // Set up the mock to return the real file
            String filename = tempPdf.getFileName().toString();
            when(storageService.getCertificate(filename)).thenReturn(tempPdf);
            
            // When/Then - should work with a real file
            mockMvc.perform(get("/api/certificates/stored/" + filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        } finally {
            // Clean up
            Files.deleteIfExists(tempPdf);
        }
    }
}