package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.util.encoders.Hex;

@Service
@Transactional
public class CertificateMetadataService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateMetadataService.class);
    
    private final CertificateMetadataRepository repository;
    
    public CertificateMetadataService(CertificateMetadataRepository repository) {
        this.repository = repository;
    }
    
    @Async("analyticsTaskExecutor")
    public CompletableFuture<Void> saveCertificateMetadata(String certificateId, Path certificatePath) {
        try {
            CertificateMetadata metadata = new CertificateMetadata(
                certificateId,
                certificatePath.getFileName().toString()
            );

            if (Files.exists(certificatePath)) {
                metadata.setFileSize(Files.size(certificatePath));

                try {
                    byte[] fileBytes = Files.readAllBytes(certificatePath);
                    
                    // Standard SHA-256
                    MessageDigest md256 = MessageDigest.getInstance("SHA-256");
                    byte[] hashBytes256 = md256.digest(fileBytes);
                    metadata.setFileHash(Hex.toHexString(hashBytes256));
                    
                    // Quantum-Resistant SHA-3 512-bit
                    try {
                        MessageDigest md512 = MessageDigest.getInstance("SHA3-512");
                        byte[] hashBytes512 = md512.digest(fileBytes);
                        metadata.setQuantumHash(Hex.toHexString(hashBytes512));
                    } catch (NoSuchAlgorithmException e) {
                        logger.warn("SHA3-512 not available, falling back to SHA-512");
                        MessageDigest md512 = MessageDigest.getInstance("SHA-512");
                        byte[] hashBytes512 = md512.digest(fileBytes);
                        metadata.setQuantumHash(Hex.toHexString(hashBytes512));
                    }
                } catch (Exception e) {
                    logger.warn("Could not calculate file hashes for {}", certificatePath, e);
                }
            }
            
            repository.save(metadata);
            logger.info("Saved certificate metadata for {}", certificateId);
        } catch (Exception e) {
            logger.error("Error saving certificate metadata", e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    public CertificateMetadata getCertificateMetadata(String certificateId) {
        return repository.findById(certificateId).orElse(null);
    }
    
    public CertificateMetadata getCertificateMetadataByFilename(String filename) {
        return repository.findByFilename(filename).orElse(null);
    }
}
