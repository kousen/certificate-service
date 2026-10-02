package com.kousen.cert.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PdfSignatureVerifierTest {

    @TempDir
    static Path tempDir;

    private static KeyStoreProvider provider;
    private static PdfSignatureVerifier verifier;
    private static byte[] signedPdfBytes;

    @BeforeAll
    static void signTestPdf() throws Exception {
        provider = new KeyStoreProvider(tempDir.resolve("verifier-test-keystore.p12"));
        verifier = new PdfSignatureVerifier(provider);

        Path unsigned = tempDir.resolve("unsigned.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(unsigned.toFile());
        }

        Path signed = new PdfSigner(provider).sign(unsigned);
        signedPdfBytes = Files.readAllBytes(signed);
        Files.deleteIfExists(signed);
    }

    @Test
    void shouldVerifyDocumentSignedByThisService() {
        var result = verifier.verify(signedPdfBytes);

        assertThat(result.signaturePresent()).isTrue();
        assertThat(result.documentIntact()).isTrue();
        assertThat(result.signedByThisService()).isTrue();
        assertThat(result.coversEntireDocument()).isTrue();
        assertThat(result.signerName()).contains("Ken Kousen");
    }

    @Test
    void shouldDetectTamperedDocument() {
        // Tamper with a byte inside the signed range without breaking PDF
        // structure: change the signature dictionary's location string
        byte[] tampered = signedPdfBytes.clone();
        int index = indexOf(tampered, "Connecticut".getBytes());
        assertThat(index).isGreaterThan(0);
        tampered[index] = 'X';

        var result = verifier.verify(tampered);

        assertThat(result.signaturePresent()).isTrue();
        assertThat(result.documentIntact()).isFalse();
    }

    @Test
    void shouldDetectContentAppendedAfterSigning() {
        byte[] extended = Arrays.copyOf(signedPdfBytes, signedPdfBytes.length + 16);

        var result = verifier.verify(extended);

        assertThat(result.signaturePresent()).isTrue();
        assertThat(result.coversEntireDocument()).isFalse();
    }

    @Test
    void shouldReportMissingSignatureForUnsignedPdf() throws Exception {
        byte[] unsignedBytes;
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            var baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            unsignedBytes = baos.toByteArray();
        }

        var result = verifier.verify(unsignedBytes);

        assertThat(result.signaturePresent()).isFalse();
        assertThat(result.documentIntact()).isFalse();
    }

    @Test
    void shouldHandleNonPdfInputGracefully() {
        var result = verifier.verify("not a pdf at all".getBytes());

        assertThat(result.signaturePresent()).isFalse();
        assertThat(result.message()).contains("Could not verify");
    }

    @Test
    void shouldRejectDocumentSignedByDifferentKey() throws Exception {
        // Sign with a different self-signed certificate
        KeyStoreProvider otherProvider = new KeyStoreProvider(tempDir.resolve("other-keystore.p12"));
        Path unsigned = tempDir.resolve("other-unsigned.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(unsigned.toFile());
        }
        Path signed = new PdfSigner(otherProvider).sign(unsigned);
        byte[] otherSigned = Files.readAllBytes(signed);
        Files.deleteIfExists(signed);

        var result = verifier.verify(otherSigned);

        assertThat(result.signaturePresent()).isTrue();
        assertThat(result.documentIntact()).isTrue();
        assertThat(result.signedByThisService()).isFalse();
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
