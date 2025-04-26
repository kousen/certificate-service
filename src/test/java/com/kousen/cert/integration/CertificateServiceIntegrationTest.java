package com.kousen.cert.integration;

import com.kousen.cert.model.CertificateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Certificate Service API.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Use temp locations to avoid polluting user home
                "certificate.storage.path=${java.io.tmpdir}/cert-test-storage",
                "certificate.keystore=${java.io.tmpdir}/cert-test-keystore.p12"
        }
)
public class CertificateServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void availableBooksEndpoint() {
        String url = "http://localhost:" + port + "/api/certificates/books";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).contains("availableBooks");
    }

    @Test
    void createCertificateReturnsPdf() {
        String url = "http://localhost:" + port + "/api/certificates";
        CertificateRequest req = new CertificateRequest(
                "Integration Test User",
                "Modern Java Recipes",
                Optional.of("test@example.com")
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CertificateRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        byte[] body = response.getBody();
        assertThat(body).isNotNull().hasSizeGreaterThan(100);
        // PDF files start with "%PDF"
        String header = new String(body, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }
}