package org.hifly.bikedump.gui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
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

    // Detects whether the OS is currently in dark mode by querying native
    // platform desktop properties, avoiding any dependency on the currently
    // installed LAF (which would create a circular dependency at runtime).
    private static boolean isSystemDark() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // macOS (10.14+ / Java 11+): the JDK surfaces the OS appearance as an
        // AWT desktop property that is independent of the installed LaF.
        if (SystemInfo.isMacOS) {
            Object dark = toolkit.getDesktopProperty("apple.awt.isDark");
            if (dark instanceof Boolean) {
                return (Boolean) dark;
            }
        }

        // Windows: query the native system window background colour directly
        // from the toolkit (reads Windows COLOR_WINDOW system colour, which
        // follows the "Apps" dark-mode setting in Windows 10/11 themes).
        if (SystemInfo.isWindows) {
            Object frameBackground = toolkit.getDesktopProperty("win.frame.backgroundColor");
            if (frameBackground instanceof Color) {
                Color c = (Color) frameBackground;
                double brightness = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
                return brightness < 128;
            }
        }

        // Linux (GNOME/GTK): the active GTK theme name typically contains
        // "dark" when the user has selected a dark variant.
        if (SystemInfo.isLinux) {
            Object gtkTheme = toolkit.getDesktopProperty("gnome.Net/ThemeName");
            if (gtkTheme instanceof String) {
                return ((String) gtkTheme).toLowerCase(Locale.ROOT).contains("dark");
            }
        }

        return false; // default to light on unsupported platforms or when the
                     // relevant desktop property is not available (e.g., headless
                     // environments, KDE on Linux, or older macOS/Windows versions).
    }
}