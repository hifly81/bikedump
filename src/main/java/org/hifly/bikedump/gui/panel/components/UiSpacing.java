package org.hifly.bikedump.gui.panel.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class UiSpacing {
    public static final int GAP_XS = 6;
    public static final int GAP_SM = 10;
    public static final int GAP_MD = 14;
    public static final int GAP_LG = 18;

    private UiSpacing() {}

    public static Border pad(int all) {
        return new EmptyBorder(all, all, all, all);
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static Component vgap(int px) {
        return Box.createVerticalStrut(px);
    }

    public static Component hgap(int px) {
        return Box.createHorizontalStrut(px);
    }
}