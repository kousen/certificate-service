package com.kousen.cert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A "technically real" but completely unnecessary Local Immutable Ledger.
 * It provides "Proof of Existence" for certificates.
 */
@Service
public class BlockchainService {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    
    // A simple in-memory "chain" for the gag
    private final List<Block> chain = new ArrayList<>();
    private final ConcurrentHashMap<String, String> certificateToBlockHash = new ConcurrentHashMap<>();

    public BlockchainService() {
        // Create Genesis Block
        chain.add(new Block("0", "GENESIS", "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    public synchronized String anchorCertificate(String certificateId, String fileHash) {
        String prevHash = chain.get(chain.size() - 1).hash;
        Block newBlock = new Block(prevHash, certificateId + ":" + fileHash, calculateHash(prevHash, certificateId, fileHash));
        chain.add(newBlock);
        certificateToBlockHash.put(certificateId, newBlock.hash);
        
        logger.info("Anchored certificate {} to block {}", certificateId, newBlock.hash);
        return newBlock.hash;
    }

    public String getMerkleProof(String certificateId) {
        return certificateToBlockHash.getOrDefault(certificateId, "NO_PROOF_FOUND");
    }

    public String getNetworkStatus() {
        return "LOCAL_DEMO_LEDGER_ACTIVE";
    }

    private String calculateHash(String prevHash, String certId, String fileHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = prevHash + certId + fileHash + Instant.now().toEpochMilli();
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
    }

    private record Block(String prevHash, String data, String hash) {}
}
