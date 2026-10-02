package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CertificateMetadataServiceTest {

    @TempDir
    Path tempDir;

    private CertificateMetadataRepository repository;
    private CertificateMetadataService service;

    @BeforeEach
    void setUp() {
        repository = mock(CertificateMetadataRepository.class);
        service = new CertificateMetadataService(repository);
    }

    @Test
    void shouldSaveMetadataWithSizeAndHash() throws Exception {
        // Given
        Path certificate = tempDir.resolve("cert.pdf");
        Files.writeString(certificate, "PDF content for hashing");

        // When
        service.saveCertificateMetadata("cert-1", certificate).get();

        // Then
        ArgumentCaptor<CertificateMetadata> captor = ArgumentCaptor.forClass(CertificateMetadata.class);
        verify(repository).save(captor.capture());
        CertificateMetadata saved = captor.getValue();
        assertThat(saved.getCertificateId()).isEqualTo("cert-1");
        assertThat(saved.getFilename()).isEqualTo("cert.pdf");
        assertThat(saved.getFileSize()).isEqualTo(Files.size(certificate));
        assertThat(saved.getFileHash()).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void shouldSaveMetadataWithoutHashWhenFileMissing() throws Exception {
        // When
        service.saveCertificateMetadata("cert-2", tempDir.resolve("missing.pdf")).get();

        // Then
        ArgumentCaptor<CertificateMetadata> captor = ArgumentCaptor.forClass(CertificateMetadata.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFileHash()).isNull();
        assertThat(captor.getValue().getFileSize()).isNull();
    }

    @Test
    void shouldReturnMetadataById() {
        CertificateMetadata metadata = new CertificateMetadata("cert-3", "file.pdf");
        when(repository.findById("cert-3")).thenReturn(Optional.of(metadata));

        assertThat(service.getCertificateMetadata("cert-3")).isEqualTo(metadata);
        assertThat(service.getCertificateMetadata("unknown")).isNull();
    }

    @Test
    void shouldReturnMetadataByFilename() {
        CertificateMetadata metadata = new CertificateMetadata("cert-4", "file.pdf");
        when(repository.findByFilename("file.pdf")).thenReturn(Optional.of(metadata));

        assertThat(service.getCertificateMetadataByFilename("file.pdf")).isEqualTo(metadata);
        assertThat(service.getCertificateMetadataByFilename("nope.pdf")).isNull();
    }
}
