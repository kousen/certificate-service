package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

public class PdfSigner implements SignatureInterface {

    private final KeyStoreProvider provider;
    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    public PdfSigner(KeyStoreProvider provider) {
        this.provider = provider;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        // Initialize keys and certificates
        initializeKeyMaterial();
    }

    private void initializeKeyMaterial() {
        try {
            KeyStore ks = provider.keyStore();

            // Check environment variable first, then system property, then default
            String password = System.getenv("CERT_PWD");
            if (password == null) {
                password = System.getProperty("CERT_PWD", "changeit");
            }
            char[] pwd = password.toCharArray();

            privateKey = (PrivateKey) ks.getKey("authorKey", pwd);
            certificateChain = ks.getCertificateChain("authorKey");
            if (privateKey == null || certificateChain == null) {
                throw new IllegalStateException("Key or certificate chain not found");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize key material: " + e.getMessage(), e);
        }
    }

    public Path sign(Path in) throws Exception {
        Path signed = Files.createTempFile("cert-signed-", ".pdf");

        try (var doc = Loader.loadPDF(in.toFile());
             var options = new SignatureOptions()) {
            // Create signature dictionary
            var sig = new PDSignature();
            sig.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            sig.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            sig.setName("Ken Kousen");
            sig.setLocation("Connecticut, USA");
            sig.setReason("Certificate of Ownership");
            sig.setSignDate(Calendar.getInstance());

            // Set signature size
            options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);

            // Add signature using the provided SignatureInterface implementation
            doc.addSignature(sig, this, options);

            // Save incrementally
            doc.saveIncremental(Files.newOutputStream(signed));
        }
        return signed;
    }

    // SignatureInterface implementation for creating the actual signature with proper CMS data
    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            // Read content to be signed
            byte[] buffer = content.readAllBytes();

            // Create a CMS Signed Data Generator
            var gen = new CMSSignedDataGenerator();

            // Get the signing certificate
            var signingCert = (X509Certificate) certificateChain[0];
            X509CertificateHolder certHolder = new JcaX509CertificateHolder(signingCert);

            // Add the certificate to the generator
            gen.addCertificate(certHolder);

            // Create a store of chain certificates
            var certStore = new JcaCertStore(Arrays.asList(certificateChain));
            gen.addCertificates(certStore);

            // Create the signature
            var contentSignerBuilder = new JcaContentSignerBuilder("SHA512withRSA");
            contentSignerBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);

            // Add the signer information
            var signerInfoBuilder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build());

            // Important: This makes it clear this is for non-repudiation (legal signatures)
            signerInfoBuilder.setDirectSignature(true);

            gen.addSignerInfoGenerator(signerInfoBuilder.build(
                    contentSignerBuilder.build(privateKey), certHolder));

            // Create the signed data
            var processableData = new CMSProcessableByteArray(buffer);
            CMSSignedData signedData = gen.generate(processableData, false);

            // Return encoded signature
            return signedData.getEncoded();

        } catch (Exception e) {
            throw new IOException("Failed to create signature", e);
        }
    }
}