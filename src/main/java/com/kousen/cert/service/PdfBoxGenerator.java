package com.kousen.cert.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * PDF generation utility using Apache PDFBox
 */
public class PdfBoxGenerator {

    // Font and color constants
    private static final Color GOLD_COLOR = new Color(255, 214, 92);
    
    // Font cache removed - PDType0Font must be loaded per document in PDFBox 3.0.4
    
    /**
     * Creates a certificate PDF for the specified text content
     * 
     * @param title The main title text
     * @param name The recipient's name
     * @param subtitle The subtitle or book title
     * @param qrCodePath Path to the QR code image
     * @return Path to the generated PDF file
     * @throws IOException If there's an error during PDF creation
     */
    public Path createCertificatePdf(String title, String name, String subtitle, Path qrCodePath) throws IOException {
        // Create temp file for PDF output
        Path pdfPath = Files.createTempFile("cert-", ".pdf");
        
        // Landscape A4 dimensions in points (1/72 inch)
        float pageWidth = PDRectangle.A4.getHeight();
        float pageHeight = PDRectangle.A4.getWidth();

        try (var document = new PDDocument()) {
            // Create a landscape page
            PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            document.addPage(page);
            
            // Load fonts
            // Use Standard14Fonts for fallbacks (PDFBox 3.0 way of loading standard fonts)
            PDFont titleFont = getFont(document, "CinzelDecorative-Regular.ttf", 
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
            PDFont nameFont = getFont(document, "GreatVibes-Regular.ttf", 
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE));
            PDFont textFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            
            // Add background image
            addBackgroundImage(document, page);
            
            // Start adding content
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                // Set text color for all content
                contentStream.setNonStrokingColor(GOLD_COLOR);
                
                // Title
                float titleFontSize = 48;
                float centerX = pageWidth / 2;
                float y = pageHeight - 200;
                
                // Draw title
                drawCenteredText(contentStream, titleFont, titleFontSize, title, centerX, y);
                
                // "This certifies that" text
                y -= 50;
                drawCenteredText(contentStream, textFont, 14, "This certifies that", centerX, y);
                
                // Recipient name
                y -= 60;
                float nameFontSize = 40;
                drawCenteredText(contentStream, nameFont, nameFontSize, name, centerX, y);
                
                // "is the proud owner of" text
                y -= 50;
                drawCenteredText(contentStream, textFont, 14, "is the proud owner of", centerX, y);
                
                // Book title
                y -= 40;
                float subtitleFontSize = 24;
                drawCenteredText(contentStream, titleFont, subtitleFontSize, subtitle, centerX, y);
                
                // "and has earned the author's eternal gratitude" text
                y -= 50;
                drawCenteredText(contentStream, textFont, 14,
                        "and has earned the author's eternal gratitude.", centerX, y);
                
                // Add QR code
                if (qrCodePath != null && Files.exists(qrCodePath)) {
                    addQRCode(document, contentStream, qrCodePath, 80, 80);
                    
                    // Add "Scan to verify" text below QR code
                    float qrTextX = 80;
                    float qrTextY = 60;
                    contentStream.beginText();
                    contentStream.setFont(textFont, 8);
                    contentStream.newLineAtOffset(qrTextX - 20, qrTextY);
                    contentStream.showText("Scan to verify certificate authenticity");
                    contentStream.endText();
                }
            }
            
            // Save the PDF to the temp file
            try {
                // In PDFBox 3.0.4, we need to be more careful with font embedding
                // Explicitly disable subsetting which is causing issues on Heroku
                document.getDocumentCatalog().getAcroForm();  // Initialize AcroForm if present
                
                // Disable all security features which might interfere with font handling
                document.setAllSecurityToBeRemoved(true);
                
                // Set properties to prevent font subsetting
                System.setProperty("org.apache.pdfbox.font.subset", "false");
                document.getDocumentInformation().setCustomMetadataValue("DisableFontSubsetting", "true");
                
                // Save with modified settings
                document.save(pdfPath.toFile());
                System.out.println("PDF created at: " + pdfPath.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error saving PDF: " + e.getMessage());
                
                // Try alternative approach with standard fonts only
                try {
                    System.err.println("Attempting to save with standard fonts only");
                    // Create a new document with only standard fonts
                    try (PDDocument simpleDoc = new PDDocument()) {
                        PDPage simplePage = new PDPage(new PDRectangle(pageWidth, pageHeight));
                        simpleDoc.addPage(simplePage);
                        
                        try (PDPageContentStream contentStream = new PDPageContentStream(
                                simpleDoc, simplePage, PDPageContentStream.AppendMode.APPEND, true)) {
                            
                            // Use only standard fonts
                            PDFont stdFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                            PDFont stdBoldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                            PDFont stdItalicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
                            
                            contentStream.setNonStrokingColor(GOLD_COLOR);
                            
                            // Basic certificate with standard fonts
                            float y = pageHeight - 200;
                            drawCenteredText(contentStream, stdBoldFont, 36, title, pageWidth/2, y);
                            
                            y -= 50;
                            drawCenteredText(contentStream, stdFont, 14, "This certifies that", pageWidth/2, y);
                            
                            y -= 60;
                            drawCenteredText(contentStream, stdItalicFont, 32, name, pageWidth/2, y);
                            
                            y -= 50;
                            drawCenteredText(contentStream, stdFont, 14, "is the proud owner of", pageWidth/2, y);
                            
                            y -= 40;
                            drawCenteredText(contentStream, stdBoldFont, 22, subtitle, pageWidth/2, y);
                            
                            y -= 50;
                            drawCenteredText(contentStream, stdFont, 14,
                                    "and has earned the author's eternal gratitude.", pageWidth/2, y);
                            
                            // Add QR code if available
                            if (qrCodePath != null && Files.exists(qrCodePath)) {
                                addQRCode(simpleDoc, contentStream, qrCodePath, 80, 80);
                                drawCenteredText(contentStream, stdFont, 8, 
                                        "Scan to verify", 80, 60);
                            }
                        }
                        
                        simpleDoc.save(pdfPath.toFile());
                    }
                    System.out.println("Successfully created simplified PDF with standard fonts only");
                } catch (Exception e2) {
                    System.err.println("Error saving even with simplified approach: " + e2.getMessage());
                    throw new IOException("Failed to save PDF document: " + e2.getMessage(), e2);
                }
            }
        }
        
        return pdfPath;
    }
    
    /**
     * Gets a font, loading from classpath resources
     */
    private PDFont getFont(PDDocument document, String fontFileName, PDFont fallbackFont) {
        // Note: In PDFBox 3.0.4, each font must be loaded directly into the document
        // Do not use the cache for PDType0Font as it causes issues on Heroku
        try {
            // Load the font from classpath resources
            var fontResource = new ClassPathResource("fonts/" + fontFileName);
            if (!fontResource.exists()) {
                System.err.println("Font file not found: " + fontFileName);
                return fallbackFont;
            }
            
            try (var fontStream = fontResource.getInputStream()) {
                if (fontStream == null) {
                    System.err.println("Could not open font stream for: " + fontFileName);
                    return fallbackFont;
                }
                
                // Set to false to disable subsetting which is causing issues on Heroku
                PDFont font = PDType0Font.load(document, fontStream, false);
                System.out.println("Successfully loaded font: " + fontFileName);
                return font;
            }
        } catch (IOException e) {
            System.err.println("Error loading font " + fontFileName + ": " + e.getMessage());
            System.err.println("Using fallback font: " + fallbackFont.getName());
            return fallbackFont;
        }
    }
    
    /**
     * Add background image to the PDF page
     */
    private void addBackgroundImage(PDDocument document, PDPage page) {
        try {
            var imageResource = new ClassPathResource("images/certificate-bg.png");
            try (var imageStream = imageResource.getInputStream()) {
                PDImageXObject backgroundImage = PDImageXObject.createFromByteArray(document, 
                        imageStream.readAllBytes(), "background");
                
                // Create a content stream to draw the background
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.PREPEND, true)) {
                    // Draw the image at full page size
                    contentStream.drawImage(backgroundImage, 0, 0, page.getMediaBox().getWidth(), 
                            page.getMediaBox().getHeight());
                }
            }
        } catch (IOException e) {
            System.err.println("Error adding background image: " + e.getMessage());
            // Continue without background if image can't be loaded
        }
    }
    
    /**
     * Add QR code to the page
     */
    @SuppressWarnings("SameParameterValue")
    private void addQRCode(PDDocument document, PDPageContentStream contentStream,
                           Path qrCodePath, float x, float y) throws IOException {
        try {
            // If QR code path is null or file doesn't exist, use a default QR placeholder
            if (qrCodePath == null || !Files.exists(qrCodePath)) {
                // Try to load the test QR code from resources as a fallback for tests
                try (InputStream testQrStream = getClass().getResourceAsStream("/images/test-qr.png")) {
                    if (testQrStream != null) {
                        PDImageXObject qrCode = PDImageXObject.createFromByteArray(document, 
                                testQrStream.readAllBytes(), "qr-placeholder");
                        float qrSize = 100;  // Size in points
                        contentStream.drawImage(qrCode, x, y, qrSize, qrSize);
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Could not load test QR image: " + e.getMessage());
                    // Fall through to try with original path
                }
            }
            
            // Use the provided QR code path
            if (qrCodePath != null && Files.exists(qrCodePath)) {
                PDImageXObject qrCode = PDImageXObject.createFromFile(qrCodePath.toString(), document);
                float qrSize = 100;  // Size in points
                contentStream.drawImage(qrCode, x, y, qrSize, qrSize);
            } else {
                // Draw a placeholder rectangle if no QR code is available
                contentStream.setStrokingColor(Color.BLACK);
                contentStream.addRect(x, y, 100, 100);
                contentStream.stroke();
            }
        } catch (IOException e) {
            System.err.println("Error adding QR code: " + e.getMessage());
            // Draw a placeholder rectangle
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.addRect(x, y, 100, 100);
            contentStream.stroke();
        }
    }
    
    /**
     * Draw text centered at a specific position
     */
    private void drawCenteredText(PDPageContentStream contentStream, PDFont font, 
                                 float fontSize, String text, float centerX, float y) throws IOException {
        // Calculate text width to center it
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float startX = centerX - (textWidth / 2);
        
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}