package org.hifly.bikedump.gui.panel.components;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MetricCard extends JPanel {
    private final JLabel title = new JLabel();
    private final JLabel value = new JLabel();
    private final JLabel subtitle = new JLabel();

    public MetricCard(String titleText, String valueText, String subtitleText) {
        super();
        setLayout(new BorderLayout(0, 2));
        setOpaque(true);

        title.setText(titleText);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() - 1f));
        title.setForeground(UIManager.getColor("Label.disabledForeground"));

        value.setText(valueText);
        value.setFont(value.getFont().deriveFont(Font.BOLD, value.getFont().getSize2D() + 2f));

        subtitle.setText(subtitleText == null ? "" : subtitleText);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, subtitle.getFont().getSize2D() - 1f));
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(title);
        inner.add(value);
        if (subtitleText != null && !subtitleText.isBlank()) {
            inner.add(subtitle);
        }

        add(inner, BorderLayout.CENTER);

        Color borderColor = UIManager.getColor("Component.borderColor");
        if (borderColor == null) borderColor = new Color(0, 0, 0, 40);

        setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                UiSpacing.pad(10, 12, 10, 12)
        ));
    }
}