package com.kousen.cert.template;

import com.kousen.cert.model.CertificateRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ElegantTemplateTest {

    @Test
    void templateShouldRenderPurchaserNameAndBookTitle() {
        // Given
        ElegantTemplate template = new ElegantTemplate();
        CertificateRequest request = new CertificateRequest(
                "Jean-Luc Picard",
                "Gradle Recipes for Android",
                Optional.of("jl@example.com")
        );
        
        // When
        String html = template.html(request);
        
        // Then
        assertThat(html).isNotNull();
        assertThat(html).contains("Jean-Luc Picard");  // Encoded purchaser name
        assertThat(html).contains("Gradle Recipes for Android"); // Encoded book title
    }
    
    @Test
    void templateShouldHandleSpecialCharactersInInput() {
        // Given
        ElegantTemplate template = new ElegantTemplate();
        CertificateRequest request = new CertificateRequest(
                "John & Jane Doe", 
                "Java & Spring <Guide>", 
                Optional.empty()
        );
        
        // When
        String html = template.html(request);
        
        // Then
        assertThat(html).isNotNull();
        assertThat(html).contains("John &amp; Jane Doe");  // Encoded ampersand
        assertThat(html).contains("Java &amp; Spring &lt;Guide&gt;"); // Encoded special chars
    }
}