# Certificate Service (Java 21 + Spring Boot 3.3)

Generates a personalised **Certificate of Ownership** PDF, signs it with an RSA self‑signed certificate, and returns it from a REST endpoint.

## Run locally

```bash
export CERT_PWD=changeit   # first time only
./gradlew bootRun
curl -X POST http://localhost:8080/api/certificates \
     -H "Content-Type: application/json" \
     -d '{"purchaserName":"Ada Lovelace","bookTitle":"Modern Java Recipes"}' \
     -o ada.pdf
open ada.pdf
```

## Deploy to Heroku

```bash
heroku create certificate-service
git push heroku main
```

## Add your assets

Place these under `src/main/resources`:

```
fonts/CinzelDecorative-Regular.ttf
fonts/GreatVibes-Regular.ttf
images/parchment.jpg
images/border.png
images/signature.png   (already included as placeholder)
```
