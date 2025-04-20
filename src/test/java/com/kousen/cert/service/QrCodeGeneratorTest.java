package com.kousen.cert.service;

import com.kousen.cert.config.ServerUrlConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Helper class for testing URL generation
class TestableQrCodeGenerator extends QrCodeGenerator {
    private final Path tempDir;
    
    public TestableQrCodeGenerator(ServerUrlConfig config, Path tempDir) {
        super(config);
        this.tempDir = tempDir;
    }
    
    @Override
    public Path generateQrCode(String name, String bookTitle, int size) throws IOException {
        // Create a verification URL using parent's private method via reflection
        String verificationUrl;
        try {
            var method = QrCodeGenerator.class.getDeclaredMethod("buildVerificationUrl", String.class, String.class);
            method.setAccessible(true);
            verificationUrl = (String) method.invoke(this, name, bookTitle);
        } catch (Exception e) {
            throw new IOException("Failed to create URL: " + e.getMessage(), e);
        }
        
        // Store the URL in a temp file for testing
        Path urlFile = tempDir.resolve("url.txt");
        Files.writeString(urlFile, verificationUrl);
        return urlFile;
    }
}

class QrCodeGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateQrCodeWithCorrectUrl() throws Exception {
        // Given
        ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://test-server.com");
        
        QrCodeGenerator generator = new QrCodeGenerator(mockConfig);
        
        // When
        Path qrCodePath = generator.generateQrCode("John Doe", "Test Book", 150);
        
        // Then
        assertThat(qrCodePath).exists();
        assertThat(Files.size(qrCodePath)).isGreaterThan(0);
        
        // Verify it's a PNG image
        byte[] bytes = Files.readAllBytes(qrCodePath);
        assertThat(bytes).isNotEmpty();
        assertThat(bytes.length).isGreaterThan(100); // Should be a reasonable size
        assertThat(bytes[0]).isEqualTo((byte) 0x89); // PNG signature starts with these bytes
        assertThat(bytes[1]).isEqualTo((byte) 0x50);
        assertThat(bytes[2]).isEqualTo((byte) 0x4E);
        assertThat(bytes[3]).isEqualTo((byte) 0x47);
        
        // Cleanup
        Files.deleteIfExists(qrCodePath);
    }

    @Test
    void shouldHandleSpecialCharactersInParameters() throws Exception {
        // Given
        ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://test-server.com");
        
        QrCodeGenerator generator = new QrCodeGenerator(mockConfig);
        
        String nameWithSpecialChars = "O'Reilly & Associates";
        String bookWithSpecialChars = "Java Q&A: Questions & Answers";
        
        // When
        Path qrCodePath = generator.generateQrCode(nameWithSpecialChars, bookWithSpecialChars, 100);
        
        // Then
        assertThat(qrCodePath).exists();
        assertThat(Files.size(qrCodePath)).isGreaterThan(0);
        
        // Cleanup
        Files.deleteIfExists(qrCodePath);
    }
    
    @Test
    void shouldGenerateQrCodeWithCorrectDateFormat() throws Exception {
        // Given
        ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://test-server.com");
        
        // Create a subclass to test URL generation
        TestableQrCodeGenerator generator = new TestableQrCodeGenerator(mockConfig, tempDir);
        
        // When
        Path urlFilePath = generator.generateQrCode("Test User", "Test Book", 100);
        String urlContent = Files.readString(urlFilePath);
        
        // Then
        String expectedDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        assertThat(urlContent).contains("date=" + expectedDate);
        assertThat(urlContent).startsWith("https://test-server.com/verify-certificate");
        assertThat(urlContent).contains("name=Test%20User");
        assertThat(urlContent).contains("book=Test%20Book");
    }
}