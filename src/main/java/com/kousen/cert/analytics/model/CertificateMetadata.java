package com.kousen.cert.analytics.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "certificate_metadata")
public class CertificateMetadata {
    
    @Id
    private String certificateId;
    
    @Column(nullable = false)
    private String filename;
    
    private String fileHash;
    
    private Long fileSize;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    private Integer verificationCount = 0;
    
    private Instant lastVerifiedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    
    public CertificateMetadata() {}
    
    public CertificateMetadata(String certificateId, String filename) {
        this.certificateId = certificateId;
        this.filename = filename;
        this.createdAt = Instant.now();
        this.verificationCount = 0;
    }
    
    public void incrementVerificationCount() {
        this.verificationCount++;
        this.lastVerifiedAt = Instant.now();
    }
    
    // Getters and setters
    public String getCertificateId() {
        return certificateId;
    }
    
    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getFileHash() {
        return fileHash;
    }
    
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getVerificationCount() {
        return verificationCount;
    }
    
    public void setVerificationCount(Integer verificationCount) {
        this.verificationCount = verificationCount;
    }
    
    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }
    
    public void setLastVerifiedAt(Instant lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }
}