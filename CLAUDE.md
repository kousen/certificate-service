# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build: `./gradlew build`
- Run application: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "com.kousen.cert.service.KeyStoreProviderTest"`
- Run property tests: `./gradlew test --tests "com.kousen.cert.service.QrCodeGeneratorPropertyTest"`
- Clean build: `./gradlew clean build`

## Coverage Gate
- `./gradlew build` (and `check`) run JaCoCo coverage verification and FAIL if overall line coverage drops below 80%, or if any class in `com.kousen.cert.service` drops below 70% line coverage (PdfBoxGenerator is excluded — its gap is a defensive fallback that only runs when PDFBox fails to save)
- A build failure mentioning "Rule violated" means new code needs tests, not that the build is broken
- Coverage is measured excluding Application, config/**, and model/** (same exclusions as the report)

## Test Resources
- When creating tests, ensure that necessary resources (fonts, images, etc.) are also available in src/test/resources
- Property-based tests should use controlled parameter generation for cryptographic tests to avoid ASN.1 parsing issues

## Code Style Guidelines
- Java 25 (toolchain) with Spring Boot 3.5.7 (build uses Kotlin DSL `build.gradle.kts`)
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
- **Verification Flow**: Certificate IDs are generated in CertificateController BEFORE PDF creation and threaded through PdfService → QrCodeGenerator into the QR code URL (`&id=...`); the /verify-certificate page looks the ID up in CertificateMetadata and shows issue timestamp + SHA-256 of the issued file
- **Signature Verification**: PdfSignatureVerifier (bean in CertificateConfig) verifies CMS signatures of uploaded PDFs via POST /api/certificates/verify; GET /api/certificates/public-key serves the signing cert as PEM
- **Security**: SecurityConfig applies HTTP basic auth to /admin/**, /api/analytics/**, and /api/certificates/stored* ONLY when ADMIN_PASSWORD is set; with it unset (local dev, tests) everything is permitAll and CSRF is disabled. CertificateControllerTest must @Import(SecurityConfig.class) because @WebMvcTest otherwise applies Spring Security's locked-down defaults
- **Analytics Package**: Comprehensive tracking and dashboard system under `com.kousen.cert.analytics`
- **Database**: H2 for development, PostgreSQL for production via environment variables
- **Async Processing**: Event tracking uses @Async for performance
- **Web UI**: Thymeleaf templates with Bootstrap and Chart.js for analytics dashboard
- **Deployment**: Railway (railway.json); the signing keystore arrives base64-encoded in CERTIFICATE_KEYSTORE_B64 and is decoded to /tmp/keystore.p12 by the start command

## Database Configuration
- Uses environment variable overrides: DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD (see application.yaml). The JDBC driver and Hibernate dialect are auto-detected from the URL — there is no DATABASE_DRIVER property in the code.
- Hibernate DDL mode configurable via HIBERNATE_DDL_AUTO environment variable (defaults to `update`)
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