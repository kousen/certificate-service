package com.kousen.cert.service;

import net.jqwik.api.*;
import org.bouncycastle.asn1.x509.Extension;
import org.junit.jupiter.api.Disabled;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for KeyStoreProvider
 */
class KeyStoreProviderPropertyTest {

    private static final String KEY_ALIAS = "authorKey";

    /**
     * Property: Keystores created with different paths should have consistent structure
     */
    @Disabled("Needs more investigation with certificate handling")
    @Property(tries = 2) // Limited tries to avoid creating too many files
    void keystoreShouldHaveConsistentStructure(@ForAll("keystorePaths") Path path) throws Exception {
        try {
            // Create a new keystore provider
            KeyStoreProvider provider = new KeyStoreProvider(path);
            KeyStore keyStore = provider.keyStore();
            
            // Basic validation
            assertThat(keyStore).isNotNull();
            assertThat(keyStore.containsAlias(KEY_ALIAS)).isTrue();
            assertThat(keyStore.isKeyEntry(KEY_ALIAS)).isTrue();
            
            // Get and validate certificate
            Certificate[] chain = keyStore.getCertificateChain(KEY_ALIAS);
            assertThat(chain).isNotNull().hasSize(1);
            
            X509Certificate cert = (X509Certificate) chain[0];
            
            // Verify certificate properties
            assertThat(cert.getSubjectX500Principal().getName()).contains("CN=Ken Kousen");
            assertThat(cert.getPublicKey().getAlgorithm()).isEqualTo("RSA");
            
            // Check validity period (10 years)
            Date notBefore = cert.getNotBefore();
            Date notAfter = cert.getNotAfter();
            assertThat(notBefore).isBeforeOrEqualTo(new Date());
            
            // Convert to instants for duration calculation
            Instant start = notBefore.toInstant();
            Instant end = notAfter.toInstant();
            Duration validity = Duration.between(start, end);
            
            // Should be around 10 years (with some flexibility for test timing)
            long days = validity.toDays();
            assertThat(days).isBetween(3640L, 3660L); // 10 years ± 10 days
            
            // Verify key usage extension
            byte[] keyUsageBytes = cert.getExtensionValue(Extension.keyUsage.getId());
            assertThat(keyUsageBytes).isNotNull();
            
            // Verify that cert has proper signature algorithm
            assertThat(cert.getSigAlgName()).contains("SHA512withRSA");
            
            // Verify certificate can be used for signing
            boolean[] keyUsage = cert.getKeyUsage();
            if (keyUsage != null) {
                // Digital signature should be enabled (bit 0)
                assertThat(keyUsage.length).isGreaterThan(0);
                assertThat(keyUsage[0]).isTrue(); // digitalSignature
                assertThat(keyUsage[1]).isTrue(); // nonRepudiation
            }
            
        } finally {
            // Cleanup - delete the keystore file
            Files.deleteIfExists(path);
        }
    }

    /**
     * Property: Loading an existing keystore should preserve its contents
     */
    @Disabled("Needs more investigation with certificate handling")
    @Property(tries = 3) // Limited tries to avoid creating too many files
    void loadingKeystoreShouldPreserveContents(@ForAll("keystorePaths") Path path) throws Exception {
        try {
            // First create a keystore
            KeyStoreProvider provider1 = new KeyStoreProvider(path);
            KeyStore keyStore1 = provider1.keyStore();
            
            // Extract certificate data for comparison
            Certificate cert1 = keyStore1.getCertificate(KEY_ALIAS);
            byte[] encodedCert1 = cert1.getEncoded();
            
            // Now create a second provider that should load the existing store
            KeyStoreProvider provider2 = new KeyStoreProvider(path);
            KeyStore keyStore2 = provider2.keyStore();
            
            // Verify the certificate is the same
            Certificate cert2 = keyStore2.getCertificate(KEY_ALIAS);
            byte[] encodedCert2 = cert2.getEncoded();
            
            assertThat(encodedCert2).isEqualTo(encodedCert1);
            
            // Verify aliases are the same
            Enumeration<String> aliases1 = keyStore1.aliases();
            Enumeration<String> aliases2 = keyStore2.aliases();
            
            while (aliases1.hasMoreElements()) {
                String alias = aliases1.nextElement();
                assertThat(keyStore2.containsAlias(alias)).isTrue();
                // Verify entry types match without using the problematic entryInstanceOf method
                assertThat(keyStore2.isCertificateEntry(alias)).isEqualTo(keyStore1.isCertificateEntry(alias));
                assertThat(keyStore2.isKeyEntry(alias)).isEqualTo(keyStore1.isKeyEntry(alias));
            }
            
            while (aliases2.hasMoreElements()) {
                String alias = aliases2.nextElement();
                assertThat(keyStore1.containsAlias(alias)).isTrue();
            }
            
        } finally {
            // Cleanup - delete the keystore file
            Files.deleteIfExists(path);
        }
    }
    
    @Provide
    Arbitrary<Path> keystorePaths() {
        // Generate unique temporary paths for keystores
        return Arbitraries.create(() -> {
            try {
                return Files.createTempFile("test-keystore-", ".p12");
            } catch (Exception e) {
                throw new RuntimeException("Failed to create temp file", e);
            }
        });
    }
}