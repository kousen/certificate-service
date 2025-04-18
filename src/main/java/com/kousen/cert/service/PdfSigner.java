package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Calendar;

public class PdfSigner {

    private final KeyStoreProvider provider;

    public PdfSigner(KeyStoreProvider provider) {
        this.provider = provider;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public Path sign(Path in) throws Exception {
        Path signed = Files.createTempFile("cert-signed-", ".pdf");

        KeyStore ks = provider.keyStore();
        char[] pwd = System.getProperty("CERT_PWD", "changeit").toCharArray();
        PrivateKey pk = (PrivateKey) ks.getKey("authorKey", pwd);
        Certificate[] chain = ks.getCertificateChain("authorKey");
        if (pk == null || chain == null) {
            throw new IllegalStateException("Key or certificate chain not found");
        }

        try (PDDocument doc = Loader.loadPDF(in.toFile());
             SignatureOptions options = new SignatureOptions()) {
            // Create signature dictionary
            PDSignature sig = new PDSignature();
            sig.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            sig.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            sig.setName("Ken Kousen");
            sig.setSignDate(Calendar.getInstance());
            
            // Set certificate chain in the signature options
            options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            
            // Add the signature
            doc.addSignature(sig, (InputStream content) -> {
                try {
                    Signature signature = Signature.getInstance("SHA512withRSA");
                    signature.initSign(pk);

                    byte[] buffer = content.readAllBytes();
                    signature.update(buffer);
                    return signature.sign();
                } catch (GeneralSecurityException | IOException e) {
                    throw new IOException(e);
                }
            }, options);

            doc.saveIncremental(Files.newOutputStream(signed));
        }
        return signed;
    }
}