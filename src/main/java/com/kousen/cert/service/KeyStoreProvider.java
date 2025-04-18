package com.kousen.cert.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.nio.file.*;
import java.security.*;
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
        this.pwd = System.getProperty("CERT_PWD", "changeit").toCharArray();
        this.keyStore = Files.exists(storePath) ? load() : create();
    }

    public KeyStore keyStore() { return keyStore; }

    private KeyStore create() {
        try {
            var kpGen = KeyPairGenerator.getInstance("RSA");
            kpGen.initialize(4096);
            KeyPair kp = kpGen.generateKeyPair();

            var subject = new X500Name("CN=Ken Kousen, O=Self, L=CT, C=US");
            var serial = BigInteger.valueOf(System.currentTimeMillis());
            var notBefore = new Date();
            var notAfter  = Date.from(Instant.now().plus(3650, ChronoUnit.DAYS));

            ContentSigner signer = new JcaContentSignerBuilder("SHA512withRSA").build(kp.getPrivate());
            var builder =
                    new JcaX509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, kp.getPublic());

            X509Certificate cert = new JcaX509CertificateConverter()
                                          .getCertificate(builder.build(signer));

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(KEY_ALIAS, kp.getPrivate(), pwd, new Certificate[]{cert});
            try (var os = Files.newOutputStream(storePath)) {
                ks.store(os, pwd);
            }
            return ks;
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
            throw new IllegalStateException(e);
        }
    }
}
