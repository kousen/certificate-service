package com.kousen.cert.analytics.repository;

import com.kousen.cert.analytics.model.CertificateMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateMetadataRepository extends JpaRepository<CertificateMetadata, String> {
    
    Optional<CertificateMetadata> findByFilename(String filename);
    
    @Query("SELECT COUNT(c) FROM CertificateMetadata c")
    long countTotalCertificates();
    
    @Query("SELECT SUM(c.verificationCount) FROM CertificateMetadata c")
    Long sumTotalVerifications();
    
    List<CertificateMetadata> findTop10ByOrderByCreatedAtDesc();
    
    @Query("SELECT c FROM CertificateMetadata c WHERE c.verificationCount > 0 ORDER BY c.lastVerifiedAt DESC")
    List<CertificateMetadata> findRecentlyVerified();
}