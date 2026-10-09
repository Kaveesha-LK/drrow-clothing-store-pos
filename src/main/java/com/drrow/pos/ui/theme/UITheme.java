package com.drrow.pos.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern, premium fashion-retail design system for D'RROW Clothing Store POS.
 */
public class UITheme {

    // Palette Colors
    public static final Color COLOR_BG_DARK = new Color(17, 24, 39);        // #111827
    public static final Color COLOR_CARD_BG = new Color(31, 41, 55);        // #1F2937
    public static final Color COLOR_CARD_BORDER = new Color(55, 65, 81);    // #374151
    public static final Color COLOR_HEADER_BG = new Color(22, 30, 46);      // #161E2E

    public static final Color COLOR_GOLD = new Color(197, 160, 89);          // #C5A059
    public static final Color COLOR_GOLD_HOVER = new Color(212, 175, 55);    // #D4AF37

    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);       // #2563EB
    public static final Color COLOR_PRIMARY_HOVER = new Color(59, 130, 246);
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981
    public static final Color COLOR_SUCCESS_HOVER = new Color(52, 211, 153);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // #EF4444
    public static final Color COLOR_DANGER_HOVER = new Color(248, 113, 113);
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // #F59E0B
    public static final Color COLOR_WARNING_HOVER = new Color(251, 191, 36);

    public static final Color COLOR_TEXT_PRIMARY = new Color(249, 250, 251);// #F9FAFB
    public static final Color COLOR_TEXT_MUTED = new Color(156, 163, 175);  // #9CA3AF
    public static final Color COLOR_INPUT_BG = new Color(24, 32, 47);       // #18202F
    public static final Color COLOR_TABLE_ROW_ALT = new Color(26, 35, 50);

    // Typography
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_PRICE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 13);

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_CARD_BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        );
    }

    public static void applyGlobalTheme() {
        try {
            // Attempt FlatLaf dark if present
            Class<?> flatDark = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            flatDark.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        UIManager.put("Panel.background", COLOR_BG_DARK);
        UIManager.put("OptionPane.background", COLOR_CARD_BG);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXT_PRIMARY);
    }
}
