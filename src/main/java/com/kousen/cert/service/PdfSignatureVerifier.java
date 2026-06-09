package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the CMS (PKCS#7) digital signature embedded in a PDF and reports
 * whether the document is intact and whether it was signed by this service's
 * own (self-signed) certificate.
 */
public class PdfSignatureVerifier {

    private static final Logger logger = LoggerFactory.getLogger(PdfSignatureVerifier.class);

    private final KeyStoreProvider keyStoreProvider;

    public PdfSignatureVerifier(KeyStoreProvider keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Result of verifying a PDF signature.
     *
     * @param signaturePresent     whether the PDF contains a digital signature
     * @param documentIntact       whether the signed content verifies against the signature
     * @param signedByThisService  whether the signer certificate matches this service's certificate
     * @param coversEntireDocument whether the signature's byte range covers the whole file
     * @param signerName           subject DN of the signer certificate, if available
     * @param signedAt             signing time recorded in the signature dictionary, if available
     * @param message              human-readable summary of the outcome
     */
    public record VerificationResult(
            boolean signaturePresent,
            boolean documentIntact,
            boolean signedByThisService,
            boolean coversEntireDocument,
            String signerName,
            String signedAt,
            String message) {

        static VerificationResult unsigned(String message) {
            return new VerificationResult(false, false, false, false, null, null, message);
        }
    }

    public VerificationResult verify(byte[] pdfBytes) {
        try (var doc = Loader.loadPDF(pdfBytes)) {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                return VerificationResult.unsigned("The PDF does not contain a digital signature.");
            }

            // Verify the most recent signature (incremental saves append signatures)
            PDSignature signature = signatures.getLast();
            byte[] cmsBytes = signature.getContents(pdfBytes);
            byte[] signedContent = signature.getSignedContent(pdfBytes);

            CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(signedContent), cmsBytes);
            SignerInformation signer = cms.getSignerInfos().getSigners().iterator().next();
            Collection<X509CertificateHolder> matches = cms.getCertificates().getMatches(signer.getSID());
            if (matches.isEmpty()) {
                return new VerificationResult(true, false, false, false, null, null,
                        "Signature found but the signer certificate is missing from the CMS data.");
            }
            X509CertificateHolder certHolder = matches.iterator().next();

            boolean intact;
            try {
                intact = signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(certHolder));
            } catch (Exception e) {
                // BC throws (rather than returning false) for some mismatches
                intact = false;
            }
            boolean ours = Arrays.equals(certHolder.getEncoded(),
                    keyStoreProvider.certificate().getEncoded());
            boolean coversAll = coversEntireDocument(signature, pdfBytes.length);

            String signerName = certHolder.getSubject().toString();
            String signedAt = signature.getSignDate() != null
                    ? DateTimeFormatter.ISO_INSTANT.format(signature.getSignDate().toInstant().atOffset(ZoneOffset.UTC))
                    : null;

            String message;
            if (!intact) {
                message = "The document has been modified since it was signed, or the signature is invalid.";
            } else if (!ours) {
                message = "The signature is cryptographically valid but was not produced by this service's certificate.";
            } else if (!coversAll) {
                message = "The signature is valid but content was appended to the document after signing.";
            } else {
                message = "The signature is valid and the document is exactly as signed by this service. "
                        + "(Remember: the signing certificate is self-signed, so trust it accordingly.)";
            }
            return new VerificationResult(true, intact, ours, coversAll, signerName, signedAt, message);
        } catch (Exception e) {
            logger.warn("Failed to verify PDF signature: {}", e.getMessage());
            return VerificationResult.unsigned(
                    "Could not verify the file. Is it a valid PDF? (" + e.getMessage() + ")");
        }
    }

    /**
     * A PDF signature's byte range must start at offset 0 and, together with the
     * signature contents gap, span the entire file; otherwise data was appended
     * after signing.
     */
    private boolean coversEntireDocument(PDSignature signature, int fileLength) {
        int[] byteRange = signature.getByteRange();
        if (byteRange == null || byteRange.length != 4) {
            return false;
        }
        return byteRange[0] == 0 && byteRange[2] + byteRange[3] == fileLength;
    }
}
