package com.kousen.cert.config;

import com.kousen.cert.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Configuration class responsible for initializing server-specific URL values
 */
@Configuration
public class ServerUrlConfig {

    @Value("${server.url:}")
    private String configuredServerUrl;

    /**
     * Initialize the QrCode utility with the server's base URL
     */
    @PostConstruct
    public void init() {
        // First check if a URL was configured in application properties
        if (configuredServerUrl != null && !configuredServerUrl.isEmpty()) {
            QrCodeUtil.setServerBaseUrl(configuredServerUrl);
            return;
        }
        
        // Default to custom domain URL using HTTP for now
        // TODO: Set up SSL certificate for the custom domain and switch to HTTPS
        String customDomain = "http://certificate-service.kousenit.com";
        QrCodeUtil.setServerBaseUrl(customDomain);
    }
    
    /**
     * Returns the base URL for the server
     * @return the server's base URL
     */
    public String getUrl() {
        if (configuredServerUrl != null && !configuredServerUrl.isEmpty()) {
            return configuredServerUrl;
        }
        
        return "http://certificate-service.kousenit.com";
    }
}