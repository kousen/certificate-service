package com.kousen.cert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.service.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument; // Import PDFBox
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CertificateController.class)
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PdfService pdfService;

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

        when(pdfService.createPdf(any(), any())).thenReturn(tempPdf);

        // When/Then
        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        // Clean up
        Files.deleteIfExists(tempPdf);
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
}