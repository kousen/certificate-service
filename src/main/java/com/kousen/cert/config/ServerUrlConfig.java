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
        
        // Use the custom domain with dynamic protocol detection
        // We'll try HTTPS first and fall back to HTTP if needed
        String customDomain = "certificate-service.kousenit.com";
        
        // First try to check if HTTPS is available by making a test connection
        boolean useHttps = true;
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) 
                    new java.net.URL("https://" + customDomain).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            useHttps = (responseCode < 400); // If we get a successful response, use HTTPS
        } catch (Exception e) {
            System.out.println("HTTPS not available for " + customDomain + ": " + e.getMessage());
            useHttps = false;
        }
        
        String protocol = useHttps ? "https" : "http";
        String baseUrl = protocol + "://" + customDomain;
        System.out.println("Using base URL: " + baseUrl);
        QrCodeUtil.setServerBaseUrl(baseUrl);
    }
    
    /**
     * Returns the base URL for the server
     * @return the server's base URL
     */
    public String getUrl() {
        if (configuredServerUrl != null && !configuredServerUrl.isEmpty()) {
            return configuredServerUrl;
        }
        
        // Use the same dynamic protocol detection as in init()
        String customDomain = "certificate-service.kousenit.com";
        boolean useHttps = false;
        
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) 
                    new java.net.URL("https://" + customDomain).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);  // Short timeout
            connection.connect();
            int responseCode = connection.getResponseCode();
            useHttps = (responseCode < 400);
        } catch (Exception e) {
            // HTTPS not available, use HTTP
            useHttps = false;
        }
        
        String protocol = useHttps ? "https" : "http";
        return protocol + "://" + customDomain;
    }
}