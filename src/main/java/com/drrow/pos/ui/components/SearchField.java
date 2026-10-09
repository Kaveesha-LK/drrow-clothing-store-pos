package com.drrow.pos.ui.components;

import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SearchField extends JTextField {

    private final String placeholder;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        setFont(UITheme.FONT_REGULAR);
        setBackground(UITheme.COLOR_INPUT_BG);
        setForeground(UITheme.COLOR_TEXT_PRIMARY);
        setCaretColor(Color.WHITE);
        setPreferredSize(new Dimension(200, 36));

        setBorder(new CompoundBorder(
            new LineBorder(UITheme.COLOR_CARD_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(UITheme.COLOR_TEXT_MUTED);
            g2.setFont(getFont());
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, insets.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
