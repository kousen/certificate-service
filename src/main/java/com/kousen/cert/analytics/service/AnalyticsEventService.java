package com.kousen.cert.analytics.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for tracking analytics events throughout the application.
 * This service delegates to AnalyticsService for actual event tracking.
 */
@Service
public class AnalyticsEventService {
    private final AnalyticsService analyticsService;

    public AnalyticsEventService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Track certificate generation event.
     *
     * @param certificateId  The unique ID of the certificate
     * @param purchaserName  The name of the purchaser
     * @param purchaserEmail The email of the purchaser
     * @param bookTitle      The title of the book
     * @param durationMs     The time taken to generate the certificate in milliseconds
     * @param request        The HTTP request
     * @return CompletableFuture for async processing
     */
    public CompletableFuture<Void> trackCertificateGeneration(
            String certificateId,
            String purchaserName,
            String purchaserEmail,
            String bookTitle,
            long durationMs,
            HttpServletRequest request) {
        return analyticsService.trackCertificateGenerated(
                certificateId,
                purchaserName,
                purchaserEmail,
                bookTitle,
                durationMs,
                request
        );
    }

    /**
     * Track certificate verification event.
     *
     * @param certificateId The unique ID of the certificate
     * @param request       The HTTP request
     * @return CompletableFuture for async processing
     */
    public CompletableFuture<Void> trackCertificateVerification(
            String certificateId,
            HttpServletRequest request) {
        return analyticsService.trackCertificateVerified(certificateId, request);
    }

    /**
     * Track certificate download event.
     *
     * @param certificateId The unique ID of the certificate
     * @param request       The HTTP request
     * @return CompletableFuture for async processing
     */
    public CompletableFuture<Void> trackCertificateDownload(
            String certificateId,
            HttpServletRequest request) {
        return analyticsService.trackCertificateDownloaded(certificateId, request);
    }

    /**
     * Track certificate generation error.
     *
     * @param errorMessage The error message
     * @param request      The HTTP request
     * @return CompletableFuture for async processing
     */
    public CompletableFuture<Void> trackCertificateError(
            String errorMessage,
            HttpServletRequest request) {
        return analyticsService.trackCertificateError(errorMessage, request);
    }

    /**
     * Track API usage.
     * This method will be called by the API tracking interceptor.
     *
     * @param endpoint     The API endpoint
     * @param responseTime The response time in milliseconds
     * @param request      The HTTP request
     * @return CompletableFuture for async processing
     */
    public CompletableFuture<Void> trackApiUsage(
            String endpoint,
            long responseTime,
            HttpServletRequest request) {
        return analyticsService.trackApiUsage(endpoint, responseTime, request);
    }
}
