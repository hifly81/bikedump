package org.hifly.geomapviewer.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.geomapviewer.controller.GPSController;
import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.Coordinate;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.graph.WaypointAvgSpeedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGainedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGraph;
import org.hifly.geomapviewer.graph.WaypointTimeGraph;
import org.hifly.geomapviewer.gui.dialog.GraphViewer;
import org.hifly.geomapviewer.gui.dialog.Setting;
import org.hifly.geomapviewer.gui.dialog.TipOfTheDay;
import org.hifly.geomapviewer.gui.events.PanelWindowAdapter;
import org.hifly.geomapviewer.gui.panel.*;
import org.hifly.geomapviewer.gui.menu.GeoFileChooser;
import org.hifly.geomapviewer.gui.menu.GeoFolderChooser;
import org.hifly.geomapviewer.gui.menu.GeoMapMenu;
import org.hifly.geomapviewer.gui.menu.GeoToolbar;
import org.hifly.geomapviewer.storage.GeoMapStorage;
import org.hifly.geomapviewer.utility.GUIUtility;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
//TODO use resource bundles i18n
public class GeoMapViewer extends JFrame implements JMapViewerEventListener {

    private ProfileSetting profileSetting = new ProfileSetting();
    private GeoMapViewer currentFrame = this;
    private Setting settingDialog;
    protected MapViewer mapViewer;
    private JSplitPane mainPanel = new JSplitPane();
    private JLabel zoomLabel, zoomValue, measureLabel, measureValue;
    private Map.Entry<Integer, Integer> dimension;
    private List<List<WaypointSegment>> waypointsCalculated;
    private final Map<String, String> trackFileNames = new HashMap();


    public GeoMapViewer() {
        super();
        //panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey(), dimension.getValue());
        //layout
        setLayout(new BorderLayout());
        //exit behaviour
        addWindowListener(new PanelWindowAdapter());

        List<Bike> bikes = GeoMapStorage.savedBikesList;
        if (bikes != null && !bikes.isEmpty()) {
            profileSetting.setBikes(bikes);
        }

        //define dialogs
        settingDialog = new Setting(currentFrame, profileSetting);
        settingDialog.pack();

        //create toolbar
        GeoToolbar toolBar = new GeoToolbar();
        toolBar.getGraphButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                WaypointElevationGraph waypointElevationGraph = new WaypointElevationGraph(waypointsCalculated);
                WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(waypointsCalculated);
                WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(waypointsCalculated);
                WaypointElevationGainedGraph waypointElevationGainedGraph = new WaypointElevationGainedGraph(waypointsCalculated);
                GraphViewer graphViewer =
                        new GraphViewer(currentFrame,
                                Arrays.asList(waypointElevationGraph, waypointAvgSpeedGraph, waypointTimeGraph, waypointElevationGainedGraph));
            }
        });
        add(toolBar, BorderLayout.PAGE_START);

        //create menu and its events
        GeoMapMenu mainMenu = new GeoMapMenu(currentFrame);
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
                    reloadTrackPanel(file);
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
                    File directory = folderChooser.getSelectedFile();
                    Collection files = FileUtils.listFiles(directory, null, true);
                    List<List<Coordinate>> coordinates = new ArrayList();
                    List<Map<String, WaypointSegment>> waypoint = new ArrayList();
                    if (waypointsCalculated == null) {
                        waypointsCalculated = new ArrayList();
                    }
                    List<Track> tracks = new ArrayList();
                    for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                        File file = (File) iterator.next();
                        addTrackToCache(coordinates, waypoint, tracks, file);
                    }

                    //sort tracks by dates
                    Collections.sort(tracks, new Comparator<Track>() {
                        public int compare(Track o1, Track o2) {
                            if (o1.getStartDate() != null && o2.getStartDate() != null) {
                                return o1.getStartDate().compareTo(o2.getStartDate());
                            }
                            return -1;
                        }
                    });

                    JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                    JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                    JScrollPane tableViewer = createTableTracksViewer(tracks);

                    repaintPanels(tableViewer, mapScrollViewer, detailViewer);
                }
            }
        });

        //profile setting menu item
        JMenuItem itemProfileSetting = mainMenu.getItemOptionsSetting();
        itemProfileSetting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                settingDialog.setLocationRelativeTo(currentFrame);
                settingDialog.setVisible(true);

            }
        });


        //add menu
        setJMenuBar(mainMenu);

        new SwingWorker<Void, String>() {
            final JLabel label = new JLabel("Loading... ", JLabel.CENTER);
            @Override
            protected Void doInBackground() throws Exception {
                add(label,BorderLayout.CENTER);
                label.setVisible(true);
                //load saved elements
                if (GeoMapStorage.tracksLibrary != null) {
                    List<List<Coordinate>> coordinates = new ArrayList();
                    List<Map<String, WaypointSegment>> waypoint = new ArrayList();
                    if (waypointsCalculated == null) {
                        waypointsCalculated = new ArrayList();
                    }
                    List<Track> tracks = new ArrayList();
                    for (Map.Entry<String, String> entry : GeoMapStorage.tracksLibrary.entrySet()) {
                        File file = new File(entry.getKey());
                        addTrackToCache(coordinates, waypoint, tracks, file);
                    }

                    //sort tracks by dates
                    Collections.sort(tracks, new Comparator<Track>() {
                        public int compare(Track o1, Track o2) {
                            if (o1.getStartDate() != null && o2.getStartDate() != null) {
                                return o1.getStartDate().compareTo(o2.getStartDate());
                            }
                            return -1;
                        }
                    });

                    JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                    JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                    JScrollPane tableViewer = createTableTracksViewer(tracks);

                    repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                }
                return null;
            }

            @Override
            protected void done() {
                label.setVisible(false);
                TipOfTheDay tip = new TipOfTheDay();
                tip.setVisible(true);
            }
        }.execute();


    }

    private void repaintPanels(
            JScrollPane treeViewer,
            JScrollPane mapScrollViewer,
            JScrollPane detailViewer) {

        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeViewer, mapScrollViewer);
        split1.setDividerLocation(dimension.getKey() / 6);
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailViewer, null);
        JSplitPane split3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split1, split2);
        split3.setDividerLocation(dimension.getKey() - 350);


        split1.setOneTouchExpandable(true);
        split2.setOneTouchExpandable(true);
        split3.setOneTouchExpandable(true);

        remove(mainPanel);
        add(split3, BorderLayout.CENTER);

        validate();
        repaint();

        mainPanel = split3;
    }

    private void reloadTrackPanel(File file) {
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        Track track = null;
        Map.Entry<Track, StringBuffer> resultTrack = null;
        if (ext.equalsIgnoreCase("gpx")) {
            resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
        } else if (ext.equalsIgnoreCase("tcx")) {
            resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
        }
        if (resultTrack.getValue().toString().equals("")) {
            track = resultTrack.getKey();
            if (track != null) {
                //add to map
                trackFileNames.put(track.getName(), track.getFileName());
                List<List<Coordinate>> coordinates = new ArrayList();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList();
                List<Track> tracks = new ArrayList(1);
                tracks.add(track);
                coordinates.add(track.getCoordinates());
                waypoint.add(track.getCoordinatesNewKm());
                waypointsCalculated = new ArrayList(1);
                List<WaypointSegment> listWaypoints = new ArrayList(track.getCoordinatesNewKm().values());
                waypointsCalculated.add(listWaypoints);

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, false);
                JScrollPane detailViewer = new DetailViewer(track, currentFrame);
                JScrollPane treeViewer = createTableTracksViewer(tracks);

                repaintPanels(treeViewer, mapScrollViewer, detailViewer);

            }
        } else {
            JOptionPane.showMessageDialog(currentFrame,
                    resultTrack.getValue().toString(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }


    private void addTrackToCache(
            List<List<Coordinate>> coordinates,
            List<Map<String, WaypointSegment>> waypoint,
            List<Track> tracks,
            File file) {
        if (file.exists()) {
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            Track track = null;
            Map.Entry<Track, StringBuffer> resultTrack = null;
            if (ext.equalsIgnoreCase("gpx")) {
                resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
            } else if (ext.equalsIgnoreCase("tcx")) {
                resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
            }
            if (resultTrack.getValue().toString().equals("")) {
                track = resultTrack.getKey();
                if (track != null) {
                    //add to map
                    trackFileNames.put(track.getName(), track.getFileName());
                    //add to opened files map
                    if (GeoMapStorage.tracksLibrary == null) {
                        GeoMapStorage.tracksLibrary = new HashMap();
                    }
                    GeoMapStorage.tracksLibrary.put(track.getFileName(), track.getFileName());
                    coordinates.add(track.getCoordinates());
                    waypoint.add(track.getCoordinatesNewKm());
                    tracks.add(track);
                    List<WaypointSegment> listWaypoints =
                            new ArrayList(track.getCoordinatesNewKm().values());
                    waypointsCalculated.add(listWaypoints);
                }
            } else {
                JOptionPane.showMessageDialog(currentFrame,
                        resultTrack.getValue().toString(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JScrollPane createTableTracksViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        final TrackTable table = new TrackTable(tracks);
        table.getSelectionModel().addListSelectionListener(
                //TODO if track is already selected dont'load again
                //TODO if a list of track is shown, when load the single track don't redraw the table list
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        if (event.getValueIsAdjusting()) {
                            return;
                        }
                        List<String> trackNamesTemp = new ArrayList(table.getSelectedRowCount());
                        for (int c : table.getSelectedRows()) {
                            Object fileKey = table.getValueAt(c, 1);
                            if (fileKey != null) {
                                trackNamesTemp.add(fileKey.toString());
                            }

                        }
                        //TODO reload more than one file
                        if (trackNamesTemp.size() == 1) {
                            reloadTrackPanel(new File(trackFileNames.get(trackNamesTemp.get(0))));
                        }
                    }
                });


        panel.getViewport().add(table);
        return panel;
    }

    private JScrollPane createMapViewer(
            List<List<Coordinate>> coordinates,
            List<Map<String, WaypointSegment>> waypoints,
            boolean multiple) {
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
        if (multiple) {
            mapViewer = new MapViewer(null, null, 10);
        } else {
            mapViewer = new MapViewer(resultList, waypoints, 10);
        }
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

    }
}
