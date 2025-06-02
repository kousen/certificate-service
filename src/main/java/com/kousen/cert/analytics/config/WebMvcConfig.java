package com.kousen.cert.analytics.config;

import com.kousen.cert.analytics.interceptor.ApiTrackingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final ApiTrackingInterceptor apiTrackingInterceptor;
    
    public WebMvcConfig(ApiTrackingInterceptor apiTrackingInterceptor) {
        this.apiTrackingInterceptor = apiTrackingInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Track all API endpoints
        registry.addInterceptor(apiTrackingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/analytics/**"); // Don't track analytics endpoints
    }
}