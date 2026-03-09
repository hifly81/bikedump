package org.hifly.bikedump.gui.theme;

public enum ThemePreference {
    SYSTEM,
    LIGHT,
    DARK;

    public static ThemePreference fromString(String value) {
        if (value == null) {
            return SYSTEM;
        }
        try {
            return ThemePreference.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SYSTEM;
        }
    }
}