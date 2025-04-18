package com.kousen.cert.template;

import com.kousen.cert.model.CertificateRequest;

public sealed interface PdfTemplate permits ElegantTemplate {
    String html(CertificateRequest request);
}
