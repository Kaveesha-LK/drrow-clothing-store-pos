package com.drrow.pos.ui.components;

import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class KpiCard extends JPanel {

    private final JLabel lblTitle = new JLabel();
    private final JLabel lblValue = new JLabel();
    private final JLabel lblSubtitle = new JLabel();
    private final Color accentColor;

    public KpiCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : UITheme.COLOR_GOLD;
        setLayout(new BorderLayout(0, 6));
        setBackground(UITheme.COLOR_CARD_BG);
        setBorder(UITheme.createCardBorder());

        // Header / Title
        lblTitle.setText(title.toUpperCase());
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        // Value
        lblValue.setText(initialValue);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblValue.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        // Subtitle
        lblSubtitle.setText(subtitle);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel pnlCenter = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlCenter.setOpaque(false);
        pnlCenter.add(lblValue);
        pnlCenter.add(lblSubtitle);

        add(lblTitle, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Left accent bar
        g2.setColor(accentColor);
        g2.fillRoundRect(2, 6, 4, getHeight() - 12, 4, 4);

        g2.dispose();
    }
}
