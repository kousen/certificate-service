package com.kousen.cert.analytics.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableJpaRepositories(basePackages = "com.kousen.cert.analytics.repository")
@EntityScan(basePackages = "com.kousen.cert.analytics.model")
public class AnalyticsConfig {
}