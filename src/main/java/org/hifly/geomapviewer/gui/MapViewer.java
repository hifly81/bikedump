package org.hifly.geomapviewer.gui;

import org.hifly.geomapviewer.domain.gps.Waypoint;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 * @author
 * @date 03/02/14
 */
public class MapViewer extends JMapViewer implements MouseListener {
    protected Logger log = LoggerFactory.getLogger(MapViewer.class);

    public Map<String, WaypointKm> mapCircleCoordinates = new HashMap();

    public MapViewer(
            List<List<ICoordinate>> coordinates,
            List<Map<String, WaypointKm>> coordinatesNewKm,
            int zoomLevel) {
        super();

        //addMouseListener(this);

        double limitFlat = 200;
        double limitHill = 600;
        double limitMountain = 800;
        double currentLimit = 0;

        int indexMap = 0;
        for (List<ICoordinate> listCoordinates : coordinates) {
            List<ICoordinate> listTemp = new ArrayList<>();
            Map<String, WaypointKm> tempMap = coordinatesNewKm.get(indexMap);
            for (int i = 0; i < listCoordinates.size(); i++) {
                String key = GpsUtility.getKeyForCoordinatesMap(
                        String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon()));
                Double obj = GPSStorage.gpsElevationMap.get(key);
                double ele = 0;
                if (obj == null) {
                    //skip element
                    log.warn("Elevation not found for:" + key);
                } else {
                    ele = GPSStorage.gpsElevationMap.get(key);
                }
                if (listTemp.size() == 0) {
                    if (ele < limitFlat) {
                        currentLimit = limitFlat;
                    } else if (ele >= limitFlat && ele < limitHill) {
                        currentLimit = limitHill;
                    } else {
                        currentLimit = limitMountain;
                    }
                }

                if (currentLimit == limitFlat) {
                    if (ele > currentLimit) {
                        MapPolygonImpl poly = new LineMarker(listTemp, Color.GREEN);
                        addMapPolygon(poly);
                        listTemp = new ArrayList<>();
                        i--;
                    } else {
                        listTemp.add(listCoordinates.get(i));
                    }
                } else if (currentLimit == limitHill) {
                    if (ele < limitFlat || ele > limitHill) {
                        MapPolygonImpl poly = new LineMarker(listTemp, Color.YELLOW);
                        addMapPolygon(poly);
                        listTemp = new ArrayList<>();
                        i--;
                    } else {
                        listTemp.add(listCoordinates.get(i));
                    }
                } else if (currentLimit == limitMountain) {
                    if (ele < limitHill) {
                        MapPolygonImpl poly = new LineMarker(listTemp, Color.RED);
                        addMapPolygon(poly);
                        listTemp = new ArrayList<>();
                        i--;
                    } else {
                        listTemp.add(listCoordinates.get(i));
                    }
                }
            }


            if (listTemp.size() > 0) {
                Color color = null;
                if (currentLimit <= limitFlat) {
                    color = Color.GREEN;
                } else if (currentLimit > limitFlat && currentLimit <= limitMountain) {
                    color = Color.YELLOW;
                } else {
                    color = Color.RED;
                }

                MapPolygonImpl poly = new LineMarker(listTemp, color);
                addMapPolygon(poly);
            }

            for (int i = 0; i < listCoordinates.size(); i++) {
                String key = String.valueOf(listCoordinates.get(i).getLat())
                        + "-"
                        +
                        String.valueOf(listCoordinates.get(i).getLon());
                WaypointKm waypoint = tempMap.get(key);
                if (waypoint != null) {
                    CircleMarker c = new CircleMarker(
                            listCoordinates.get(i).getLat(),
                            listCoordinates.get(i).getLon(),
                            waypoint,
                            this);
                    addMapMarker(c);
                }
            }
            indexMap++;

        }

        //final settings for mapviewer

        if (coordinates != null && !coordinates.isEmpty()) {
            setDisplayPositionByLatLon(
                    coordinates.get(0).get(0).getLat(),
                    coordinates.get(0).get(0).getLon(), zoomLevel);
        }
        setZoom(zoomLevel);
        setDisplayToFitMapMarkers();
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //TODO define a method for method clicked
    @Override
    public void mouseClicked(MouseEvent me) {

        /*int screenX = me.getX();
        int screenY = me.getY();
        System.out.println(screenX + "-" + screenY);
        System.out.println(me.getXOnScreen() + "-" + me.getYOnScreen());
        System.out.println(me.getPoint().getX() + "-" + me.getPoint().getY());

        WaypointKm waypoint = this.mapCircleCoordinates.get(screenX + "-" + screenY);
        if (waypoint != null) {
            System.out.println("found screen(X,Y) for marker:" + waypoint.getKm() + " -->" + screenX + "," + screenY);
        } else {
            int distance = 10000;
            WaypointKm found = null;
            //find nearest
            for (Map.Entry<String, WaypointKm> entry : this.mapCircleCoordinates.entrySet()) {
                int sizex = Integer.valueOf(entry.getKey().split("-")[0]);
                int sizey = Integer.valueOf(entry.getKey().split("-")[1]);
                int diffx = screenX;
                int diffy = screenY;
                int diff = diffx + diffy;
                if (diff < distance) {
                    distance = diff;
                    found = entry.getValue();
                }

            }

            System.out.println("found approx screen(X,Y) for marker:" + found.getKm() + " -->" + screenX + "," + screenY);
        }  */


    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
