package com.kousen.cert.service;

import com.kousen.cert.config.ServerUrlConfig;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility to create a QrCodeGenerator instance for tests
 */
public class MockQrCodeGenerator {

    /**
     * Creates a real QrCodeGenerator with mock ServerUrlConfig
     * to use in tests
     * 
     * @return QrCodeGenerator instance
     */
    public static QrCodeGenerator createMock() {
        // Create a mock ServerUrlConfig
        ServerUrlConfig mockConfig = Mockito.mock(ServerUrlConfig.class);
        Mockito.when(mockConfig.getUrl()).thenReturn("https://test.example.com");
        
        // Create a real QrCodeGenerator with the mock config
        return new QrCodeGenerator(mockConfig);
    }
    
    /**
     * Creates a fully mocked QrCodeGenerator that returns null for the QR code path,
     * which will trigger the fallback behavior in PdfBoxGenerator
     * 
     * @return mock QrCodeGenerator
     */
    public static QrCodeGenerator createFullMock() {
        // Create a mock QrCodeGenerator that returns null for the QR code path
        QrCodeGenerator mockGenerator = Mockito.mock(QrCodeGenerator.class);
        try {
            Mockito.when(mockGenerator.generateQrCode(
                    Mockito.anyString(), 
                    Mockito.anyString(), 
                    Mockito.anyInt()))
                .thenReturn(null);
        } catch (Exception e) {
            System.err.println("Error creating mock: " + e.getMessage());
        }
        
        return mockGenerator;
    }
}