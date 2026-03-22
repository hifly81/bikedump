package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.marker.CircleMarker;
import org.hifly.bikedump.gui.marker.LineMarker;
import org.hifly.bikedump.gui.marker.NamedDotMarker;
import org.hifly.bikedump.gui.marker.TooltipMarker;
import org.hifly.bikedump.gui.panel.tiles.HumanitarianTileSource;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.utility.OfflineTileSource;
import org.hifly.bikedump.utility.GPSUtility;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

import static org.hifly.bikedump.gui.panel.RouteColors.baseColorForRoute;

public class MapViewer extends JMapViewer implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 24L;

    private final Logger log = LoggerFactory.getLogger(MapViewer.class);

    public Map<String, WaypointSegment> mapCircleCoordinates = new Hashtable<>();
    public TooltipMarker lastOpenedMarker = null;

    public enum MapStyle {
        CITY,
        NATURE,
        CYCLE
    }

    private volatile MapStyle currentStyle = MapStyle.CITY;


    private enum ElevBand { FLAT, HILL, MOUNTAIN }

    private static Color blend(Color c1, Color c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(c1.getRed() * (1 - t) + c2.getRed() * t);
        int g = Math.round(c1.getGreen() * (1 - t) + c2.getGreen() * t);
        int b = Math.round(c1.getBlue() * (1 - t) + c2.getBlue() * t);
        return new Color(r, g, b);
    }

    private static Color colorForRouteAndBand(int routeIndex, ElevBand band) {
        Color base = baseColorForRoute(routeIndex);
        // same hue, different brightness
        return switch (band) {
            case FLAT -> blend(base, Color.WHITE, 0.45f);     // lighter
            case HILL -> blend(base, Color.WHITE, 0.20f);     // mid
            case MOUNTAIN -> blend(base, Color.BLACK, 0.25f); // darker
        };
    }

    public MapViewer(List<RouteOverlay> routes, int zoomLevel) {
        super();

        configureOfflineTiles();

        // Default style (only applies if NOT in offline mode)
        setMapStyle(MapStyle.CITY);

        // Add the on-map toggle for Nature
        installNatureToggle();

        addMouseListener(this);

        double limitFlat = 200;
        double limitHill = 600;
        double limitMountain = 800;
        double currentLimit = 0;

        if (routes != null) {
            int routeIndex = 0;

            for (RouteOverlay route : routes) {
                if (route == null || route.getCoordinates() == null || route.getCoordinates().isEmpty()) {
                    routeIndex++;
                    continue;
                }

                final String routeName = (route.getName() == null || route.getName().isBlank())
                        ? ("Track " + (routeIndex + 1))
                        : route.getName();

                final Color base = baseColorForRoute(routeIndex);

                List<ICoordinate> listCoordinates = route.getCoordinates();
                Map<String, WaypointSegment> tempMap = route.getWaypointsNewKm();

                // Add START/END markers to identify a route even if overlapped
                ICoordinate start = listCoordinates.get(0);
                ICoordinate end = listCoordinates.get(listCoordinates.size() - 1);

                addMapMarker(new NamedDotMarker(start.getLat(), start.getLon(), base, "START: " + routeName));
                addMapMarker(new NamedDotMarker(end.getLat(), end.getLon(), base.darker(), "END: " + routeName));

                // Draw polyline segments (altitude bands) but route-tinted
                List<ICoordinate> listTemp = new ArrayList<>();

                for (int i = 0; i < listCoordinates.size(); i++) {
                    String keyForElevation = GPSUtility.getKeyForCoordinatesMap(
                            listCoordinates.get(i).getLat() + "-" + listCoordinates.get(i).getLon());

                    double ele = 0;
                    if (GeoMapStorage.gpsElevationMap != null) {
                        Double obj = GeoMapStorage.gpsElevationMap.get(keyForElevation);
                        if (obj == null) {
                            Double fb = GeoMapStorage.gpsElevationMapFallback.get(
                                    listCoordinates.get(i).getLat() + "-" + listCoordinates.get(i).getLon());
                            if (fb != null) ele = fb;
                            else log.debug("Elevation not found for:" + keyForElevation);
                        } else {
                            ele = obj;
                        }
                    } else {
                        Double fb = GeoMapStorage.gpsElevationMapFallback.get(
                                listCoordinates.get(i).getLat() + "-" + listCoordinates.get(i).getLon());
                        if (fb != null) ele = fb;
                        else log.debug("Elevation not found for:" + keyForElevation);
                    }

                    if (listTemp.isEmpty()) {
                        if (ele < limitFlat) currentLimit = limitFlat;
                        else if (ele >= limitFlat && ele < limitHill) currentLimit = limitHill;
                        else currentLimit = limitMountain;
                    }

                    if (currentLimit == limitFlat) {
                        if (ele > currentLimit) {
                            MapPolygonImpl poly = new LineMarker(listTemp, colorForRouteAndBand(routeIndex, ElevBand.FLAT));
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else {
                            listTemp.add(listCoordinates.get(i));
                        }
                    } else if (currentLimit == limitHill) {
                        if (ele < limitFlat || ele > limitHill) {
                            MapPolygonImpl poly = new LineMarker(listTemp, colorForRouteAndBand(routeIndex, ElevBand.HILL));
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else {
                            listTemp.add(listCoordinates.get(i));
                        }
                    } else if (currentLimit == limitMountain) {
                        if (ele < limitHill) {
                            MapPolygonImpl poly = new LineMarker(listTemp, colorForRouteAndBand(routeIndex, ElevBand.MOUNTAIN));
                            addMapPolygon(poly);
                            listTemp = new ArrayList<>();
                            i--;
                        } else {
                            listTemp.add(listCoordinates.get(i));
                        }
                    }
                }

                if (!listTemp.isEmpty()) {
                    ElevBand band;
                    if (currentLimit <= limitFlat) band = ElevBand.FLAT;
                    else if (currentLimit > limitFlat && currentLimit < limitMountain) band = ElevBand.HILL;
                    else band = ElevBand.MOUNTAIN;

                    MapPolygonImpl poly = new LineMarker(listTemp, colorForRouteAndBand(routeIndex, band));
                    addMapPolygon(poly);
                }

                // Existing km-circle markers
                if (tempMap != null) {
                    for (int i = 0; i < listCoordinates.size(); i++) {
                        String key = listCoordinates.get(i).getLat() + "-" + listCoordinates.get(i).getLon();
                        WaypointSegment waypoint = tempMap.get(key);
                        if (waypoint != null) {
                            CircleMarker c = new CircleMarker(
                                    listCoordinates.get(i).getLat(),
                                    listCoordinates.get(i).getLon(),
                                    waypoint,
                                    this);
                            addMapMarker(c);
                            mapCircleCoordinates.put(key, waypoint);
                        }
                    }
                }

                routeIndex++;
            }
        }

        // final settings for mapviewer
        if (routes != null && !routes.isEmpty() && routes.get(0) != null
                && routes.get(0).getCoordinates() != null && !routes.get(0).getCoordinates().isEmpty()) {
            ICoordinate first = routes.get(0).getCoordinates().get(0);
            setDisplayPosition((int) first.getLat(), (int) first.getLon(), zoomLevel);
        }

        setZoom(zoomLevel);
    }

    /**
     * Configure offline tiles if enabled and available
     */
    private void configureOfflineTiles() {
        try {
            if (GeoMapStorage.librarySetting != null &&
                    GeoMapStorage.librarySetting.isUseOfflineTiles() &&
                    GeoMapStorage.librarySetting.getOfflineTilesPath() != null) {

                String tilesPath = GeoMapStorage.librarySetting.getOfflineTilesPath();
                OfflineTileSource offlineSource = new OfflineTileSource(tilesPath, "Offline Maps");

                if (offlineSource.isAvailable()) {
                    setTileSource(offlineSource);
                    log.info("Using offline tiles from: " + tilesPath);
                } else {
                    log.warn("Offline tiles path not available, using default online tiles: " + tilesPath);
                }
            }
        } catch (Exception e) {
            log.error("Error configuring offline tiles, falling back to online tiles", e);
        }
    }

    public void setMapStyle(MapStyle style) {
        if (style == null) style = MapStyle.CITY;
        currentStyle = style;

        // Offline tiles win
        if (GeoMapStorage.librarySetting != null
                && GeoMapStorage.librarySetting.isUseOfflineTiles()
                && GeoMapStorage.librarySetting.getOfflineTilesPath() != null) {
            configureOfflineTiles();
            repaint();
            return;
        }

        switch (style) {
            case CITY:
                setTileSource(new OsmTileSource.Mapnik());
                break;

            case NATURE:
                setTileSource(new HumanitarianTileSource());
                break;

            case CYCLE:
                setTileSource(new OsmTileSource.CycleMap());
                break;

            default:
                setTileSource(new OsmTileSource.Mapnik());
                break;
        }

        repaint();
    }

    private void installNatureToggle() {
        setLayout(null);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 3));
        panel.setOpaque(true);
        panel.setBackground(new Color(255, 255, 255, 220));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 60)));

        JRadioButton cityRb = new JRadioButton("City", currentStyle == MapStyle.CITY);
        JRadioButton natureRb = new JRadioButton("Nature", currentStyle == MapStyle.NATURE);
        JRadioButton cycleRb = new JRadioButton("Cycle", currentStyle == MapStyle.CYCLE);

        ButtonGroup group = new ButtonGroup();
        group.add(cityRb);
        group.add(natureRb);
        group.add(cycleRb);

        cityRb.addActionListener(e -> setMapStyle(MapStyle.CITY));
        natureRb.addActionListener(e -> setMapStyle(MapStyle.NATURE));
        cycleRb.addActionListener(e -> setMapStyle(MapStyle.CYCLE));

        panel.add(cityRb);
        panel.add(natureRb);
        panel.add(cycleRb);

        panel.setSize(panel.getPreferredSize());
        panel.setLocation(Math.max(0, getWidth() - panel.getWidth() - 10), 10);

        add(panel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                panel.setSize(panel.getPreferredSize());
                panel.setLocation(Math.max(0, getWidth() - panel.getWidth() - 10), 10);
            }
        });
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent me) {
        ICoordinate position = getPosition(me.getPoint());

        WaypointSegment waypoint = this.mapCircleCoordinates.get(position.getLat() + "-" + position.getLon());
        if (waypoint == null) {
            double distance = 1111111111;
            for (Map.Entry<String, WaypointSegment> entry : mapCircleCoordinates.entrySet()) {
                double lat = Math.abs(position.getLat() - Double.parseDouble(entry.getKey().split("-")[0]));
                double lon = Math.abs(position.getLon() - Double.parseDouble(entry.getKey().split("-")[1]));
                double tot = lat + lon;
                if (tot < distance) {
                    distance = tot;
                    waypoint = entry.getValue();
                }
            }
        }

        if (waypoint == null) return;

        log.debug("found marker:" + waypoint.getUnit());

        if (lastOpenedMarker != null)
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

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastOpenedMarker != null) {
            removeMapMarker(lastOpenedMarker);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (lastOpenedMarker != null)
            removeMapMarker(lastOpenedMarker);
    }

    public void setDisplayPositionByLatLon(double lat, double lon, int zoom) {
        int latE7 = (int) Math.round(lat * 1e7);
        int lonE7 = (int) Math.round(lon * 1e7);

        if (Math.abs(latE7) > 90 && Math.abs(lonE7) > 180) {
            setDisplayPosition(latE7, lonE7, zoom);
        } else {
            setDisplayPosition((int) Math.round(lat), (int) Math.round(lon), zoom);
        }

        setZoom(zoom);
    }
}