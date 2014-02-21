package org.hifly.geomapviewer.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.geomapviewer.controller.GPSController;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.Coordinate;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.hifly.geomapviewer.gui.dialog.TipOfTheDay;
import org.hifly.geomapviewer.gui.frame.GraphViewer;
import org.hifly.geomapviewer.gui.menu.GeoFileChooser;
import org.hifly.geomapviewer.gui.menu.GeoFolderChooser;
import org.hifly.geomapviewer.gui.menu.GeoMapMenu;
import org.hifly.geomapviewer.gui.menu.GeoToolbar;
import org.hifly.geomapviewer.gui.tree.DateCatalogTree;
import org.hifly.geomapviewer.gui.tree.EmptyCatalogTree;
import org.hifly.geomapviewer.utility.GUIUtility;
import org.hifly.geomapviewer.utility.TimeUtility;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author
 * @date 07/02/14
 */
public class GeoMapViewer extends JFrame implements JMapViewerEventListener {

    private GeoMapViewer currentFrame = this;
    protected MapViewer mapViewer;
    private JSplitPane mainPanel = new JSplitPane();
    private JLabel zoomLabel, zoomValue, measureLabel, measureValue;
    private Map.Entry<Integer, Integer> dimension;
    private List<List<WaypointKm>> waypointsCalculated;
    private final Map<String,String> trackFileNames = new HashMap();


    public GeoMapViewer() {
        super();
        //frame dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey(), dimension.getValue());
        //layout
        setLayout(new BorderLayout());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //create toolbar
        GeoToolbar toolBar = new GeoToolbar();
        toolBar.getGraphButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                GraphViewer graphViewer = new GraphViewer(currentFrame,waypointsCalculated);
            }
        });
        add(toolBar,BorderLayout.PAGE_START);

        //create menu and its events
        GeoMapMenu mainMenu = new GeoMapMenu();
        final GeoFileChooser fileChooser = new GeoFileChooser();
        final GeoFolderChooser folderChooser = new GeoFolderChooser();

        //import file action
        JMenuItem itemImportFile = mainMenu.getItemImportFile();
        itemImportFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int returnVal = fileChooser.showOpenDialog(GeoMapViewer.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    reloadTrackPanels(file);
                }
            }
        });

        //import folder action
        JMenuItem itemImportFolder = mainMenu.getItemImportFolder();
        itemImportFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int returnVal = folderChooser.showOpenDialog(GeoMapViewer.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File root = folderChooser.getSelectedFile();
                    //TODO refactor this!
                    Collection files = FileUtils.listFiles(root, null, true);
                    List<List<Coordinate>> coordinates = new ArrayList();
                    List<Map<String, WaypointKm>> waypoint = new ArrayList();
                    waypointsCalculated = new ArrayList();
                    List<Track> tracks = new ArrayList();
                    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
                        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
                        //TODO extend to other extensions
                        if (ext.equalsIgnoreCase("gpx")) {
                            Track track = GPSController.extractTrackFromGpx(file.getAbsolutePath());
                            if (track != null) {
                                //add to map
                                trackFileNames.put(track.getName(),track.getFileName());
                                coordinates.add(track.getCoordinates());
                                waypoint.add(track.getCoordinatesNewKm());
                                tracks.add(track);
                                List<WaypointKm> listWaypoints =
                                        new ArrayList(track.getCoordinatesNewKm().values());
                                waypointsCalculated.add(listWaypoints);
                            }


                        }
                    }

                    JScrollPane mapScrollViewer = createMapViewer(coordinates,waypoint);
                    JScrollPane detailViewer = createDetailsViewer(tracks);
                    JScrollPane treeViewer = createTreeTracksViewer(tracks);

                    repaintPanels(treeViewer,mapScrollViewer,detailViewer);

                }
            }
        });


        //add menu
        setJMenuBar(mainMenu);


    }

    private void repaintPanels(
            JScrollPane treeViewer,
            JScrollPane mapScrollViewer,
            JScrollPane detailViewer) {

        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treeViewer,mapScrollViewer);
        split1.setDividerLocation(dimension.getKey()/6);
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,detailViewer,null);
        JSplitPane split3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,split1,split2);
        split3.setDividerLocation(dimension.getKey()-350);


        split1.setOneTouchExpandable(true);
        split2.setOneTouchExpandable(true);
        split3.setOneTouchExpandable(true);

        remove(mainPanel);
        add(split3,BorderLayout.CENTER);

        validate();
        repaint();

        mainPanel = split3;
    }

    private void reloadTrackPanels(File file) {
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        //TODO extend to other extensions
        if (ext.equalsIgnoreCase("gpx")) {
            Track track = GPSController.extractTrackFromGpx(file.getAbsolutePath());
            if (track != null) {
                //add to map
                trackFileNames.put(track.getName(),track.getFileName());
                List<List<Coordinate>> coordinates = new ArrayList();
                List<Map<String, WaypointKm>> waypoint = new ArrayList();
                List<Track> tracks = new ArrayList(1);
                tracks.add(track);
                coordinates.add(track.getCoordinates());
                waypoint.add(track.getCoordinatesNewKm());
                waypointsCalculated  = new ArrayList(1);
                List<WaypointKm> listWaypoints = new ArrayList(track.getCoordinatesNewKm().values());
                waypointsCalculated.add(listWaypoints);

                JScrollPane mapScrollViewer = createMapViewer(coordinates,waypoint);
                JScrollPane detailViewer = createDetailsViewer(track);
                JScrollPane treeViewer = createTreeTracksViewer(tracks);

                repaintPanels(treeViewer,mapScrollViewer,detailViewer);

            }

        }

    }

    private JScrollPane createTreeTracksViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel treeViewer = new JPanel();
        List<String> trackNames = new ArrayList(tracks.size());
        for(Track track:tracks) {
            trackNames.add(track.getName());
        }
        final JTree treeEmptyTracks = new JTree(
                EmptyCatalogTree.createNodes(trackNames));
        final JTree treeDateTracks = new JTree(
                DateCatalogTree.createNodes(tracks));

        //listener to tree nodes
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = treeEmptyTracks.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = treeEmptyTracks.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        //TODO single click
                    }
                    else if(e.getClickCount() == 2) {
                        String fileKey = selPath.getLastPathComponent().toString();
                        reloadTrackPanels(new File(trackFileNames.get(fileKey)));
                    }
                }
            }
        };
        treeEmptyTracks.addMouseListener(ml);

        treeViewer.add(treeEmptyTracks);
        treeViewer.add(treeDateTracks);

        panel.getViewport().add(treeViewer);
        return panel;
    }

    private JScrollPane createDetailsViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");

        double totalDistance = 0;
        double totalCalories = 0;
        double totalDeviceElevation = 0;
        double totalRealElevation = 0;
        double totalDeviceDescent = 0;
        double totalRealDescent = 0;
        double totalCalculatedSpeed = 0;
        double totalEffectiveSpeed = 0;
        long totalRealTime = 0;
        long totalEffectiveTime = 0;

        String text = "<p><font face=\"arial\" size=\"2\">Number of tracks:"+tracks.size()+"<br>";
        for(Track track:tracks) {
             //TODO report details
            totalDistance+=track.getTotalDistance();
            totalCalories+=track.getCalories();
            totalDeviceElevation+=track.getCalculatedElevation();
            totalRealElevation+=track.getRealElevation();
            totalDeviceDescent+=track.getCalculatedDescent();
            totalRealDescent+=track.getRealDescent();
            totalCalculatedSpeed+=track.getCalculatedAvgSpeed();
            totalEffectiveSpeed+=track.getEffectiveAvgSpeed();
            totalRealTime+=track.getRealTime();
            totalEffectiveTime+=track.getEffectiveTime();
        }
        text+= "Total distance:"+totalDistance+"<br>";
        text+= "Avg distance:"+totalDistance/tracks.size()+"<br>";
        text+= "Total calories:"+totalCalories+"<br>";
        text+= "Avg calories:"+totalCalories/tracks.size()+"<br>";
        text+= "<br><br>";
        text+= "Total duration:"+TimeUtility.toStringFromTimeDiff(totalRealTime)+"<br>";
        text+= "Total effective duration:"+TimeUtility.toStringFromTimeDiff(totalEffectiveTime)+"<br>";
        text+= "Avg duration:"+TimeUtility.toStringFromTimeDiff(totalRealTime/tracks.size())+"<br>";
        text+= "Avg effective duration:"+TimeUtility.toStringFromTimeDiff(totalEffectiveTime/tracks.size())+"<br>";
        text+= "<br><br>";
        text+= "Avg calculated speed:"+totalCalculatedSpeed/tracks.size()+"<br>";
        text+= "Avg effective speed:"+totalEffectiveSpeed/tracks.size()+"<br>";
        text+= "<br><br>";
        text+= "Total device elevation:"+totalDeviceElevation+"<br>";
        text+= "Total real elevation:"+totalRealElevation+"<br>";
        text+= "Total device descent:"+totalDeviceDescent+"<br>";
        text+= "Total real descent:"+totalRealDescent+"<br>";
        text+= "<br><br>";
        text+= "Avg device elevation:"+totalDeviceElevation/tracks.size()+"<br>";
        text+= "Avg real elevation:"+totalRealElevation/tracks.size()+"<br>";
        text+= "Avg device descent:"+totalDeviceDescent/tracks.size()+"<br>";
        text+= "Avg real descent:"+totalRealDescent/tracks.size()+"<br>";
        text+= "</font></p></font></p><hr>";

        textPane.setText(text);
        panel.getViewport().add(textPane);
        return panel;

    }

    private JScrollPane createDetailsViewer(Track track) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");

        String text = "<p><font face=\"arial\" size=\"2\"><b>"+track.getName()+"</b><br>";
        text+= track.getStartDate()+"&nbsp;"+track.getEndDate()+"<br>";
        text+= "Duration:"+TimeUtility.toStringFromTimeDiff(track.getRealTime())+"<br>";
        text+= "Effective duration:"+TimeUtility.toStringFromTimeDiff(track.getEffectiveTime())+"<br>";
        text+= "<br><br>";
        text+= "Distance:"+track.getTotalDistance()+"<br>";
        text+= "Calories:"+track.getCalories()+"<br>";
        text+= "<br><br>";
        text+= "Calculated Speed:"+track.getCalculatedAvgSpeed()+"<br>";
        text+= "Effective Speed:"+track.getEffectiveAvgSpeed()+"<br>";
        text+= "Max Speed:"+track.getMaxSpeed()+"<br>";
        text+= "<br><br>";
        text+= "Device elevation:"+track.getCalculatedElevation()+"<br>";
        text+= "Real elevation:"+track.getRealElevation()+"<br>";
        text+= "Device descent:"+track.getCalculatedDescent()+"<br>";
        text+= "Real descent:"+track.getRealDescent()+"<br>";
        text+= "<br><br>";
        WaypointKm fastest = track.getStatsNewKm().get("Fastest");
        WaypointKm slowest = track.getStatsNewKm().get("Slowest");
        WaypointKm shortest = track.getStatsNewKm().get("Shortest");
        WaypointKm longest = track.getStatsNewKm().get("Longest");
        WaypointKm lessElevated = track.getStatsNewKm().get("Less Elevated");
        WaypointKm mostElevated = track.getStatsNewKm().get("Most Elevated");
        text+= "Fastest Lap:"+fastest.getKm()+" - " + fastest.getAvgSpeed()+"<br>";
        text+= "Slowest Lap:"+slowest.getKm()+" - " + slowest.getAvgSpeed()+"<br>";
        text+= "Shortest Lap:"+shortest.getKm()+" - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement())+"<br>";
        text+= "Longest Lap:"+longest.getKm()+" - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement())+"<br>";
        text+= "Most elevated Lap:"+mostElevated.getKm()+" - " + mostElevated.getEleGained()+"<br>";
        text+= "Less elevated Lap:"+lessElevated.getKm()+" - " + lessElevated.getEleGained()+"<br>";
        text+= "<br><br></font></p><hr>";
        text+= "<p><font face=\"arial\" size=\"2\"><b>Slopes"+"("+track.getSlopes().size()+")</b><br><br>";
        for(SlopeSegment slope:track.getSlopes()) {
            text+= "Distance:"+slope.getDistance()+" km<br>";
            text+= "Start km:"+slope.getStartDistance()+" km<br>";
            text+= "End km:"+slope.getEndDistance()+" km<br>";
            text+= "Duration:"+TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime()-slope.getStartDate().getTime())+"<br>";
            text+= "Elevation:"+slope.getElevation()+" m<br>";
            text+= "Gradient:"+slope.getGradient()+" %<br>";
            text+= "Start elevetion m:"+slope.getStartElevation()+" m<br>";
            text+= "End elevation km:"+slope.getEndElevation()+" m<br>";
            text+= "Avg speed:"+slope.getAvgSpeed()+" km/h<br>";
            text+= "<br><br>";
        }
        text+= "</font></p></font></p><hr>";
        text+= "<p><font face=\"arial\" size=\"2\"><b>Details for km.</b><br><br>";
        int km = 1;
        for (Map.Entry<String, WaypointKm> entry : track.getCoordinatesNewKm().entrySet()) {
            text+= km+")<br>";
            text+= "Time relevation:"+entry.getValue().getTimeSpent()+"<br>";
            text+= "Time lap:"+TimeUtility.toStringFromTimeDiff(entry.getValue().getTimeIncrement())+"<br>";
            text+= "Avg speed:"+entry.getValue().getAvgSpeed()+" km/h<<br>";
            String fontElevation = "<font face=\"arial\" size=\"2\" color=\"red\">";
            String fontDescent = "<font face=\"arial\" size=\"2\" color=\"green\">";
            if(entry.getValue().getEleGained()>0) {
                text+= "Elevation gained:"+fontElevation+entry.getValue().getEleGained()+" m</font><br>";
            }
            else {
                text+= "Elevation gained:"+fontDescent+entry.getValue().getEleGained()+" m</font><br>";
            }
            text+= "<br><br>";
            km++;
        }
        text+= "</font></p></font></p><hr>";

        textPane.setText(text);
        panel.getViewport().add(textPane);
        return panel;
    }

    private JScrollPane createMapViewer(
            List<List<Coordinate>> coordinates,
            List<Map<String, WaypointKm>> waypoints) {
        JScrollPane scrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel = new JPanel();

        List<List<ICoordinate>> resultList = new ArrayList();
        if (coordinates != null && !coordinates.isEmpty()) {
            List<ICoordinate> list = new ArrayList();
            for (List<Coordinate> listCoordinates : coordinates) {
                for (Coordinate coordinate : listCoordinates) {
                    org.openstreetmap.gui.jmapviewer.Coordinate temp =
                            new org.openstreetmap.gui.jmapviewer.Coordinate(
                                    coordinate.getDecimalLatitude(),
                                    coordinate.getDecimalLongitude());
                    list.add(temp);
                }
                resultList.add(list);
            }

        }

        JScrollPane pane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        mapViewer = new MapViewer(resultList, waypoints, 10);
        pane.getViewport().add(mapViewer);
        mapViewer.addJMVListener(this);


        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();

        measureLabel = new JLabel("Meters/Pixels: ");
        measureValue = new JLabel(String.format("%s", mapViewer.getMeterPerPixel()));

        zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", mapViewer.getZoom()));


        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        panel.add(panelBottom, BorderLayout.SOUTH);
        JLabel helpLabel = new JLabel("Use right mouse button to move,left double click or mouse wheel to zoom.");
        JButton button = new JButton("Fit map markers");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mapViewer.setDisplayToFitMapMarkers();
            }
        });
        JComboBox tileSourceSelector = new JComboBox(new TileSource[]{new OsmTileSource.Mapnik(),
                new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource()});
        tileSourceSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                mapViewer.setTileSource((TileSource) e.getItem());
            }
        });
        JComboBox tileLoaderSelector;
        try {
            tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmFileCacheTileLoader(mapViewer),
                    new OsmTileLoader(mapViewer)});
        } catch (IOException e) {
            tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmTileLoader(mapViewer)});
        }
        tileLoaderSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                mapViewer.setTileLoader((TileLoader) e.getItem());
            }
        });
        mapViewer.setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);
        final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(mapViewer.getMapMarkersVisible());
        showMapMarker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapViewer.setMapMarkerVisible(showMapMarker.isSelected());
            }
        });
        panelBottom.add(showMapMarker);

        final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
        showToolTip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapViewer.setToolTipText(null);
            }
        });
        panelBottom.add(showToolTip);

        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        showTileGrid.setSelected(mapViewer.isTileGridVisible());
        showTileGrid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapViewer.setTileGridVisible(showTileGrid.isSelected());
            }
        });
        panelBottom.add(showTileGrid);
        final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
        showZoomControls.setSelected(mapViewer.getZoomContolsVisible());
        showZoomControls.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapViewer.setZoomContolsVisible(showZoomControls.isSelected());
            }
        });
        panelBottom.add(showZoomControls);
        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapViewer.setScrollWrapEnabled(scrollWrapEnabled.isSelected());
            }
        });
        panelBottom.add(scrollWrapEnabled);
        panelBottom.add(button);
        panelBottom.add(helpLabel);

        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(measureLabel);
        panelTop.add(measureValue);

        mapViewer.setTileGridVisible(true);

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mapViewer.getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        mapViewer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = mapViewer.getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    mapViewer.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    mapViewer.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (showToolTip.isSelected()) mapViewer.setToolTipText(mapViewer.getPosition(p).toString());
            }
        });


        scrollPanel.getViewport().add(panel);
        return scrollPanel;


    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

    private void updateZoomParameters() {
        if (measureValue != null)
            measureValue.setText(String.format("%s", mapViewer.getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", mapViewer.getZoom()));
    }

    public static void main(String[] args) throws Exception {


        GeoMapViewer viewer = new GeoMapViewer();
        viewer.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TipOfTheDay tip = new TipOfTheDay();
                tip.setVisible(true);
            }
        });

    }
}
