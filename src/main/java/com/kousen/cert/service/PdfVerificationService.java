package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Performs a "Biometric Stylometric Analysis" on the embedded signature image.
     * This is a "gag" feature that actually extracts real image metrics but labels them as biometric data.
     */
    public Map<String, Object> performBiometricAnalysis(Path pdfPath) {
        Map<String, Object> analysis = new HashMap<>();
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDPage page = document.getPage(0);
            PDResources resources = page.getResources();
            
            for (org.apache.pdfbox.cos.COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);
                if (xObject instanceof PDImageXObject image) {
                    BufferedImage bufferedImage = image.getImage();
                    
                    // Extract real metrics but give them "gag" names
                    long pixelCount = (long) bufferedImage.getWidth() * bufferedImage.getHeight();
                    double avgBrightness = calculateAverageBrightness(bufferedImage);
                    String pixelDistribution = String.format("STYL-%04x-%04x", 
                            bufferedImage.getWidth(), bufferedImage.getHeight());
                    
                    analysis.put("stylometricConfidence", 0.98 + (Math.random() * 0.019)); // Always > 98%
                    analysis.put("pressurePointAnalysis", "AUTHENTIC");
                    analysis.put("inkDistributionCoherent", true);
                    analysis.put("pixelEntropy", avgBrightness);
                    analysis.put("biometricId", pixelDistribution);
                    analysis.put("status", "VALIDATED");
                    
                    return analysis;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not perform biometric analysis: {}", e.getMessage());
        }
        
        analysis.put("status", "IMAGE_NOT_FOUND");
        analysis.put("stylometricConfidence", 0.0);
        return analysis;
    }

    private double calculateAverageBrightness(BufferedImage img) {
        long sum = 0;
        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                sum += (r + g + b) / 3;
            }
        }
        return (double) sum / (width * height);
    }
}
