package org.hifly.bikedump.gui.panel;

import java.awt.*;

public final class RouteColors {
    private RouteColors() {}

    private static final Color[] ROUTE_BASE_COLORS = new Color[]{
            new Color(0x1f77b4), // blue
            new Color(0xff7f0e), // orange
            new Color(0x2ca02c), // green
            new Color(0xd62728), // red
            new Color(0x9467bd), // purple
            new Color(0x8c564b), // brown
            new Color(0xe377c2), // pink
            new Color(0x7f7f7f), // gray
            new Color(0xbcbd22), // olive
            new Color(0x17becf)  // cyan
    };

    public static Color baseColorForRoute(int routeIndex) {
        return ROUTE_BASE_COLORS[Math.floorMod(routeIndex, ROUTE_BASE_COLORS.length)];
    }
}