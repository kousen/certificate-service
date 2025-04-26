package com.kousen.cert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for certificate verification URLs.
 */
@Configuration
public class ServerUrlConfig {

    @Value("${certificate.verification.base-url:}")
    private String baseUrl;

    /**
     * Returns the configured base URL for certificate verification.
     */
    public String getUrl() {
        return baseUrl;
    }
}