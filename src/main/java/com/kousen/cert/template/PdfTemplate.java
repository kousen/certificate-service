package com.kousen.cert.template;

import com.kousen.cert.model.CertificateRequest;

/**
 * @deprecated This interface is no longer used with the PDFBox implementation
 */
@Deprecated
public sealed interface PdfTemplate permits ElegantTemplate {
    String html(CertificateRequest request);
}
