package com.kousen.cert.controller;

import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.service.*;
import com.kousen.cert.template.ElegantTemplate;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final PdfService pdfService;
    private final PdfSigner pdfSigner;

    public CertificateController(PdfService pdfService) {
        this.pdfService = pdfService;
        Path ksPath = Paths.get(System.getProperty("user.home"), ".cert_keystore.p12");
        this.pdfSigner = new PdfSigner(new KeyStoreProvider(ksPath));
    }

    @PostMapping(produces = "application/pdf")
    public ResponseEntity<FileSystemResource> create(@Valid @RequestBody CertificateRequest req) throws Exception {
        Path unsigned = pdfService.createPdf(new ElegantTemplate(), req);
        Path signed   = pdfSigner.sign(unsigned);
        FileSystemResource res = new FileSystemResource(signed);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate.pdf\"")
                .body(res);
    }
    
    @GetMapping("/books")
    public ResponseEntity<Map<String, Object>> getAvailableBooks() {
        Map<String, Object> response = new HashMap<>();
        response.put("availableBooks", CertificateRequest.ALLOWED_BOOK_TITLES);
        return ResponseEntity.ok(response);
    }
    
}
