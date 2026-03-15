package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class RouteOverlay {
    private final String name;
    private final List<ICoordinate> coordinates;
    private final Map<String, WaypointSegment> waypointsNewKm;
    private final Color baseColor;

    public RouteOverlay(String name,
                        List<ICoordinate> coordinates,
                        Map<String, WaypointSegment> waypointsNewKm,
                        Color baseColor) {
        this.name = name;
        this.coordinates = coordinates;
        this.waypointsNewKm = waypointsNewKm;
        this.baseColor = baseColor;
    }

    public String getName() { return name; }
    public List<ICoordinate> getCoordinates() { return coordinates; }
    public Map<String, WaypointSegment> getWaypointsNewKm() { return waypointsNewKm; }
    public Color getBaseColor() { return baseColor; }
}