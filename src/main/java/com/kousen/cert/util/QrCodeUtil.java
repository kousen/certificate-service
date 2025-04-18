package com.kousen.cert.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public final class QrCodeUtil {
    private QrCodeUtil() {}

    private static final String URL = "https://youtube.com/@TalesFromTheJarSide";

    public static String dataUri(int sizePx) {
        try {
            var bitMatrix = new QRCodeWriter().encode(URL, BarcodeFormat.QR_CODE, sizePx, sizePx);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(img, "PNG", baos);
                var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                return "data:image/png;base64," + base64;
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
