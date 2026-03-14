package org.hifly.bikedump.gui.marker;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.awt.*;

public class NamedDotMarker extends MapMarkerDot {

    private final String label;

    public NamedDotMarker(double lat, double lon, Color fill, String label) {
        super(new Coordinate(lat, lon));
        this.label = label;
        setBackColor(fill);
        setColor(fill.darker());
    }

    @Override
    public String getName() {
        return label;
    }
}