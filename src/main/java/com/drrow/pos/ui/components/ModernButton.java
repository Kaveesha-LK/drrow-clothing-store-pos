package com.drrow.pos.ui.components;

import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY, SUCCESS, DANGER, WARNING, SECONDARY, GOLD
    }

    private final ButtonType type;
    private boolean isHovered = false;

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        setFont(UITheme.FONT_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color baseColor;
        Color hoverColor;
        Color textColor = Color.WHITE;

        switch (type) {
            case SUCCESS:
                baseColor = UITheme.COLOR_SUCCESS;
                hoverColor = UITheme.COLOR_SUCCESS_HOVER;
                break;
            case DANGER:
                baseColor = UITheme.COLOR_DANGER;
                hoverColor = UITheme.COLOR_DANGER_HOVER;
                break;
            case WARNING:
                baseColor = UITheme.COLOR_WARNING;
                hoverColor = UITheme.COLOR_WARNING_HOVER;
                textColor = Color.BLACK;
                break;
            case GOLD:
                baseColor = UITheme.COLOR_GOLD;
                hoverColor = UITheme.COLOR_GOLD_HOVER;
                textColor = new Color(20, 24, 33);
                break;
            case SECONDARY:
                baseColor = UITheme.COLOR_CARD_BORDER;
                hoverColor = new Color(75, 85, 99);
                textColor = UITheme.COLOR_TEXT_PRIMARY;
                break;
            case PRIMARY:
            default:
                baseColor = UITheme.COLOR_PRIMARY;
                hoverColor = UITheme.COLOR_PRIMARY_HOVER;
                break;
        }

        g2.setColor(isHovered ? hoverColor : baseColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
