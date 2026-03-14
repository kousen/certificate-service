### [Branch: `gag-features`] The "Self-Signed Gag" Technical Documentation

This project takes a fundamentally silly premise—digitally signing a self-signed PDF "certificate of ownership" for an ebook—and applies an absurd level of enterprise-grade over-engineering to it. 

The goal was to move beyond "vibe coding" (fake UI labels) and implement **technically real but contextually meaningless** cryptographic features.

### 🚀 Over-Engineered Features

#### 1. ⛓️ Local Immutable "Blockchain" (`BlockchainService.java`)
Instead of a simple database record, every certificate is now "anchored" to a local, in-memory blockchain.
- **Implementation**: A sequential chain of `Block` objects, each containing the hash of the previous block and a Merkle Root of its transactions.
- **Proof of Existence**: Each new certificate generation triggers the creation of a new block, ensuring a "verifiable history" of the ebook signatures.
- **Technical Irony**: It's an "immutable" ledger that resets every time the Spring Boot application restarts.

#### 2. 🧬 Biometric Stylometry Analysis (`PdfVerificationService.java`)
The "Deep Verification" process doesn't just check if the file exists; it performs an "analysis" of the author's signature.
- **Implementation**: The service uses PDFBox to extract image metadata (dimensions, color space, brightness) from the embedded signature image.
- **"Biometric" Scoring**: These raw metrics are passed through a scoring algorithm that produces a "Signature Confidence Score" (always between 98.4% and 99.9%).
- **Technical Irony**: It treats static image metadata as "dynamic biometric data" to sound more impressive.

#### 3. 🌳 Merkle Proof Integration (`CertificateMetadata.java`)
Every certificate includes a "Merkle Proof" as part of its metadata.
- **Implementation**: When a certificate is anchored to the blockchain, a SHA-256 hash is generated that includes the certificate ID and a "Global Entropy Seed."
- **Verification**: The `VerificationController` can verify this proof against the current state of the (temporary) blockchain.

#### 4. ⚛️ Quantum-Resistant Hashing (`PdfSigner.java` / `CertificateController.java`)
While the main signature uses standard RSA 4096-bit, we've added a layer of "Quantum Protection."
- **Implementation**: We calculate a **SHA-3 512-bit** hash of the certificate content.
- **Technical Irony**: Labeling a standard SHA-3 hash as "Quantum-Resistant" is technically accurate but functionally irrelevant for a gag project about signing PDFs.

#### 5. 🛠️ Deep Verification Engine (`VerificationController.java`)
The `/verify/{id}/deep` endpoint provides a full "Audit Log" of the verification process.
- **Process Flow**:
    1. **Blockchain Sync**: Verifies the certificate's existence in the local ledger.
    2. **Cryptographic Integrity**: Performs a real server-side signature check using BouncyCastle (`CMSSignedData`).
    3. **Biometric Scan**: Executes the stylometry analysis.
    4. **Merkle Validation**: Confirms the proof token.

### 🧪 Automated Verification
All features are backed by real JUnit 5 and MockMvc tests:
- `BlockchainServiceTest`: Ensures the chain remains valid and blocks are linked.
- `PdfVerificationServiceTest`: Validates the extraction of "biometric" metrics from PDF images.
- `VerificationControllerDeepVerifyTest`: Tests the end-to-end "Deep Verify" orchestration.

### 🎭 The Joke (Technically Explained)
The technical effort required to implement these features (handling PDF image extraction, building a Merkle-compatible block structure, and server-side signature parsing) is entirely disproportionate to the value of "verifying" a gag certificate. That disproportion is the core of the joke.
