package org.hifly.bikedump.gui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.prefs.Preferences;

public final class ThemeManager {
    private static final String PREF_KEY = "ui.theme";
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {}

    public static ThemePreference getThemePreference() {
        return ThemePreference.fromString(PREFS.get(PREF_KEY, ThemePreference.SYSTEM.name()));
    }

    public static void setThemePreference(ThemePreference pref) {
        Objects.requireNonNull(pref, "pref");
        PREFS.put(PREF_KEY, pref.name());
    }

    public static void initLookAndFeelEarly() {
        applyTheme(getThemePreference(), null);
    }

    public static void applyTheme(ThemePreference pref, Component root) {
        Objects.requireNonNull(pref, "pref");

        // Decide which LAF to install
        boolean dark = switch (pref) {
            case DARK -> true;
            case LIGHT -> false;
            case SYSTEM -> isSystemDark();
        };

        // Install LAF
        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            // Global tuning (subtle “modern” defaults)
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Table.showHorizontalLines", false);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TitlePane.showIcon", true);

            // A slightly nicer default font size (keeps it conservative)
            Font base = UIManager.getFont("defaultFont");
            if (base != null) {
                UIManager.put("defaultFont", base.deriveFont(Math.max(13f, base.getSize2D())));
            }

        } catch (Exception e) {
            // If something goes wrong, fall back to system LAF
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        if (root != null) {
            SwingUtilities.updateComponentTreeUI(root);
            root.invalidate();
            root.validate();
            root.repaint();
        } else {
            // update all windows if no root passed
            FlatLaf.updateUI();
        }
    }

    // Heuristic "system dark" detection (portable-ish). Good enough for toggle UX.
    private static boolean isSystemDark() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) return false;
        // perceived brightness
        double brightness = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue());
        return brightness < 128;
    }
}