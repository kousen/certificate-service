package com.kousen.cert.service;

import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to register custom fonts with the PDF renderer
 * Uses a simplified approach to ensure fonts work in all deployment scenarios
 */
public class CustomFontResolver {

    // Cache of temporary font files to avoid recreating them for each PDF
    private static final Map<String, Path> fontTempFiles = new HashMap<>();
    
    /**
     * Register custom fonts with the PDF renderer
     * This is the main entry point used by PdfService
     * 
     * @param renderer the PDF renderer
     * @throws IOException if font files cannot be loaded
     */
    public static void registerFonts(ITextRenderer renderer) throws IOException {
        ITextFontResolver fontResolver = renderer.getFontResolver();
        
        // Load and register custom fonts
        registerFont(fontResolver, "/fonts/CinzelDecorative-Regular.ttf");
        registerFont(fontResolver, "/fonts/GreatVibes-Regular.ttf");
        
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
     * Register a font with the renderer
     * Maps font file to exact font-family name used in CSS
     */
    private static void registerFont(ITextFontResolver fontResolver, String resourcePath) throws IOException {
        Path fontFile = extractFontToTempFile(resourcePath);
        System.out.println("Loading font from: " + fontFile);
        
        // Extract font name from the resource path
        String fontName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1, resourcePath.lastIndexOf('.'));
        
        try {
            // Register the font with the exact same name used in the CSS font-family declaration
            // Align with the @font-face declaration in the HTML
            fontResolver.addFont(fontFile.toString(), fontName, true);
            System.out.println("Successfully registered font '" + fontName + "' from: " + fontFile);
        } catch (Exception e) {
            System.err.println("Failed to register font with font family: " + e.getMessage());
            
            try {
                // Fallback to basic registration
                fontResolver.addFont(fontFile.toString(), true);
                System.out.println("Fallback: Registered font (basic): " + fontFile);
            } catch (Exception e2) {
                System.err.println("All font registration methods failed: " + fontFile);
                e2.printStackTrace();
            }
        }
    }
    
    /**
     * Extract a font file from classpath resources to a temporary file on disk
     */
    private static Path extractFontToTempFile(String resourcePath) throws IOException {
        // Check if we already have this font as a temp file
        if (fontTempFiles.containsKey(resourcePath)) {
            return fontTempFiles.get(resourcePath);
        }
        
        // Get font file name for creating temp file
        String fontFileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        
        // Create temporary file
        Path tempFile = Files.createTempFile("font-", "-" + fontFileName);
        
        // Copy font data from classpath resource to temp file
        try (InputStream in = new ClassPathResource(resourcePath).getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        // Cache the temp file
        fontTempFiles.put(resourcePath, tempFile);
        
        return tempFile;
    }
}
