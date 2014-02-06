package org.hifly.geomapviewer.gui;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.hifly.geomapviewer.gui.marker.CircleMarker;
import org.hifly.geomapviewer.gui.marker.LineMarker;
import org.hifly.geomapviewer.storage.GPSStorage;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 03/02/14
 */
public class MapViewer extends JMapViewer {
    protected Logger log = LoggerFactory.getLogger(MapViewer.class);

    public MapViewer(
            List<ICoordinate> coordinates,
            Map<String, WaypointKm> coordinatesNewKm,
            int zoomLevel) {
        super();

        double limitFlat = 200;
        double limitHill = 600;
        double limitMountain = 800;
        double currentLimit = 0;

        List<ICoordinate> listTemp = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            String key = GpsUtility.getKeyForCoordinatesMap(String.valueOf(coordinates.get(i).getLat()) + "-" + String.valueOf(coordinates.get(i).getLon()));
            Double obj = GPSStorage.gpsElevationMap.get(key);
            double ele = 0;
            if (obj == null) {
                //skip element
                log.warn("Elevation not found for:"+key);
            }
            else {
                ele = GPSStorage.gpsElevationMap.get(key);
            }
            if (listTemp.size() == 0) {
                if (ele < limitFlat) {
                    currentLimit = limitFlat;
                }
                else if (ele >= limitFlat && ele < limitHill) {
                    currentLimit = limitHill;
                }
                else {
                    currentLimit = limitMountain;
                }
            }

            if (currentLimit == limitFlat) {
                if (ele > currentLimit) {
                    MapPolygonImpl poly = new LineMarker(listTemp, Color.GREEN);
                    addMapPolygon(poly);
                    listTemp = new ArrayList<>();
                    i--;
                }
                else {
                    listTemp.add(coordinates.get(i));
                }
            }
            else if (currentLimit == limitHill) {
                if (ele < limitFlat || ele > limitHill) {
                    MapPolygonImpl poly = new LineMarker(listTemp, Color.YELLOW);
                    addMapPolygon(poly);
                    listTemp = new ArrayList<>();
                    i--;
                }
                else {
                    listTemp.add(coordinates.get(i));
                }
            }
            else if (currentLimit == limitMountain) {
                if (ele < limitHill) {
                    MapPolygonImpl poly = new LineMarker(listTemp, Color.RED);
                    addMapPolygon(poly);
                    listTemp = new ArrayList<>();
                    i--;
                }
                else {
                    listTemp.add(coordinates.get(i));
                }
            }
        }


        if (listTemp.size() > 0) {
            Color color = null;
            if (currentLimit <= limitFlat) {
                color = Color.GREEN;
            }
            else if (currentLimit > limitFlat && currentLimit <= limitMountain) {
                color = Color.YELLOW;
            }
            else {
                color = Color.RED;
            }

            MapPolygonImpl poly = new LineMarker(listTemp, color);
            addMapPolygon(poly);
        }

        for (int i = 0; i < coordinates.size(); i++) {
            String key = String.valueOf(coordinates.get(i).getLat())
                    + "-"
                    +
                    String.valueOf(coordinates.get(i).getLon());
            WaypointKm waypoint = coordinatesNewKm.get(key);
            if (waypoint != null) {
                addMapMarker(
                        new CircleMarker(
                                coordinates.get(i).getLat(),
                                coordinates.get(i).getLon(),
                                String.valueOf(waypoint.getKm())));
            }
        }

        setDisplayPositionByLatLon(coordinates.get(0).getLat(), coordinates.get(0).getLon(), zoomLevel);
        setZoom(zoomLevel);
        setDisplayToFitMapMarkers();

    }
}
