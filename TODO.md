# TODO List for Certificate Service

This document lists recommended cleanup tasks and improvements for the certificate-service project.

## High Priority

### 1. Replace System.out/System.err with Proper Logging
- [ ] Replace direct console output with SLF4J/Logback logging in:
  - `PdfBoxGenerator.java` (lines with System.out.println)
  - `QrCodeUtil.java` (error handling)
  - `ServerUrlConfig.java` (initialization logs)
  - `KeyStoreProvider.java` (debug outputs)
- [ ] Add appropriate log levels (INFO, DEBUG, ERROR) based on message importance
- [ ] Consider adding structured logging for important operations

### 2. Externalize Hardcoded Values to Configuration
- [ ] Move hardcoded URLs to application.yaml config:
  - `QrCodeUtil.java`: "https://certificate-service-997e5d9f565a.herokuapp.com"
  - `ServerUrlConfig.java`: "certificate-service.kousenit.com"
- [ ] Create proper configuration properties class with validation
- [ ] Add environment-specific configurations (dev, test, prod)

### 3. Improve Security of Password Handling
- [ ] Review password handling in `KeyStoreProvider.java`
- [ ] Use secure credential provider or key vault for sensitive information
- [ ] Ensure passwords are cleared from memory when no longer needed
- [ ] Add appropriate security headers to API responses

## Medium Priority

### 4. Reduce Code Duplication
- [ ] Consolidate duplicated protocol detection logic in `ServerUrlConfig.java`
- [ ] Extract common certificate rendering code in `PdfBoxGenerator.java` into helper methods
- [ ] Create utility methods for repeated operations

### 5. Improve Exception Handling
- [ ] Replace broad catch blocks with specific exception types
- [ ] Add contextual information to exceptions
- [ ] Implement consistent error responses across the API
- [ ] Add validation for all incoming request data

### 6. Fix Resource Management
- [ ] Use try-with-resources for all IO operations
- [ ] Ensure HTTP connections are properly closed in `ServerUrlConfig.java`
- [ ] Add cleanup for temporary files in error scenarios
- [ ] Implement proper resource disposal in all service classes

### 7. Strengthen Dependency Injection
- [ ] Replace direct instantiation with proper Spring dependency injection
- [ ] Use constructor injection consistently throughout the application
- [ ] Configure appropriate bean scopes for components

## Lower Priority

### 8. Enhance Documentation
- [ ] Add Javadoc to all public methods
- [ ] Document security considerations for certificate handling
- [ ] Add architectural diagrams to explain component relationships
- [ ] Improve API documentation with examples

### 9. Review Dependencies
- [ ] Ensure all dependencies are current
- [ ] Remove any unused dependencies
- [ ] Check for security vulnerabilities using OWASP dependency checker
- [ ] Consider upgrading BouncyCastle and PDFBox to latest versions

### 10. Performance Optimizations
- [ ] Cache fonts in `PdfBoxGenerator.java` instead of loading for each generation
- [ ] Add metrics for certificate generation time
- [ ] Profile the application to identify bottlenecks
- [ ] Consider adding caching for frequently generated certificates

## Future Enhancements

### 11. Testing Improvements
- [ ] Increase test coverage for error paths
- [ ] Add load testing for PDF generation
- [ ] Add security testing for certificate validation
- [ ] Create more sophisticated property-based tests

### 12. User Experience
- [ ] Improve verification page design
- [ ] Add certificate preview functionality
- [ ] Support additional certificate templates
- [ ] Add internationalization support

### 13. DevOps
- [ ] Set up CI/CD pipeline with automated testing
- [ ] Add infrastructure-as-code for deployment
- [ ] Configure monitoring and alerting
- [ ] Set up automated security scanning