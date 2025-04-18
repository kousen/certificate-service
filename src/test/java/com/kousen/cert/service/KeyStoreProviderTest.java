package com.kousen.cert.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

class KeyStoreProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateKeyStoreWhenFileDoesNotExist() throws Exception {
        // Given
        Path keystorePath = tempDir.resolve("new_keystore.p12");
        assertThat(Files.exists(keystorePath)).isFalse();
        
        // When
        KeyStoreProvider provider = new KeyStoreProvider(keystorePath);
        KeyStore keyStore = provider.keyStore();
        
        // Then
        assertThat(Files.exists(keystorePath)).isTrue();
        assertThat(keyStore).isNotNull();
        assertThat(keyStore.containsAlias("authorKey")).isTrue();
        
        // Verify we can access the private key
        char[] pwd = System.getProperty("CERT_PWD", "changeit").toCharArray();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("authorKey", pwd);
        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
        
        // Verify certificate chain exists
        Certificate[] chain = keyStore.getCertificateChain("authorKey");
        assertThat(chain).isNotNull().isNotEmpty();
        
        // Verify certificate properties
        X509Certificate cert = (X509Certificate) chain[0];
        assertThat(cert.getSubjectX500Principal().getName()).contains("CN=Ken Kousen");
        assertThat(cert.getNotAfter()).isAfter(cert.getNotBefore());
    }
    
    @Test
    void shouldLoadExistingKeyStore() throws Exception {
        // Given - Create a keystore first
        Path keystorePath = tempDir.resolve("existing_keystore.p12");
        KeyStoreProvider firstProvider = new KeyStoreProvider(keystorePath);
        assertThat(firstProvider.keyStore()).isNotNull();
        assertThat(Files.exists(keystorePath)).isTrue();
        
        // When - Create a second provider using the same path
        KeyStoreProvider secondProvider = new KeyStoreProvider(keystorePath);
        KeyStore keyStore = secondProvider.keyStore();
        
        // Then - It should load the existing keystore
        assertThat(keyStore).isNotNull();
        assertThat(keyStore.containsAlias("authorKey")).isTrue();
        
        // Verify access to the certificate
        Certificate cert = keyStore.getCertificate("authorKey");
        assertThat(cert).isNotNull();
        assertThat(cert).isInstanceOf(X509Certificate.class);
    }
}