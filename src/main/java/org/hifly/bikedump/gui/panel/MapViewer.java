package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.marker.CircleMarker;
import org.hifly.bikedump.gui.marker.LineMarker;
import org.hifly.bikedump.gui.marker.TooltipMarker;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.utility.GPSUtility;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

public class MapViewer extends JMapViewer implements MouseListener, MouseMotionListener {
   
    private static final long serialVersionUID = 24L;

    private Logger log = LoggerFactory.getLogger(MapViewer.class);

    public Map<String,WaypointSegment>  mapCircleCoordinates = new Hashtable<>();
    public TooltipMarker lastOpenedMarker = null;

    //TODO extremely slow even with 4 routes
    public MapViewer(
            List<List<ICoordinate>> coordinates,
            List<Map<String, WaypointSegment>> coordinatesNewKm,
            int zoomLevel,
            double initialLat,
            double initialLon) {
        this(coordinates, coordinatesNewKm, zoomLevel);
        setDisplayPosition(
                (int)initialLat,
                (int)initialLon,
                zoomLevel);
    }

    public MapViewer(
            List<List<ICoordinate>> coordinates,
            List<Map<String, WaypointSegment>> coordinatesNewKm,
            int zoomLevel) {
        super();

        addMouseListener(this);

        double limitFlat = 200;
        double limitHill = 600;
        double limitMountain = 800;
        double currentLimit = 0;


        int indexMap = 0;
        if (coordinates != null) {
            for (List<ICoordinate> listCoordinates : coordinates) {
                List<ICoordinate> listTemp = new ArrayList<>();

                Map<String, WaypointSegment> tempMap = coordinatesNewKm.get(indexMap);
                for (int i = 0; i < listCoordinates.size(); i++) {
                    String key = GPSUtility.getKeyForCoordinatesMap(
                            String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon()));
                    double ele = 0;
                    if(GeoMapStorage.gpsElevationMap != null) {
                        Double obj = GeoMapStorage.gpsElevationMap.get(key);
                        if (obj == null) {
                            if(GeoMapStorage.gpsElevationMapFallback.containsKey(String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon())))
                                ele = GeoMapStorage.gpsElevationMapFallback.get(String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon()));
                            else
                                //skip element
                                log.debug("Elevation not found for:" + key);
                        } else {
                            ele = GeoMapStorage.gpsElevationMap.get(key);
                        }
                    }
                    else {
                        if(GeoMapStorage.gpsElevationMapFallback.containsKey(String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon())))
                            ele = GeoMapStorage.gpsElevationMapFallback.get(String.valueOf(listCoordinates.get(i).getLat()) + "-" + String.valueOf(listCoordinates.get(i).getLon()));
                        else
                            //skip element
                            log.debug("Elevation not found for:" + key);
                    }

                    if (listTemp.size() == 0) {
                        if (ele < limitFlat)
                            currentLimit = limitFlat;
                        else if (ele >= limitFlat && ele < limitHill)
                            currentLimit = limitHill;
                        else
                            currentLimit = limitMountain;
                    }

                    if (currentLimit == limitFlat) {
                        if (ele > currentLimit) {
                            MapPolygonImpl poly = new LineMarker(listTemp, Color.GREEN);
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else
                            listTemp.add(listCoordinates.get(i));
                    } else if (currentLimit == limitHill) {
                        if (ele < limitFlat || ele > limitHill) {
                            MapPolygonImpl poly = new LineMarker(listTemp, Color.YELLOW);
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else
                            listTemp.add(listCoordinates.get(i));
                    } else if (currentLimit == limitMountain) {
                        if (ele < limitHill) {
                            MapPolygonImpl poly = new LineMarker(listTemp, Color.RED);
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else
                            listTemp.add(listCoordinates.get(i));
                    }
                }

                if (listTemp.size() > 0) {
                    Color color;
                    if (currentLimit <= limitFlat)
                        color = Color.GREEN;
                    else if (currentLimit > limitFlat && currentLimit < limitMountain)
                        color = Color.YELLOW;
                    else
                        color = Color.RED;

                    MapPolygonImpl poly = new LineMarker(listTemp, color);
                    addMapPolygon(poly);
                }

                for (int i = 0; i < listCoordinates.size(); i++) {
                    String key = listCoordinates.get(i).getLat()
                            + "-"
                            +
                            listCoordinates.get(i).getLon();
                    if(tempMap !=null) {
                        WaypointSegment waypoint = tempMap.get(key);
                        if (waypoint != null) {
                            CircleMarker c = new CircleMarker(
                                    listCoordinates.get(i).getLat(),
                                    listCoordinates.get(i).getLon(),
                                    waypoint,
                                    this);
                            addMapMarker(c);
                            mapCircleCoordinates.put(listCoordinates.get(i).getLat() + "-" + listCoordinates.get(i).getLon(), waypoint);
                        }
                    }
                }
                indexMap++;

            }
        }

        //final settings for mapviewer
        if (coordinates != null && !coordinates.isEmpty()) {
            setDisplayPosition(
                    (int)coordinates.get(0).get(0).getLat(),
                    (int)coordinates.get(0).get(0).getLon(), zoomLevel);
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

    @Override
    public void mouseClicked(MouseEvent me) {
        ICoordinate position = getPosition(me.getPoint());

        WaypointSegment waypoint = this.mapCircleCoordinates.get(position.getLat() + "-" + position.getLon());
        if(waypoint == null) {
            double distance = 1111111111;
            for (Map.Entry<String, WaypointSegment> entry : mapCircleCoordinates.entrySet()) {
                    double lat = Math.abs(position.getLat()-Double.valueOf(entry.getKey().split("-")[0]));
                    double lon = Math.abs(position.getLon()-Double.valueOf(entry.getKey().split("-")[1]));
                    double tot = lat+lon;
                    if(tot < distance) {
                        distance = tot;
                        waypoint = entry.getValue();
                    }
            }
        }

        log.debug("found marker:" + waypoint.getUnit());

        if(lastOpenedMarker != null)
           removeMapMarker(lastOpenedMarker);

        TooltipMarker t = new TooltipMarker(
                position.getLat(),
                position.getLon(),
                me.getPoint().getX(),
                me.getPoint().getY(),
                waypoint,
                this);
        addMapMarker(t);
        lastOpenedMarker = t;


    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(lastOpenedMarker!=null) {
            removeMapMarker(lastOpenedMarker);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(lastOpenedMarker != null)
            removeMapMarker(lastOpenedMarker);
    }
}
