package com.kousen.cert.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@ComponentScan(
    basePackages = "com.kousen.cert",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.kousen\\.cert\\.analytics\\.config\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.kousen\\.cert\\.analytics\\.interceptor\\..*"
        )
    }
)
public class TestWebConfig {
}