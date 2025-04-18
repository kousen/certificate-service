package com.kousen.cert.service;

import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to register custom fonts with the PDF renderer
 */
public class CustomFontResolver {
    
    // Cache of temporary font files to avoid recreating them for each PDF
    private static final Map<String, Path> fontTempFiles = new HashMap<>();
    
    /**
     * Register custom fonts with the PDF renderer
     * 
     * @param renderer the PDF renderer
     * @throws IOException if font files cannot be loaded
     */
    public static void registerFonts(ITextRenderer renderer) throws IOException {
        ITextFontResolver fontResolver = renderer.getFontResolver();
        
        // Register "CinzelDecorative" font
        registerFont(fontResolver, "/fonts/CinzelDecorative-Regular.ttf", "CinzelDecorative", false, false);
        
        // Register "GreatVibes" font
        registerFont(fontResolver, "/fonts/GreatVibes-Regular.ttf", "GreatVibes", false, false);
        
        // Add cleanup hook for temporary files on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Path tempFile : fontTempFiles.values()) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    System.err.println("Failed to delete temp font file: " + tempFile);
                }
            }
        }));
    }
    
    /**
     * Register a single font with the renderer
     */
    private static void registerFont(ITextFontResolver fontResolver, 
                                    String resourcePath, 
                                    String fontFamily, 
                                    boolean italic, 
                                    boolean bold) throws IOException {
        // Check if we already have this font as a temp file
        if (fontTempFiles.containsKey(resourcePath)) {
            // Use cached temp file
            fontResolver.addFont(
                fontTempFiles.get(resourcePath).toString(),
                fontFamily,
                italic,
                bold,
                true  // embedded
            );
            return;
        }
        
        // Create temporary file for the font
        ClassPathResource fontResource = new ClassPathResource(resourcePath);
        Path tempFontFile = Files.createTempFile(fontFamily, ".ttf");
        
        // Copy font data to temporary file
        Files.copy(fontResource.getInputStream(), tempFontFile, StandardCopyOption.REPLACE_EXISTING);
        
        // Register the font
        fontResolver.addFont(
            tempFontFile.toString(),
            fontFamily,
            italic,
            bold,
            true  // embedded
        );
        
        // Cache the temporary file
        fontTempFiles.put(resourcePath, tempFontFile);
        
        System.out.println("Registered font: " + fontFamily + " (" + resourcePath + ")");
    }
}