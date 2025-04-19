package com.kousen.cert.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class KeyStoreProvider {

    private static final String KEY_ALIAS = "authorKey";
    private final Path storePath;
    private final char[] pwd;
    private final KeyStore keyStore;

    public KeyStoreProvider(Path storePath) {
        this.storePath = storePath;
        // Check environment variable first, then system property, then default
        String password = System.getenv("CERT_PWD");
        if (password == null) {
            password = System.getProperty("CERT_PWD", "changeit");
        }
        this.pwd = password.toCharArray();
        
        // Create parent directories if they don't exist
        try {
            Path parent = storePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            // Log but continue, as the actual file operation will fail with a more specific error
            System.err.println("Failed to create parent directories: " + e.getMessage());
        }
        
        this.keyStore = Files.exists(storePath) ? load() : create();
    }

    public KeyStore keyStore() { return keyStore; }

    private KeyStore create() {
        try {
            // Generate a strong RSA key pair
            var kpGen = KeyPairGenerator.getInstance("RSA");
            kpGen.initialize(4096);
            KeyPair kp = kpGen.generateKeyPair();

            // Create certificate subject with more details
            var subject = new X500Name(
                    "CN=Ken Kousen, O=Tales from the Jar Side, OU=PDF Signing, L=Connecticut, ST=CT, C=US");
            var serial = BigInteger.valueOf(System.currentTimeMillis());
            var notBefore = new Date();
            var notAfter  = Date.from(Instant.now().plus(3650, ChronoUnit.DAYS)); // 10 years

            // Create a certificate builder with enhanced properties
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject,         // Issuer is same as Subject for self-signed
                    serial,
                    notBefore,
                    notAfter,
                    subject,         // Subject
                    kp.getPublic()
            );

            // Add Extensions to make it more compatible with Adobe
            
            // Basic constraints - not a CA
            certBuilder.addExtension(
                    Extension.basicConstraints,
                    true,
                    new BasicConstraints(false));
            
            // Key usage - digital signature, non-repudiation
            certBuilder.addExtension(
                    Extension.keyUsage,
                    true,
                    new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation));
            
            // Extended key usage - PDF signing
            certBuilder.addExtension(
                    Extension.extendedKeyUsage,
                    true,
                    new ExtendedKeyUsage(new KeyPurposeId[] {
                            KeyPurposeId.id_kp_emailProtection,
                            KeyPurposeId.id_kp_codeSigning
                            // Adobe's PDF signing OID is 1.2.840.113583.1.1.5,
                            // but we can't use it directly due to access restrictions
                    }));

            // Sign the certificate
            ContentSigner signer = new JcaContentSignerBuilder("SHA512withRSA").build(kp.getPrivate());
            X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

            // Create and save the keystore
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(KEY_ALIAS, kp.getPrivate(), pwd, new Certificate[]{cert});
            try (var os = Files.newOutputStream(storePath)) {
                ks.store(os, pwd);
            }
            return ks;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create certificate: " + e.getMessage(), e);
        }
    }

    private KeyStore load() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (var is = Files.newInputStream(storePath)) {
                ks.load(is, pwd);
            }
            return ks;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load keystore: " + e.getMessage(), e);
        }
    }
}