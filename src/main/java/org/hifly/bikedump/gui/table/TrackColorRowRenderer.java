package org.hifly.bikedump.gui.table;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Map;

public class TrackColorRowRenderer implements TableCellRenderer {

    private final TableCellRenderer delegate;
    private final Map<String, Color> colorByTrackName;
    private final int nameColumnIndex;

    public TrackColorRowRenderer(TableCellRenderer delegate,
                                Map<String, Color> colorByTrackName,
                                int nameColumnIndex) {
        this.delegate = delegate;
        this.colorByTrackName = colorByTrackName;
        this.nameColumnIndex = nameColumnIndex;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component comp = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!(comp instanceof JComponent)) return comp;

        // Find the route color using the "Name" column value of this row
        Object nameObj = table.getValueAt(row, nameColumnIndex);
        String trackName = nameObj != null ? nameObj.toString() : null;
        Color base = (trackName != null) ? colorByTrackName.get(trackName) : null;

        // Apply a custom border that paints a left stripe
        JComponent jc = (JComponent) comp;

        // keep existing border, add stripe as outer border
        Border existing = jc.getBorder();
        int stripeW = 6;

        Color stripeColor = base;
        if (stripeColor != null && isSelected) {
            // a bit darker so it stays visible on selection background
            stripeColor = stripeColor.darker();
        }

        jc.setBorder(new StripeBorder(stripeW, stripeColor, existing));

        return comp;
    }

    /**
     * Outer border that paints a colored stripe on the left,
     * then delegates insets to the original border.
     */
    private static class StripeBorder implements Border {
        private final int stripeWidth;
        private final Color stripeColor;
        private final Border inner;

        private StripeBorder(int stripeWidth, Color stripeColor, Border inner) {
            this.stripeWidth = stripeWidth;
            this.stripeColor = stripeColor;
            this.inner = inner;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets in = inner != null ? inner.getBorderInsets(c) : new Insets(0, 0, 0, 0);
            return new Insets(in.top, in.left + stripeWidth, in.bottom, in.right);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (stripeColor != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(stripeColor);
                    g2.fillRect(x, y, stripeWidth, height);
                } finally {
                    g2.dispose();
                }
            }
            if (inner != null) {
                inner.paintBorder(c, g, x + stripeWidth, y, width - stripeWidth, height);
            }
        }
    }
}