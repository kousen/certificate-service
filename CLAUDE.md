# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build: `./gradlew build`
- Run application: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "com.kousen.cert.service.KeyStoreProviderTest"`
- Run property tests: `./gradlew test --tests "com.kousen.cert.service.QrCodeGeneratorPropertyTest"`
- Clean build: `./gradlew clean build`

## Test Resources
- When creating tests, ensure that necessary resources (fonts, images, etc.) are also available in src/test/resources
- Property-based tests should use controlled parameter generation for cryptographic tests to avoid ASN.1 parsing issues

## Code Style Guidelines
- Java 21 with Spring Boot 3.4.4
- Class names use PascalCase, method/variable names use camelCase
- Organize imports: java.*, javax.*, org.*, com.* (alphabetical within groups)
- Prefer Optional<T> over null for optional values
- Use explicit constructor injection for Spring dependencies
- Exception handling: specific exceptions with meaningful messages
- Test naming: descriptive method names with 'should' prefix
- Use static imports for JUnit and Mockito methods
- Maintain separation of concerns (controller/service/model)
- Use final for immutable fields
- Use records for simple data classes wherever possible
- Remove unused imports

## Architecture Overview
- **Main Application**: Certificate generation with PDF signing and QR codes
- **Analytics Package**: Comprehensive tracking and dashboard system under `com.kousen.cert.analytics`
- **Database**: H2 for development, PostgreSQL for production via environment variables
- **Async Processing**: Event tracking uses @Async for performance
- **Web UI**: Thymeleaf templates with Bootstrap and Chart.js for analytics dashboard

## Database Configuration
- Uses environment variable overrides: DATABASE_URL, DATABASE_DRIVER, DATABASE_USERNAME, etc.
- Hibernate DDL mode configurable via HIBERNATE_DDL_AUTO environment variable
- Production profile available in application-production.yaml
- Analytics tables: certificate_events, certificate_metadata, aggregated_metrics

## Analytics Features
- Real-time event tracking for certificate generation, verification, and downloads
- Dashboard at /admin/dashboard with charts and metrics
- REST API endpoints under /api/analytics/ for programmatic access
- Async event processing to avoid blocking main application flow
- Metrics aggregation service for performance optimization

## Database Compatibility Notes
- SQL queries must work with both H2 and PostgreSQL
- Use CAST() functions instead of database-specific functions (e.g., CAST(timestamp AS DATE) not DATE())
- JPA @Query ORDER BY clauses should reference entity attributes, not SELECT aliases