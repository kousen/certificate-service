# [Branch: `gag-features`] How to Run and Experience the Over-Engineered Gag

This branch contains the "Nuclear-Grade" over-engineered features for the Certificate Service gag. Follow these steps to see the blockchain, biometric analysis, and quantum-resistant hashing in action.

## 1. Ensure You're on the Right Branch
First, make sure you've checked out the branch where all the new services live:
```bash
git checkout gag-features
```

## 2. Start the Application
Run the Spring Boot application using the Gradle wrapper:
```bash
./gradlew bootRun
```
*Wait for the application to start (usually on `http://localhost:8080`).*

## 3. Generate a "Certified" Certificate
Use a `curl` command to create a certificate. This process now automatically anchors the certificate to the **Local Immutable Blockchain** and generates a **Merkle Proof**.

```bash
curl -X POST http://localhost:8080/api/certificates \
     -H "Content-Type: application/json" \
     -d '{
           "purchaserName": "Admiral Grace Hopper",
           "bookTitle": "Help Your Boss Help You",
           "purchaserEmail": "grace@hopper.org"
         }' --output certificate.pdf
```

## 4. Experience "Deep Verification"
To see the new UI and the underlying "gag" logic:
1.  **Find the Verification URL**: Open the generated `certificate.pdf`. You'll see a QR code. Alternatively, you can visit the verification page directly. Since validation is "insecure-by-design," you can just visit:
    `http://localhost:8080/verify?name=Admiral%20Grace%20Hopper&book=Help%20Your%20Boss%20Help%20You&date=2026-03-14`
2.  **Click "Deep Verify"**: On the verification page, you'll see a new button labeled **"Perform Nuclear-Grade Deep Verification"**.
3.  **Watch the Console**: A real-time log will appear, showing the "Biometric Stylometry Analysis," "Blockchain Consensus Syncing," and "RSA-4096 Signature Decryption."
4.  **View the Proof**: The page will display the real **SHA-3 512-bit "Quantum-Resistant" Hash** and the **Merkle Root** from the local blockchain.

## 5. Check the Analytics Dashboard
Visit `http://localhost:8080/admin/dashboard` to see the "Enterprise-Grade" tracking of your certificates. Each entry now includes its blockchain status and technical metadata.

## 6. Run the New Tests
If you want to see the cryptographic and blockchain logic being verified in the background, run the newly added tests:
```bash
./gradlew test --tests "com.kousen.cert.service.*" --tests "com.kousen.cert.controller.VerificationControllerDeepVerifyTest"
```

---
**Note**: Because this is a gag, the "Biometric Analysis" actually analyzes the pixels of the author's scanned signature image to calculate a "confidence score," and the "Blockchain" is a perfectly immutable ledger that exists solely in your computer's RAM!
