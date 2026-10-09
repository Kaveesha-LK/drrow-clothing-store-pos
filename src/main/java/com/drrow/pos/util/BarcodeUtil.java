package com.drrow.pos.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Barcode utility for generating and rendering Code 128 barcodes and clothing tags.
 * Includes both a pure Java Code 128 standard renderer and ZXing integration.
 */
public class BarcodeUtil {

    // Code 128 pattern table (patterns for characters 0..106)
    // Each string contains 6 digits indicating bar and space widths (sum of digits = 11)
    private static final String[] CODE128_PATTERNS = {
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213", // 0-9
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132", // 10-19
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211", // 20-29
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313", // 30-39
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331", // 40-49
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", // 50-59
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214", // 60-69
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111", // 70-79
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141", // 80-89
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141", // 90-99
        "114131", "311141", "411131", "211412", "211214", "211232", "2331112" // 100-106 (106 is STOP pattern, 7 bars)
    };

    private static final int START_CODE_B = 104;
    private static final int STOP_CODE = 106;

    /**
     * Encodes a string into a boolean array representing Code 128 (Subset B) black/white bars.
     */
    public static boolean[] encodeCode128B(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Barcode text cannot be empty");
        }

        List<Integer> codes = new ArrayList<>();
        codes.add(START_CODE_B);
        int checksum = START_CODE_B;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int code = c - 32;
            if (code < 0 || code > 95) {
                // Out of standard printable ASCII range, clamp to '?'
                code = '?' - 32;
            }
            codes.add(code);
            checksum += (code * (i + 1));
        }

        int checkCode = checksum % 103;
        codes.add(checkCode);
        codes.add(STOP_CODE);

        // Calculate total module width
        int totalModules = 0;
        for (int i = 0; i < codes.size(); i++) {
            int code = codes.get(i);
            String pattern = CODE128_PATTERNS[code];
            for (int p = 0; p < pattern.length(); p++) {
                totalModules += (pattern.charAt(p) - '0');
            }
        }

        // Add quiet zones (10 modules on each side)
        int quietZoneModules = 10;
        totalModules += (quietZoneModules * 2);

        boolean[] bars = new boolean[totalModules];
        int idx = quietZoneModules;

        for (int i = 0; i < codes.size(); i++) {
            int code = codes.get(i);
            String pattern = CODE128_PATTERNS[code];
            boolean isBar = true;
            for (int p = 0; p < pattern.length(); p++) {
                int width = pattern.charAt(p) - '0';
                for (int w = 0; w < width; w++) {
                    bars[idx++] = isBar;
                }
                isBar = !isBar;
            }
        }

        return bars;
    }

    /**
     * Generates a Code 128 barcode image with human-readable text underneath.
     */
    public static BufferedImage generateCode128(String text, int width, int height) {
        // Try ZXing reflection first if present in classpath, else fallback to native pure Java
        BufferedImage zxingImg = tryZXing(text, width, height);
        if (zxingImg != null) {
            return zxingImg;
        }
        return generateCode128Native(text, width, height);
    }

    private static BufferedImage tryZXing(String text, int width, int height) {
        try {
            Class<?> barcodeFormatClass = Class.forName("com.google.zxing.BarcodeFormat");
            Object code128Format = Enum.valueOf((Class<Enum>) barcodeFormatClass, "CODE_128");

            Class<?> multiFormatWriterClass = Class.forName("com.google.zxing.MultiFormatWriter");
            Object writer = multiFormatWriterClass.getDeclaredConstructor().newInstance();

            Method encodeMethod = multiFormatWriterClass.getMethod("encode", String.class, barcodeFormatClass, int.class, int.class);
            Object bitMatrix = encodeMethod.invoke(writer, text, code128Format, width, height - 18);

            Class<?> matrixToImageWriterClass = Class.forName("com.google.zxing.client.j2se.MatrixToImageWriter");
            Method toBufferedImageMethod = matrixToImageWriterClass.getMethod("toBufferedImage", bitMatrix.getClass());
            BufferedImage rawBarcode = (BufferedImage) toBufferedImageMethod.invoke(null, bitMatrix);

            // Add human readable text below the barcode
            BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = finalImg.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
            g2.drawImage(rawBarcode, 0, 0, null);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics();
            int textX = Math.max(4, (width - fm.stringWidth(text)) / 2);
            int textY = height - 4;
            g2.drawString(text, textX, textY);
            g2.dispose();
            return finalImg;
        } catch (Throwable t) {
            // ZXing not available at runtime, fallback seamlessly to native renderer
            return null;
        }
    }

    public static BufferedImage generateCode128Native(String text, int targetWidth, int targetHeight) {
        boolean[] bars = encodeCode128B(text);
        int barcodeHeight = targetHeight - 20; // 20px reserved for text below
        if (barcodeHeight < 15) barcodeHeight = 15;

        BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, targetWidth, targetHeight);

        // Calculate scaling factor
        double scale = (double) targetWidth / bars.length;

        g2.setColor(Color.BLACK);
        for (int i = 0; i < bars.length; i++) {
            if (bars[i]) {
                int x1 = (int) Math.round(i * scale);
                int x2 = (int) Math.round((i + 1) * scale);
                int w = Math.max(1, x2 - x1);
                g2.fillRect(x1, 5, w, barcodeHeight);
            }
        }

        // Draw human readable text below barcode
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = Math.max(4, (targetWidth - textWidth) / 2);
        int textY = targetHeight - 4;
        g2.drawString(text, textX, textY);

        g2.dispose();
        return image;
    }

    /**
     * Renders a clothing tag label layout:
     * D’RROW
     * Product Name
     * SKU: ... | Size: ... | Colour: ...
     * Price: Rs. ...
     * [BARCODE IMAGE]
     * Barcode Number
     */
    public static BufferedImage renderClothingTag(String brand, String productName, String sku,
                                                  String size, String colour, double price,
                                                  String barcodeText, int tagWidthPx, int tagHeightPx) {
        BufferedImage tag = new BufferedImage(tagWidthPx, tagHeightPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tag.createGraphics();

        // High quality rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Tag Background & Border
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, tagWidthPx, tagHeightPx);

        g2.setColor(new Color(220, 224, 230));
        g2.drawRoundRect(2, 2, tagWidthPx - 5, tagHeightPx - 5, 8, 8);

        // Brand Banner Header
        g2.setColor(new Color(26, 34, 56)); // D'RROW Navy
        g2.fillRoundRect(3, 3, tagWidthPx - 6, 26, 6, 6);

        g2.setColor(new Color(197, 160, 89)); // Gold
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fmBrand = g2.getFontMetrics();
        String brandHeader = "D’RROW CLOTHING";
        g2.drawString(brandHeader, (tagWidthPx - fmBrand.stringWidth(brandHeader)) / 2, 20);

        int curY = 44;

        // Product Name
        g2.setColor(new Color(20, 24, 33));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fmProd = g2.getFontMetrics();
        String pName = productName != null ? productName : "Apparel Item";
        if (fmProd.stringWidth(pName) > tagWidthPx - 16) {
            while (pName.length() > 3 && fmProd.stringWidth(pName + "...") > tagWidthPx - 16) {
                pName = pName.substring(0, pName.length() - 1);
            }
            pName += "...";
        }
        g2.drawString(pName, (tagWidthPx - fmProd.stringWidth(pName)) / 2, curY);

        curY += 16;
        // SKU & Attribute badges
        g2.setColor(new Color(80, 90, 105));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String details = "SKU: " + sku;
        g2.drawString(details, 12, curY);

        curY += 15;
        String specLine = "Size: " + size + "   |   Colour: " + colour;
        g2.drawString(specLine, 12, curY);

        curY += 18;
        // Price Badge
        g2.setColor(new Color(197, 160, 89));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String priceText = FormatUtil.formatCurrency(price);
        FontMetrics fmPrice = g2.getFontMetrics();
        g2.drawString(priceText, (tagWidthPx - fmPrice.stringWidth(priceText)) / 2, curY);

        curY += 8;
        // Embedded Barcode
        int bcWidth = tagWidthPx - 20;
        int bcHeight = Math.max(38, tagHeightPx - curY - 10);
        BufferedImage barcodeImg = generateCode128(barcodeText, bcWidth, bcHeight);
        g2.drawImage(barcodeImg, 10, curY, null);

        g2.dispose();
        return tag;
    }
}
