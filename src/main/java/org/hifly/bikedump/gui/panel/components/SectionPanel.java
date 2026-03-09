package org.hifly.bikedump.gui.panel.components;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SectionPanel extends JPanel {
    private final JToggleButton toggle;
    private final JPanel body;

    public SectionPanel(String title, JComponent bodyContent, boolean expanded) {
        super(new BorderLayout());
        setOpaque(false);

        Color borderColor = UIManager.getColor("Component.borderColor");
        if (borderColor == null) borderColor = new Color(0, 0, 0, 40);

        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, 1, true), UiSpacing.pad(8, 10, 8, 10)));

        toggle = new JToggleButton(title, expanded);
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setHorizontalAlignment(SwingConstants.LEADING);
        toggle.setFont(toggle.getFont().deriveFont(Font.BOLD));

        body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(bodyContent, BorderLayout.CENTER);
        body.setVisible(expanded);

        toggle.addActionListener(e -> body.setVisible(toggle.isSelected()));

        card.add(toggle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }
}