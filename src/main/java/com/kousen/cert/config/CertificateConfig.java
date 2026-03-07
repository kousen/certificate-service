package com.kousen.cert.config;

import com.kousen.cert.service.KeyStoreProvider;
import com.kousen.cert.service.PdfSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Configuration
public class CertificateConfig implements WebMvcConfigurer {

    @Bean
    KeyStoreProvider keyStoreProvider(@Value("${certificate.keystore}") String keystoreLocation) {
        return new KeyStoreProvider(resolveKeyStorePath(keystoreLocation));
    }

    @Bean
    PdfSigner pdfSigner(KeyStoreProvider provider) {
        return new PdfSigner(provider);
    }

    private Path resolveKeyStorePath(String keystoreLocation) {
        if (keystoreLocation.startsWith("classpath:")) {
            String resourcePath = keystoreLocation.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try {
                Path tempFile = Files.createTempFile("certificate-keystore-", ".p12");
                try (var inputStream = resource.getInputStream()) {
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                tempFile.toFile().deleteOnExit();
                return tempFile;
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to resolve classpath keystore: " + keystoreLocation, e);
            }
        }
        return Paths.get(keystoreLocation);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Make fonts accessible
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/fonts/");
        
        // Make images accessible
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
        
        // Make CSS files accessible
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
    }
}
