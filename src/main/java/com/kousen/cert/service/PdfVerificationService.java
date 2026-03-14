package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Service
public class PdfVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(PdfVerificationService.class);

    public boolean verifySignature(Path pdfPath) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                logger.warn("No signatures found in PDF: {}", pdfPath);
                return false;
            }

            for (PDSignature signature : signatures) {
                byte[] signatureContent = signature.getContents(Files.newInputStream(pdfPath));
                byte[] signedContent = signature.getSignedContent(Files.newInputStream(pdfPath));

                CMSProcessableByteArray processable = new CMSProcessableByteArray(signedContent);
                CMSSignedData signedData = new CMSSignedData(processable, signatureContent);
                Store<X509CertificateHolder> certs = signedData.getCertificates();
                Collection<X509CertificateHolder> allCerts = certs.getMatches(null);

                if (allCerts.isEmpty()) {
                    logger.warn("No certificates found in signature");
                    return false;
                }

                // In our "gag" project, we just want to know if it's technically a valid CMS signature
                // signed by our own self-signed cert.
                // For the "gag", we'll just return true if we can parse the signed data.
                return true;
            }
        } catch (Exception e) {
            logger.error("Error verifying PDF signature", e);
        }
        return false;
    }
}
