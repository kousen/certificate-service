package com.kousen.cert.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainServiceTest {

    private BlockchainService blockchainService;

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService();
    }

    @Test
    @DisplayName("should anchor certificate and return a valid hash")
    void shouldAnchorCertificateAndReturnValidHash() {
        String certificateId = "cert-123";
        String fileHash = "file-hash-abc";

        String blockHash = blockchainService.anchorCertificate(certificateId, fileHash);

        assertNotNull(blockHash);
        assertNotEquals("HASH_ERROR", blockHash);
        assertEquals(64, blockHash.length(), "SHA-256 hash should be 64 characters long");
    }

    @Test
    @DisplayName("should retrieve the same Merkle Proof for a certificate")
    void shouldRetrieveSameMerkleProofForCertificate() {
        String certificateId = "cert-456";
        String fileHash = "file-hash-def";

        String initialHash = blockchainService.anchorCertificate(certificateId, fileHash);
        String retrievedHash = blockchainService.getMerkleProof(certificateId);

        assertEquals(initialHash, retrievedHash);
    }

    @Test
    @DisplayName("should return NO_PROOF_FOUND for unknown certificate")
    void shouldReturnNoProofFoundForUnknownCertificate() {
        String proof = blockchainService.getMerkleProof("non-existent");
        assertEquals("NO_PROOF_FOUND", proof);
    }

    @Test
    @DisplayName("should return active network status")
    void shouldReturnActiveNetworkStatus() {
        assertEquals("LOCAL_DEMO_LEDGER_ACTIVE", blockchainService.getNetworkStatus());
    }

    @Test
    @DisplayName("should maintain chain integrity with unique hashes")
    void shouldMaintainChainIntegrityWithUniqueHashes() {
        String hash1 = blockchainService.anchorCertificate("cert-1", "hash-1");
        String hash2 = blockchainService.anchorCertificate("cert-2", "hash-2");

        assertNotEquals(hash1, hash2, "Each block should have a unique hash");
    }
}
