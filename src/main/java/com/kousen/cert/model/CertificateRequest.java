package com.kousen.cert.model;

import java.util.Optional;

public record CertificateRequest(
        String purchaserName,
        String bookTitle,
        Optional<String> purchaserEmail
) { }
