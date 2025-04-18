package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CertificateStorageService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateStorageService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    
    private final Path storagePath;
    
    public CertificateStorageService(@Value("${certificate.storage.path}") String storagePath) {
        this.storagePath = Paths.get(storagePath);
        createStorageDirectoryIfNeeded();
    }
    
    /**
     * Stores a certificate PDF file with a unique name based on the request.
     * 
     * @param certificatePath Path to the temporary certificate file
     * @param request The certificate request containing purchaser name and book title
     * @return Path to the stored certificate file
     * @throws IOException if there's an error during file storage
     */
    public Path storeCertificate(Path certificatePath, CertificateRequest request) throws IOException {
        createStorageDirectoryIfNeeded();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String filename = generateFilename(request, timestamp);
        Path destinationPath = storagePath.resolve(filename);
        
        // Copy the certificate to the storage location
        Files.copy(certificatePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Certificate stored at: {}", destinationPath);
        
        return destinationPath;
    }
    
    /**
     * Generates a clean, URL-safe filename from the certificate request.
     * 
     * @param request The certificate request
     * @param timestamp A timestamp to ensure uniqueness
     * @return A sanitized filename
     */
    private String generateFilename(CertificateRequest request, String timestamp) {
        String sanitizedName = sanitize(request.purchaserName());
        String sanitizedBookTitle = abbreviateTitle(sanitize(request.bookTitle()));
        
        return String.format("%s_%s_%s.pdf", sanitizedName, sanitizedBookTitle, timestamp);
    }
    
    /**
     * Sanitizes a string to be used in a filename by:
     * 1. Converting to lowercase
     * 2. Normalizing (removing accents)
     * 3. Removing non-latin characters
     * 4. Replacing whitespace with underscores
     */
    private String sanitize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String latinOnly = NON_LATIN.matcher(noAccents).replaceAll("");
        String noWhitespace = WHITESPACE.matcher(latinOnly).replaceAll("_");
        return noWhitespace.toLowerCase(Locale.ENGLISH);
    }
    
    /**
     * Abbreviates a book title to a reasonable length for a filename by:
     * 1. Taking the first few words (up to a maximum length)
     * 2. If it's still too long, truncating it
     */
    private String abbreviateTitle(String title) {
        // If title is made of multiple words, keep first 3 words
        String[] words = title.split("_");
        if (words.length > 3) {
            return String.join("_", words[0], words[1], words[2]);
        }
        
        // If title is still very long, truncate
        return title.length() > 20 ? title.substring(0, 20) : title;
    }
    
    /**
     * Lists all stored certificates.
     * 
     * @return A list of certificate file paths
     * @throws IOException if there's an error reading the directory
     */
    public List<Path> listAllCertificates() throws IOException {
        createStorageDirectoryIfNeeded();
        
        try (var stream = Files.list(storagePath)) {
            return stream
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .sorted((p1, p2) -> {
                        try {
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Gets a stored certificate by filename.
     * 
     * @param filename The name of the certificate file
     * @return Path to the certificate if found
     * @throws IOException if the file can't be found
     */
    public Path getCertificate(String filename) throws IOException {
        Path certificatePath = storagePath.resolve(filename);
        if (!Files.exists(certificatePath)) {
            throw new IOException("Certificate not found: " + filename);
        }
        return certificatePath;
    }
    
    /**
     * Gets the storage directory path.
     * 
     * @return The storage directory path
     */
    public Path getStoragePath() {
        return storagePath;
    }
    
    private void createStorageDirectoryIfNeeded() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                logger.info("Created certificate storage directory: {}", storagePath);
            }
        } catch (IOException e) {
            logger.error("Failed to create certificate storage directory: {}", storagePath, e);
            throw new RuntimeException("Could not create certificate storage directory", e);
        }
    }
}