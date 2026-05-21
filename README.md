# Certificate Service

A Spring Boot application that generates personalized certificates of ownership for books, complete with digital signatures and QR code verification.

## Live Demo

A deployed version of this service is available at:
**https://certificate-service.kousenit.com**

You can try the certificate generation feature directly on the web interface.

## Overview

This service generates professionally designed certificates of ownership for book purchases. Each certificate:

- Is personalized with the purchaser's name
- Includes the book title
- Features an elegant background design
- Is digitally signed with a 4096-bit RSA key
- Includes a QR code for online verification

## Features

- **PDF Generation**: Creates beautiful certificates using HTML templates rendered to PDF
- **Digital Signatures**: Each PDF is cryptographically signed for authenticity
- **Certificate Verification**: QR codes link to a verification page with signature details
- **Certificate Storage**: Maintains copies of all generated certificates
- **Analytics Dashboard**: Comprehensive analytics with charts, metrics, and usage tracking
- **RESTful API**: Simple API for certificate generation and retrieval

## Technologies

- Java 25 (with virtual threads)
- Spring Boot 3.5
- Spring Data JPA with Hibernate
- H2 Database (development) / PostgreSQL (production)
- PDFBox 3.0 for PDF manipulation and signing
- BouncyCastle for cryptography
- ZXing for QR code generation
- Thymeleaf templating for web UI
- Chart.js for analytics visualizations
- jqwik for property-based testing

## API Endpoints

### Create a Certificate

```
POST /api/certificates
```

Request body:
```json
{
  "purchaserName": "Ada Lovelace",
  "bookTitle": "Modern Java Recipes",
  "purchaserEmail": "ada@example.com"
}
```

`purchaserEmail` is optional; `bookTitle` must be one of the allowed titles (see below).

Response: PDF certificate file (also returns `X-Certificate-Id` and `X-Certificate-Status` headers)

### Available Book Titles

```
GET /api/certificates/books
```

Returns the list of available book titles that can be used in certificate generation. The `bookTitle` in a create request must match one of these exactly. Currently allowed titles: Gradle Recipes for Android, Help Your Boss Help You, Kotlin Cookbook, Making Java Groovy, Mockito Made Clear, Modern Java Recipes.

### List Stored Certificates

```
GET /api/certificates/stored
```

Returns a list of all stored certificates.

### Retrieve a Stored Certificate

```
GET /api/certificates/stored/{filename}
```

Returns a specific stored certificate by filename.

### Signature Information

```
GET /api/certificates/signature-info
```

Returns JSON describing the signing certificate (self-signed X.509, SHA512withRSA, 4096-bit key) and notes about reader warnings.

### Certificate Verification

```
GET /verify-certificate
```

Displays certificate verification information and instructions for validating the digital signature.

### Analytics Dashboard

```
GET /admin/dashboard
```

Web interface showing comprehensive analytics including:
- Certificate generation metrics and trends
- Book popularity statistics
- Performance metrics and response times
- Recent activity logs

### Analytics API

```
GET /api/analytics/dashboard    # Complete dashboard data
GET /api/analytics/summary      # Summary statistics
GET /api/analytics/trends       # Daily trend data
GET /api/analytics/books        # Book popularity data
GET /api/analytics/performance  # Performance metrics
```

Returns JSON data for programmatic access to analytics.

## Running Locally

### Prerequisites

- JDK 25 (the Gradle toolchain targets Java 25; a JDK 25 must be available or Gradle will attempt to provision one)
- No separate Gradle install needed — use the bundled wrapper (`./gradlew`), which uses Gradle 9.4

No manual keystore or certificate setup is required: on first run the app generates a self-signed PKCS#12 keystore automatically (see "Signing Keystore" below).

### Database Configuration

The application uses an H2 in-memory database for local development and PostgreSQL for production. The JDBC driver and Hibernate dialect are auto-detected from the datasource URL, so no driver property needs to be set.

- **Development**: With no environment variables set, the app falls back to an in-memory H2 database (`jdbc:h2:mem:testdb`, user `sa`, empty password). The H2 web console is enabled at `/h2-console`.
- **Production**: Set `DATABASE_URL` (and, if needed, `DATABASE_USERNAME` / `DATABASE_PASSWORD`) to point at PostgreSQL. The `application-production.yaml` profile disables the H2 console and tunes the HikariCP pool. Note: on Heroku, the platform-provided `DATABASE_URL` is consumed automatically.

### Environment Variables

All of the following are optional; defaults shown are from `src/main/resources/application.yaml`.

| Variable | Default | Description |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:h2:mem:testdb` | JDBC datasource URL. Set to a PostgreSQL URL in production. |
| `DATABASE_USERNAME` | `sa` | Datasource username. |
| `DATABASE_PASSWORD` | (empty) | Datasource password. |
| `HIBERNATE_DDL_AUTO` | `update` | Hibernate DDL mode (`update`, `validate`, etc.). |
| `SERVER_URL` | the deployed Heroku URL | Base server URL; used as the default for the QR-code verification base URL. |
| `CERTIFICATE_VERIFICATION_BASE_URL` | value of `SERVER_URL` | Absolute base URL embedded in generated QR-code verification links. |
| `CERTIFICATE_KEYSTORE` | `${user.home}/.cert_keystore.p12` | Path to the PKCS#12 signing keystore (auto-created if absent). |
| `CERTIFICATE_STORAGE_PATH` | `${user.home}/certificate-service/certificates` | Directory where generated certificates are stored. |
| `CERT_PWD` | `changeit` | Password for the signing keystore. |
| `SPRING_PROFILES_ACTIVE` | (none) | Set to `production` to activate the production profile. |

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/certificate-service.git
   cd certificate-service
   ```

2. Build the application
   ```bash
   ./gradlew build
   ```

3. Run the application
   ```bash
   ./gradlew bootRun
   ```

### Configuration

See the Environment Variables table above for all settings. As an example, the service relies on the `certificate.verification.base-url` property to generate absolute QR code links. By default it inherits the `server.url` setting, but you can override it in one of two ways:
1. Set the environment variable (dots become underscores):
   ```bash
   export CERTIFICATE_VERIFICATION_BASE_URL=https://yourdomain.com
   ```
2. Override in `src/main/resources/application.yaml`:
   ```yaml
   certificate:
     verification:
       base-url: https://yourdomain.com
   ```

### Signing Keystore

No manual keystore setup is needed. On startup, if no keystore exists at the configured path (`CERTIFICATE_KEYSTORE`, default `${user.home}/.cert_keystore.p12`), the application generates a self-signed 4096-bit RSA certificate (SHA512withRSA) and stores it as a PKCS#12 file. The keystore password is read from the `CERT_PWD` environment variable (or the `CERT_PWD` system property), defaulting to `changeit`. If a keystore already exists at that path, it is loaded with the same password instead of being regenerated.

### Testing

This project uses both traditional unit tests and property-based testing:

1. Run all tests
   ```bash
   ./gradlew test
   ```

2. Run specific property-based tests
   ```bash
   ./gradlew test --tests "com.kousen.cert.service.QrCodeGeneratorPropertyTest"
   ```
3. Run integration (end-to-end) tests
   ```bash
   ./gradlew test --tests "*IntegrationTest"
   ```

Property-based testing systematically tests properties of the application with many random inputs, helping to discover edge cases that traditional unit tests might miss.

4. Generate a certificate (example using curl)
   ```bash
   curl -X POST http://localhost:8080/api/certificates \
        -H "Content-Type: application/json" \
        -d '{"purchaserName":"Ada Lovelace","bookTitle":"Modern Java Recipes"}' \
        -o ada.pdf
   ```

5. Open the generated PDF
   ```bash
   open ada.pdf
   ```

6. View analytics dashboard
   ```
   http://localhost:8080/admin/dashboard
   ```

## Deployment

### Heroku Deployment

The application is configured for easy Heroku deployment with PostgreSQL:

1. **Add PostgreSQL**: `heroku addons:create heroku-postgresql:essential-0`
2. **Set Environment Variables**:
   ```bash
   heroku config:set SPRING_PROFILES_ACTIVE=production
   heroku config:set HIBERNATE_DDL_AUTO=validate
   ```
3. **Deploy**: Standard git push to Heroku

The application automatically detects the Heroku PostgreSQL `DATABASE_URL` and switches from H2 to persistent storage.

## Notes on Digital Signatures

The certificates are signed with a self-signed certificate, which means that Adobe Reader and other PDF readers may display warnings about the signature's validity. This is normal and doesn't affect the integrity of the signature itself. The verification page accessible via the QR code explains how to interpret these warnings.

## Font and Image Assets

Place your custom assets under `src/main/resources`:

```
fonts/CinzelDecorative-Regular.ttf
fonts/GreatVibes-Regular.ttf
images/certificate-bg.png
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Ken Kousen - Tales from the Jar Side