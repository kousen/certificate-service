package com.kousen.cert;

import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockBeanConfiguration {
    
    @Bean
    @Primary
    public MeterRegistry testMeterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    @Primary
    public AnalyticsService mockAnalyticsService() {
        return mock(AnalyticsService.class);
    }
    
    @Bean
    @Primary
    public CertificateMetadataService mockCertificateMetadataService() {
        return mock(CertificateMetadataService.class);
    }
}