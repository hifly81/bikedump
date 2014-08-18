package org.hifly.geomapviewer.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.geomapviewer.controller.GPSController;
import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.LibrarySetting;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.Coordinate;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.graph.WaypointAvgSpeedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGainedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGraph;
import org.hifly.geomapviewer.graph.WaypointTimeGraph;
import org.hifly.geomapviewer.gui.dialog.GraphViewer;
import org.hifly.geomapviewer.gui.dialog.ScrollableDialog;
import org.hifly.geomapviewer.gui.dialog.Setting;
import org.hifly.geomapviewer.gui.dialog.TipOfTheDay;
import org.hifly.geomapviewer.gui.events.PanelWindowAdapter;
import org.hifly.geomapviewer.gui.events.TableSelectionHandler;
import org.hifly.geomapviewer.gui.panel.*;
import org.hifly.geomapviewer.gui.menu.GeoFileChooser;
import org.hifly.geomapviewer.gui.menu.GeoFolderChooser;
import org.hifly.geomapviewer.gui.menu.GeoMapMenu;
import org.hifly.geomapviewer.gui.menu.GeoToolbar;
import org.hifly.geomapviewer.report.PdfReport;
import org.hifly.geomapviewer.storage.DataHolder;
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
    private Setting settingDialog = null;
    private GeoMapViewer currentFrame = this;
    protected MapViewer mapViewer;
    private JSplitPane mainPanel = new JSplitPane();
    private JLabel zoomLabel, zoomValue, measureLabel, measureValue;
    private Map.Entry<Integer, Integer> dimension;
    private JScrollPane folderMapScrollViewer, folderDetailViewer, folderTableViewer;
    public TrackTable trackTable = null;
    private String textForReport;

    public GeoMapViewer() {
        super();
        //panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey(), dimension.getValue());
        //layout
        setLayout(new BorderLayout());
        //exit behaviour
        addWindowListener(new PanelWindowAdapter());

        //saved pref
        List<Bike> bikes = GeoMapStorage.savedBikesList;
        if (bikes != null && !bikes.isEmpty()) {
            profileSetting.setBikes(bikes);
        }


        //define dialogs
        settingDialog = new Setting(currentFrame, profileSetting);
        settingDialog.pack();

        //create toolbar
        GeoToolbar toolBar = new GeoToolbar(currentFrame);
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
                    if (DataHolder.listsWaypointSegment == null) {
                        DataHolder.listsWaypointSegment = new ArrayList();
                    }
                    List<Track> tracks = new ArrayList();
                    StringBuffer sb = new StringBuffer();
                    for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                        File file = (File) iterator.next();
                        addTrackToCache(coordinates, waypoint, tracks, file, sb);
                    }
                    if (sb.length() > 0) {
                        ScrollableDialog dialog = new ScrollableDialog(null, sb.toString(), dimension.getKey() / 4, dimension.getValue() / 4);
                        dialog.showMessage();
                    }

                    //add dir to library
                    if (GeoMapStorage.librarySetting == null) {
                        GeoMapStorage.librarySetting = new LibrarySetting();
                    }
                    if (GeoMapStorage.librarySetting.getScannedDirs() == null) {
                        GeoMapStorage.librarySetting.setScannedDirs(new ArrayList<String>());
                    }
                    boolean foundDir = false;
                    for (String dir : GeoMapStorage.librarySetting.getScannedDirs()) {
                        if (dir.equalsIgnoreCase(directory.getAbsolutePath())) {
                            foundDir = true;
                            break;
                        }
                    }
                    if (!foundDir) {
                        GeoMapStorage.librarySetting.getScannedDirs().add(directory.getAbsolutePath());
                    }

                    JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                    JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                    JScrollPane tableViewer = createTableTracksViewer(tracks);

                    repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                    folderMapScrollViewer = mapScrollViewer;
                    folderDetailViewer = detailViewer;
                    folderTableViewer = tableViewer;

                    DataHolder.tracksLoaded = tracks;
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
                long time1 = System.currentTimeMillis();
                add(label, BorderLayout.CENTER);
                label.setVisible(true);
                //load saved elements
                if (GeoMapStorage.tracksLibrary != null) {
                    List<List<Coordinate>> coordinates = new ArrayList();
                    List<Map<String, WaypointSegment>> waypoint = new ArrayList();
                    if (DataHolder.listsWaypointSegment == null) {
                        DataHolder.listsWaypointSegment = new ArrayList();
                    }
                    List<Track> tracks = new ArrayList();
                    StringBuffer sb = new StringBuffer();
                    for (Map.Entry<String, String> entry : GeoMapStorage.tracksLibrary.entrySet()) {
                        File file = new File(entry.getKey());
                        addTrackToCache(coordinates, waypoint, tracks, file, sb);
                    }

                    long time2 = System.currentTimeMillis();
                    System.out.println("Loader tracks Duration:" + (time2 - time1));

                    //check new file
                    if (GeoMapStorage.librarySetting != null && GeoMapStorage.librarySetting.isScanFolder()) {
                        if (GeoMapStorage.librarySetting.getScannedDirs() != null && !GeoMapStorage.librarySetting.getScannedDirs().isEmpty()) {
                            for (String directory : GeoMapStorage.librarySetting.getScannedDirs()) {
                                Collection files = FileUtils.listFiles(new File(directory), null, true);
                                for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                                    File file = (File) iterator.next();
                                    if (GeoMapStorage.tracksLibrary.get(file.getAbsolutePath()) == null) {
                                        addTrackToCache(coordinates, waypoint, tracks, file, sb);
                                    }
                                }
                            }
                        }
                    }

                    if (sb.length() > 0) {
                        ScrollableDialog dialog = new ScrollableDialog(null, sb.toString(), dimension.getKey() / 2, dimension.getValue() / 2);
                        dialog.showMessage();
                    }


                    JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                    JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                    JScrollPane tableViewer = createTableTracksViewer(tracks);

                    repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                    folderMapScrollViewer = mapScrollViewer;
                    folderDetailViewer = detailViewer;
                    folderTableViewer = tableViewer;

                    DataHolder.tracksLoaded = tracks;

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

    public void repaintPanels(
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

    public void loadSelectedTracks(TrackTable table) {
        if (!DataHolder.tracksSelected.isEmpty()) {
            if (!DataHolder.tracksLoaded.isEmpty()) {
                List<Track> selectedTracks = new ArrayList();
                //TODO change algo
                for (Track track : DataHolder.tracksLoaded) {
                    if (DataHolder.tracksSelected.contains(track.getName())) {
                        selectedTracks.add(track);
                    }
                }

                if (!selectedTracks.isEmpty()) {
                    if (selectedTracks.size() == 1) {
                        reloadTrackPanel(new File(selectedTracks.get(0).getFileName()));
                    } else {
                        JScrollPane detailViewer = new AggregateDetailViewer(selectedTracks, currentFrame);
                        JScrollPane tableViewer = createTableTracksViewer(selectedTracks);
                        repaintPanels(tableViewer, folderMapScrollViewer, detailViewer);
                    }
                    table.clearSelection();
                    table.transferFocus();
                    DataHolder.tracksSelected.clear();

                }

            }
        }
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

    public JScrollPane getFolderMapScrollViewer() {
        return folderMapScrollViewer;
    }

    public JScrollPane getFolderDetailViewer() {
        return folderDetailViewer;
    }

    public JScrollPane getFolderTableViewer() {
        return folderTableViewer;
    }

    public String getTextForReport() {
        return textForReport;
    }

    public static void main(String[] args) throws Exception {
        GeoMapViewer viewer = new GeoMapViewer();
        viewer.setVisible(true);
    }

    private void updateZoomParameters() {
        if (measureValue != null)
            measureValue.setText(String.format("%s", mapViewer.getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", mapViewer.getZoom()));
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
            if (resultList.size() > 2) {
                mapViewer = new MapViewer(null, null, 10, resultList.get(0).get(0).getLat(), resultList.get(0).get(0).getLon());
            } else {
                mapViewer = new MapViewer(resultList, waypoints, 20);
            }
        } else {
            mapViewer = new MapViewer(resultList, waypoints, 20);
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

                if (mapViewer.lastOpenedMarker != null) {
                    mapViewer.removeMapMarker(mapViewer.lastOpenedMarker);
                }
            }
        });


        scrollPanel.getViewport().add(panel);
        return scrollPanel;
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
                DataHolder.mapFilePathTrack.put(track.getName(), track.getFileName());
                List<List<Coordinate>> coordinates = new ArrayList();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList();
                List<Track> tracks = new ArrayList(1);
                tracks.add(track);
                coordinates.add(track.getCoordinates());
                waypoint.add(track.getCoordinatesNewKm());
                DataHolder.listsWaypointSegment = new ArrayList(1);
                List<WaypointSegment> listWaypoints = new ArrayList(track.getCoordinatesNewKm().values());
                DataHolder.listsWaypointSegment.add(listWaypoints);

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, false);
                DetailViewer detailViewer = new DetailViewer(track, currentFrame);
                textForReport = detailViewer.getText4Report();
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
            File file,
            StringBuffer sb) {
        if (file.exists()) {
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            Track track;
            Map.Entry<Track, StringBuffer> resultTrack;
            //TODO check not based on extension
            if (ext.equalsIgnoreCase("gpx")) {
                resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
            } else if (ext.equalsIgnoreCase("tcx")) {
                resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
            } else {
                return;
            }
            if (resultTrack != null && resultTrack.getValue().toString().equals("")) {
                track = resultTrack.getKey();
                if (track != null) {
                    //add to map
                    DataHolder.mapFilePathTrack.put(track.getName(), track.getFileName());
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
                    DataHolder.listsWaypointSegment.add(listWaypoints);
                }
            } else {
                sb.append(resultTrack.getValue().toString() + "\n");
            }
        }

    }


    private JScrollPane createTableTracksViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        final TrackTable table = new TrackTable(tracks);
        final TableSelectionHandler listHandler;
        table.getSelectionModel().addListSelectionListener(
                //TODO if track is already selected dont'load again
                //TODO if a list of track is shown, when load the single track don't redraw the table list
                listHandler = new TableSelectionHandler(table, new HashSet()));


        table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadSelectedTracks(table);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        trackTable = table;
        panel.getViewport().add(table);
        return panel;
    }

}
