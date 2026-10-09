package com.drrow.pos.ui.components;

import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setFont(UITheme.FONT_REGULAR);
        setRowHeight(32);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(UITheme.COLOR_CARD_BG);
        setForeground(UITheme.COLOR_TEXT_PRIMARY);
        setSelectionBackground(new Color(45, 60, 85));
        setSelectionForeground(Color.WHITE);

        // Header Style
        JTableHeader header = getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setBackground(UITheme.COLOR_HEADER_BG);
        header.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        // Custom Zebra Cell Renderer
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.COLOR_CARD_BG : UITheme.COLOR_TABLE_ROW_ALT);
                    c.setForeground(UITheme.COLOR_TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(45, 60, 85));
                    c.setForeground(Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }
}
