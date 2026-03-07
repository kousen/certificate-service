package com.kousen.cert.analytics.model;

import jakarta.servlet.http.HttpServletRequest;

public record AnalyticsRequestContext(String ipAddress, String userAgent) {

    public static AnalyticsRequestContext from(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = (xForwardedFor != null && !xForwardedFor.isEmpty())
                ? xForwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
        return new AnalyticsRequestContext(ipAddress, request.getHeader("User-Agent"));
    }
}
