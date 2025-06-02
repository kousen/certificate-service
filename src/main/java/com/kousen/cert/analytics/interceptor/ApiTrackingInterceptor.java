package com.kousen.cert.analytics.interceptor;

import com.kousen.cert.analytics.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to track API usage and response times.
 */
@Component
public class ApiTrackingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiTrackingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "apiTrackingStartTime";
    
    private final AnalyticsService analyticsService;
    
    public ApiTrackingInterceptor(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record start time
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Not used
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // Calculate response time
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                String endpoint = request.getRequestURI();
                
                // Extract request info before async operation (to avoid recycled request issues)
                String ipAddress = extractIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                
                // Track API usage with pre-extracted data
                analyticsService.trackApiUsageWithExtractedData(endpoint, responseTime, ipAddress, userAgent);
                
                logger.debug("API call to {} took {}ms", endpoint, responseTime);
            }
        } catch (Exception e) {
            logger.warn("Error tracking API usage: {}", e.getMessage());
        }
    }
    
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}