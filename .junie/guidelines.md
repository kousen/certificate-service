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
- Java 21 with Spring Boot 3.3.1
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