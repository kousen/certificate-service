package com.kousen.cert.service;

import com.kousen.cert.config.ServerUrlConfig;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for QrCodeGenerator
 */
class QrCodeGeneratorPropertyTest {
    
    // Custom class for testing the private encodeUrlParam method
    private static class EncodingTester {
        private final Method encodeUrlParamMethod;
        private final QrCodeGenerator generator;
        
        public EncodingTester() throws NoSuchMethodException {
            ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
            when(mockConfig.getUrl()).thenReturn("https://test-server.com");
            generator = new QrCodeGenerator(mockConfig);
            
            // Get access to the private encodeUrlParam method
            encodeUrlParamMethod = QrCodeGenerator.class.getDeclaredMethod("encodeUrlParam", String.class);
            encodeUrlParamMethod.setAccessible(true);
        }
        
        public String encode(String input) {
            try {
                return (String) encodeUrlParamMethod.invoke(generator, input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to encode: " + e.getMessage(), e);
            }
        }
        
        // Simple decode function to reverse the encoding process
        // This simulates how a server would decode the parameters
        public String decode(String encoded) {
            if (encoded == null) return "";
            return encoded.replace("%20", " ")
                         .replace("%26", "&")
                         .replace("%3D", "=")
                         .replace("%3F", "?");
        }
    }
    
    @Property
    void encodingPreservesOriginalStringAfterDecoding(
            @ForAll @NotBlank @StringLength(min = 1, max = 100) String input) throws Exception {
        // Arrange
        EncodingTester tester = new EncodingTester();
        
        // Act
        String encoded = tester.encode(input);
        String decoded = tester.decode(encoded);
        
        // Assert
        assertThat(decoded).isEqualTo(input);
    }
    
    @Property
    void specialCharactersAreEncodedCorrectly(
            @ForAll("specialCharInputs") String input) throws Exception {
        // Arrange
        EncodingTester tester = new EncodingTester();
        
        // Act
        String encoded = tester.encode(input);
        
        // Assert
        assertThat(encoded).doesNotContain(" ", "?", "&", "=");
        
        // Check each character is properly encoded
        if (input.contains(" ")) {
            assertThat(encoded).contains("%20");
        }
        if (input.contains("?")) {
            assertThat(encoded).contains("%3F");
        }
        if (input.contains("&")) {
            assertThat(encoded).contains("%26");
        }
        if (input.contains("=")) {
            assertThat(encoded).contains("%3D");
        }
    }
    
    @Provide
    Arbitrary<String> specialCharInputs() {
        // Generate strings with at least one special character
        Arbitrary<String> oneSpecialChar = Arbitraries.oneOf(
            Arbitraries.just(" "),
            Arbitraries.just("?"),
            Arbitraries.just("&"),
            Arbitraries.just("=")
        );
        
        // Then add random special chars
        return oneSpecialChar.flatMap(base ->
            Arbitraries.strings()
                .withChars(' ', '?', '&', '=')
                .ofMinLength(0)
                .ofMaxLength(10)
                .map(suffix -> base + suffix)
        );
    }
    
    @Property
    void encodingIsConsistentForTheSameInput(
            @ForAll @NotBlank @StringLength(min = 1, max = 100) String input) throws Exception {
        // Arrange
        EncodingTester tester = new EncodingTester();
        
        // Act
        String firstEncoding = tester.encode(input);
        String secondEncoding = tester.encode(input);
        
        // Assert
        assertThat(secondEncoding).isEqualTo(firstEncoding);
    }
    
    @Example
    void handlesNullInput() throws Exception {
        // Arrange
        EncodingTester tester = new EncodingTester();
        
        // Act
        String encoded = tester.encode(null);
        
        // Assert
        assertThat(encoded).isEmpty();
    }
    
    @Property
    void generatedUrlContainsAllRequiredParameters(
            @ForAll @NotBlank @StringLength(min = 1, max = 30) String name,
            @ForAll @NotBlank @StringLength(min = 1, max = 30) String book) throws Exception {
        // Arrange
        ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://test-server.com");
        
        QrCodeGenerator generator = new QrCodeGenerator(mockConfig);
        
        // Set up reflection to access the private method
        Method buildUrlMethod = QrCodeGenerator.class.
                getDeclaredMethod("buildVerificationUrl", String.class, String.class);
        buildUrlMethod.setAccessible(true);
        
        // Act
        String url = (String) buildUrlMethod.invoke(generator, name, book);
        
        // Assert
        assertThat(url)
            .startsWith("https://test-server.com/verify-certificate")
            .contains("name=", "book=", "date=");
    }
}