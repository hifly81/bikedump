package org.hifly.bikedump.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.bikedump.controller.GPSController;
import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.dialog.Scrollable;
import org.hifly.bikedump.gui.dialog.Settings;
import org.hifly.bikedump.gui.dialog.TipOfTheDay;
import org.hifly.bikedump.gui.events.QuitWindowHandler;
import org.hifly.bikedump.gui.events.TableSelectionHandler;
import org.hifly.bikedump.gui.menu.FileChooser;
import org.hifly.bikedump.gui.menu.FolderChooser;
import org.hifly.bikedump.gui.menu.TopMenu;
import org.hifly.bikedump.gui.menu.Toolbar;
import org.hifly.bikedump.gui.panel.*;
import org.hifly.bikedump.gui.table.TrackColorRowRenderer;
import org.hifly.bikedump.gui.theme.ThemeManager;
import org.hifly.bikedump.gui.theme.ThemePreference;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.task.LoadTrackExecutor;
import org.hifly.bikedump.task.NewTrackTimer;
import org.hifly.bikedump.utility.GUIUtility;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

//TODO use resource bundles i18n
public class Bikedump extends JFrame implements JMapViewerEventListener {

    private static final long serialVersionUID = 10L;

    private final Logger log = LoggerFactory.getLogger(Bikedump.class);

    private Settings settingDialog = null;
    private final Bikedump currentFrame = this;
    protected MapViewer mapViewer;
    private JSplitPane mainPanel = new JSplitPane();
    private JScrollPane homeAggregateDetailViewer;

    private final Map.Entry<Integer, Integer> dimension;
    private JScrollPane folderMapScrollViewer, folderDetailViewer, folderTableViewer;
    private String textForReport;
    private static final String TITLE = "Bikedump";

    public TrackTable trackTable = null;
    public TopMenu topMenu = null;

    public Bikedump() {
        super();

        //fix a user-agent
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");

        //panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey(), dimension.getValue());
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setTitle(TITLE);
        setName(TITLE);
        //layout
        setLayout(new BorderLayout());
        //exit behaviour
        addWindowListener(new QuitWindowHandler());

        //settings
        initSettings();

        //dialogs
        initDialogs();

        //create toolbar
        Toolbar toolBar = new Toolbar(currentFrame);
        add(toolBar, BorderLayout.PAGE_START);

        //create menu and its events
        topMenu = new TopMenu(currentFrame);
        wireThemeMenu();

        final FileChooser fileChooser = new FileChooser();
        final FolderChooser folderChooser = new FolderChooser();

        //import file action
        JMenuItem itemImportFile = topMenu.getItemImportFile();
        itemImportFile.addActionListener(event -> {
            if (fileChooser.showOpenDialog(Bikedump.this) == JFileChooser.APPROVE_OPTION)
                reloadTrackFromFile(fileChooser.getSelectedFile());
            mapViewer.setDisplayToFitMapMarkers();
        });

        //import folder action
        JMenuItem itemImportFolder = topMenu.getItemImportFolder();
        itemImportFolder.addActionListener(event -> {
            if (folderChooser.showOpenDialog(Bikedump.this) == JFileChooser.APPROVE_OPTION) {
                File directory = folderChooser.getSelectedFile();
                List<List<Coordinate>> coordinates = new ArrayList<>();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList<>();
                if (DataHolder.listsWaypointSegment == null)
                    DataHolder.listsWaypointSegment = new ArrayList<>();
                List<Track> tracks = new ArrayList<>();
                StringBuffer sb = new StringBuffer();

                LoadTrackExecutor loadTrackExecutor = new LoadTrackExecutor(
                        false,
                        FileUtils.listFiles(folderChooser.getSelectedFile(), null, true).iterator(),
                        sb,
                        coordinates,
                        waypoint,
                        tracks);
                try {
                    loadTrackExecutor.execute();
                } catch (Exception e) {
                    log.warn("Can't import from directory", e);
                }

                if (!sb.isEmpty())
                    new Scrollable(null, sb.toString(), dimension.getKey() / 4, dimension.getValue() / 4).showMessage();

                //add dir to library
                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (GeoMapStorage.librarySetting.getScannedDirs() == null)
                    GeoMapStorage.librarySetting.setScannedDirs(new ArrayList<>());
                boolean foundDir = false;
                for (String dir : GeoMapStorage.librarySetting.getScannedDirs()) {
                    if (dir.equalsIgnoreCase(directory.getAbsolutePath())) {
                        foundDir = true;
                        break;
                    }
                }
                if (!foundDir)
                    GeoMapStorage.librarySetting.getScannedDirs().add(directory.getAbsolutePath());

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                homeAggregateDetailViewer = detailViewer;
                JScrollPane tableViewer = createTableTracksViewer(tracks);

                repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                folderMapScrollViewer = mapScrollViewer;
                folderDetailViewer = detailViewer;
                folderTableViewer = tableViewer;

                DataHolder.tracksLoaded = tracks;
            }
        });

        //try sample action
        JMenuItem itemTrySample = topMenu.getItemTrySample();
        itemTrySample.addActionListener(event -> {
            File file;
            try {
                InputStream inputStream = Bikedump.class.getClassLoader().getResourceAsStream("samples/vfassa.gpx");

                if (inputStream == null)
                    log.warn("Can't load sample gpx");

                Path tempFile = Files.createTempFile("vfassa", ".gpx");
                assert inputStream != null;
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                file = tempFile.toFile();
                reloadTrackFromFile(file);
                if (mapViewer != null) {
                    mapViewer.setDisplayToFitMapMarkers();
                }
            } catch (Exception e) {
                log.warn("Can't load sample gpx", e);
            }
        });

        //profile setting menu item
        JMenuItem itemProfileSetting = topMenu.getItemOptionsSetting();
        itemProfileSetting.addActionListener(event -> {
            settingDialog.setLocationRelativeTo(currentFrame);
            settingDialog.setVisible(true);
        });

        //add menu
        setJMenuBar(topMenu);
        topMenu.selectThemeRadio(ThemeManager.getThemePreference());

        //loading GUI and track saved
        new SwingWorker<Void, String>() {
            final JLabel label = new JLabel("Loading... ", JLabel.CENTER);

            @Override
            protected Void doInBackground() throws Exception {
                add(label, BorderLayout.CENTER);
                label.setVisible(true);

                List<Track> tracks = new ArrayList<>();
                List<List<Coordinate>> coordinates = new ArrayList<>();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList<>();

                //load saved elements
                if (GeoMapStorage.tracksLibrary != null) {
                    if (DataHolder.listsWaypointSegment == null)
                        DataHolder.listsWaypointSegment = new ArrayList<>();
                    StringBuffer sb = new StringBuffer();

                    LoadTrackExecutor loadTrackExecutor = new LoadTrackExecutor(
                            false,
                            GeoMapStorage.tracksLibrary.entrySet().iterator(),
                            sb,
                            coordinates,
                            waypoint,
                            tracks);
                    loadTrackExecutor.execute();

                    //TODO check for new files at regular interval --> TimerTask
                    checkNewTrack(coordinates, waypoint, tracks, sb);
                }

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
                homeAggregateDetailViewer = detailViewer;
                JScrollPane tableViewer = createTableTracksViewer(tracks);

                repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                folderMapScrollViewer = mapScrollViewer;
                folderDetailViewer = detailViewer;
                folderTableViewer = tableViewer;

                DataHolder.tracksLoaded = tracks;

                return null;
            }

            @Override
            protected void done() {
                label.setVisible(false);
                new TipOfTheDay().setVisible(true);
                new NewTrackTimer(currentFrame);
            }
        }.execute();
    }

    public void repaintPanels(
            JScrollPane tableViewer,
            JScrollPane mapScrollViewer,
            JScrollPane detailViewer) {

        // Hide horizontal scrollbars (table + detail). Map can keep both.
        if (tableViewer != null) tableViewer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        if (detailViewer != null) detailViewer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Left column (vertical): table over detail
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableViewer, detailViewer);
        leftSplit.setOneTouchExpandable(true);
        leftSplit.setContinuousLayout(true);
        leftSplit.setResizeWeight(0.65);
        leftSplit.setBorder(null);

        // Main (horizontal): left column | map
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, mapScrollViewer);
        splitMain.setOneTouchExpandable(true);
        splitMain.setContinuousLayout(true);
        splitMain.setResizeWeight(0.30); // keeps 30% on the left when resizing
        splitMain.setBorder(null);

        remove(mainPanel);
        add(splitMain, BorderLayout.CENTER);

        validate();
        repaint();

        mainPanel = splitMain;

        // Set divider positions AFTER layout is realized (percentages, stable)
        SwingUtilities.invokeLater(() -> {
            try {
                // 30% left column
                splitMain.setDividerLocation(0.30);
                // 60% table, 40% detail (tweak as you like)
                leftSplit.setDividerLocation(0.60);

                if (currentFrame.trackTable != null) {
                    currentFrame.trackTable.clearSelection();
                    currentFrame.trackTable.requestFocusInWindow();
                }
            } catch (Exception ignored) {
            }
        });

    }

    public void loadSelectedTracks(TrackTable table) {
        if (!DataHolder.tracksSelected.isEmpty()) {
            if (DataHolder.tracksLoaded != null && !DataHolder.tracksLoaded.isEmpty()) {
                List<Track> selectedTracks = new ArrayList<>();
                for (TrackSelected trackSelected : DataHolder.tracksSelected) {
                    int index = DataHolder.tracksLoaded.indexOf(new Track(trackSelected.getFilename()));
                    if (index != -1) {
                        Track track = DataHolder.tracksLoaded.get(index);
                        if (track != null)
                            selectedTracks.add(track);
                    }
                }

                if (!selectedTracks.isEmpty()) {
                    if (selectedTracks.size() == 1) {
                        reloadTrackFromFile(new File(selectedTracks.get(0).getFileName()));
                        clearTrackRowColors(trackTable);
                        if (mapViewer != null) {
                            mapViewer.setDisplayToFitMapMarkers();
                        }
                    } else {
                        List<Track> tracksToLoad = new ArrayList<>(selectedTracks);

                        // details (already correct)
                        JScrollPane detailViewer = new AggregateDetailViewer(tracksToLoad, currentFrame);
                        homeAggregateDetailViewer = detailViewer;

                        // table (already correct)
                        JScrollPane tableViewer = createTableTracksViewer(tracksToLoad);
                        // apply row colors ONLY for multi-track view
                        applyMultiTrackRowColors(trackTable, tracksToLoad);

                        // rebuild map for selected tracks
                        List<List<Coordinate>> coordinates = new ArrayList<>();
                        List<Map<String, WaypointSegment>> waypoints = new ArrayList<>();

                        for (Track t : tracksToLoad) {
                            if (t != null && t.getCoordinates() != null) coordinates.add(t.getCoordinates());
                            if (t != null && t.getCoordinatesNewKm() != null) waypoints.add(t.getCoordinatesNewKm());
                        }

                        JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoints, true);
                        folderMapScrollViewer = mapScrollViewer;

                        // repaint UI
                        repaintPanels(tableViewer, mapScrollViewer, detailViewer);


                    }

                    SwingUtilities.invokeLater(() -> {
                        try {
                            table.requestFocusInWindow();
                        } catch (Exception ignored) {}
                    });
                    DataHolder.tracksSelected.clear();
                }
            }
        }
    }

    public void checkNewTrack(List<List<Coordinate>> coordinates, List<Map<String, WaypointSegment>> waypoint, List<Track> tracks, StringBuffer sb) throws Exception {
        if (GeoMapStorage.librarySetting != null && GeoMapStorage.librarySetting.isScanFolder()) {
            if (GeoMapStorage.librarySetting.getScannedDirs() != null && !GeoMapStorage.librarySetting.getScannedDirs().isEmpty()) {
                for (String directory : GeoMapStorage.librarySetting.getScannedDirs()) {
                    //only files not in cache
                    LoadTrackExecutor loadTrackExecutor = new LoadTrackExecutor(
                            true,
                            FileUtils.listFiles(new File(directory), null, true).iterator(),
                            sb,
                            coordinates,
                            waypoint,
                            tracks);
                    loadTrackExecutor.execute();
                }
            }
        }

        //List of loader results
        if (!sb.isEmpty())
            new Scrollable(null, sb.toString(), dimension.getKey() / 2, dimension.getValue() / 2).showMessage();
    }

    public JScrollPane getHomeAggregateDetailViewer() {
        return homeAggregateDetailViewer;
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            //FIXME new zoom value
        }
    }

    private void initSettings() {}

    private void initDialogs() {
        //define dialogs
        settingDialog = new Settings(currentFrame);
        settingDialog.pack();
    }

    private JScrollPane createMapViewer(
            List<List<Coordinate>> coordinates,
            List<Map<String, WaypointSegment>> waypoints,
            boolean multiple) {
        JScrollPane scrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel = new JPanel();

        List<List<ICoordinate>> resultList = new ArrayList<>();
        if (coordinates != null && !coordinates.isEmpty()) {
            for (List<Coordinate> listCoordinates : coordinates) {
                List<ICoordinate> list = new ArrayList<>(); // IMPORTANT: new list per track
                if (listCoordinates != null) {
                    for (Coordinate coordinate : listCoordinates) {
                        org.openstreetmap.gui.jmapviewer.Coordinate temp =
                                new org.openstreetmap.gui.jmapviewer.Coordinate(
                                        coordinate.getDecimalLatitude(),
                                        coordinate.getDecimalLongitude());
                        list.add(temp);
                    }
                }
                if (!list.isEmpty()) {
                    resultList.add(list);
                }
            }
        }

        JScrollPane pane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        if (multiple) {
            List<RouteOverlay> routes = toRoutes(resultList, waypoints);
            mapViewer = new MapViewer(routes, 20);

            // Center map on the densest cluster of points (dominant area)
            try {
                Map.Entry<Double, Double> center = computeDominantCenter(resultList);
                if (center != null) {
                    mapViewer.setDisplayPositionByLatLon(center.getKey(), center.getValue(), 7);
                }
            } catch (Exception ignored) {
            }

            SwingUtilities.invokeLater(() -> {
                try {
                    mapViewer.setDisplayToFitMapMarkers();
                } catch (Exception ignored) {
                }
            });

        } else {
            List<RouteOverlay> routes = toRoutes(resultList, waypoints);
            mapViewer = new MapViewer(routes, 20);
        }
        pane.getViewport().add(mapViewer);
        mapViewer.addJMVListener(this);

        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JLabel helpLabel = new JLabel("Use right mouse button to move,left double click or mouse wheel to zoom.");

        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        panel.add(panelBottom, BorderLayout.SOUTH);
        JButton button = new JButton("Fit map markers");
        button.addActionListener(e -> mapViewer.setDisplayToFitMapMarkers());

        final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(mapViewer.getMapMarkersVisible());
        showMapMarker.addActionListener(e -> mapViewer.setMapMarkerVisible(showMapMarker.isSelected()));
        panelBottom.add(showMapMarker);

        final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
        showToolTip.addActionListener(e -> mapViewer.setToolTipText(null));
        panelBottom.add(showToolTip);

        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        showTileGrid.setSelected(mapViewer.isTileGridVisible());
        showTileGrid.addActionListener(e -> mapViewer.setTileGridVisible(showTileGrid.isSelected()));
        panelBottom.add(showTileGrid);

        final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
        showZoomControls.addActionListener(e -> mapViewer.setZoomContolsVisible(showZoomControls.isSelected()));
        panelBottom.add(showZoomControls);

        panelBottom.add(button);
        panelBottom.add(helpLabel);

        panelTop.add(helpLabel);

        mapViewer.setTileGridVisible(true);

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1)
                    mapViewer.getAttribution().handleAttribution(e.getPoint(), true);
            }
        });

        mapViewer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = mapViewer.getAttribution().handleAttributionCursor(p);
                if (cursorHand)
                    mapViewer.setCursor(new Cursor(Cursor.HAND_CURSOR));
                else
                    mapViewer.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (showToolTip.isSelected()) mapViewer.setToolTipText(mapViewer.getPosition(p).toString());

                if (mapViewer.lastOpenedMarker != null)
                    mapViewer.removeMapMarker(mapViewer.lastOpenedMarker);
            }
        });

        scrollPanel.getViewport().add(panel);
        return scrollPanel;
    }

    private static class BucketAgg {
        int count = 0;
        double sumLat = 0;
        double sumLon = 0;

        void add(double lat, double lon) {
            count++;
            sumLat += lat;
            sumLon += lon;
        }

        Map.Entry<Double, Double> center() {
            return new java.util.AbstractMap.SimpleEntry<>(sumLat / count, sumLon / count);
        }
    }

    private Map.Entry<Double, Double> computeDominantCenter(List<List<ICoordinate>> resultList) {
        if (resultList == null || resultList.isEmpty()) return null;

        final double cell = 1.0; // degrees

        java.util.Map<String, BucketAgg> buckets = new java.util.HashMap<>();

        for (List<ICoordinate> trackPts : resultList) {
            if (trackPts == null || trackPts.isEmpty()) continue;

            ICoordinate first = trackPts.get(0);
            ICoordinate last = trackPts.get(trackPts.size() - 1);

            addBucketPoint(buckets, first.getLat(), first.getLon(), cell);
            addBucketPoint(buckets, last.getLat(), last.getLon(), cell);
        }

        BucketAgg best = null;
        for (BucketAgg agg : buckets.values()) {
            if (best == null || agg.count > best.count) best = agg;
        }

        return (best != null && best.count > 0) ? best.center() : null;
    }

    private void addBucketPoint(java.util.Map<String, BucketAgg> buckets, double lat, double lon, double cell) {
        int latKey = (int) Math.floor(lat / cell);
        int lonKey = (int) Math.floor(lon / cell);
        String key = latKey + ":" + lonKey;

        BucketAgg agg = buckets.get(key);
        if (agg == null) {
            agg = new BucketAgg();
            buckets.put(key, agg);
        }
        agg.add(lat, lon);
    }

    private void reloadTrackFromFile(File file) {
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        Map.Entry<Track, StringBuffer> resultTrack;
        if (ext.equalsIgnoreCase("gpx"))
            resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath());
        else if (ext.equalsIgnoreCase("tcx"))
            resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath());
        else {
            JOptionPane.showMessageDialog(currentFrame,
                    "Map not available",
                    "Map not available",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        reloadTrack(resultTrack);
    }

    private void reloadTrack(Map.Entry<Track, StringBuffer> resultTrack) {
        Track track;
        if (resultTrack.getValue().toString().equals("")) {
            track = resultTrack.getKey();
            if (track != null) {
                //add to map
                DataHolder.mapFilePathTrack.put(track.getName(), track.getFileName());
                List<List<Coordinate>> coordinates = new ArrayList<>();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList<>();
                List<Track> tracks = new ArrayList<>(1);
                tracks.add(track);
                coordinates.add(track.getCoordinates());
                waypoint.add(track.getCoordinatesNewKm());
                DataHolder.listsWaypointSegment = new ArrayList<>(1);
                List<WaypointSegment> listWaypoints = new ArrayList<>(track.getCoordinatesNewKm().values());
                DataHolder.listsWaypointSegment.add(listWaypoints);

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, false);
                DetailViewer detailViewer = new DetailViewer(track, currentFrame);
                textForReport = detailViewer.getText4Report();

                JScrollPane tableViewerToUse = (folderTableViewer != null) ? folderTableViewer : createTableTracksViewer(tracks);

                repaintPanels(tableViewerToUse, mapScrollViewer, detailViewer);

                folderMapScrollViewer = mapScrollViewer;
                folderDetailViewer = detailViewer;
                if (folderTableViewer == null) {
                    folderTableViewer = tableViewerToUse;
                }
            }
        } else {
            JOptionPane.showMessageDialog(currentFrame,
                    resultTrack.getValue().toString(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JScrollPane createTableTracksViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        final TrackTable table = new TrackTable(tracks);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // IMPORTANT: do NOT apply row colors here.
        // Colors are only applied in multi-track selection view.
        clearTrackRowColors(table);

        // --- SHIFT-driven multi select state ---
        TableSelectionHandler selectionHandler = new TableSelectionHandler(currentFrame, table, new HashSet<>());
        table.getSelectionModel().addListSelectionListener(selectionHandler);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (!table.isFocusOwner()) return false;

            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    selectionHandler.setShiftDown(true);
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    selectionHandler.setShiftDown(false);

                    selectionHandler.rebuildSelectedTracksFromTable();
                    if (!DataHolder.tracksSelected.isEmpty()) {
                        loadSelectedTracks(table);
                    }
                }
            }
            return false;
        });

        table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectionHandler.setShiftDown(false);
                    selectionHandler.rebuildSelectedTracksFromTable();
                    if (!DataHolder.tracksSelected.isEmpty()) {
                        loadSelectedTracks(table);
                    }
                }
            }
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {}
        });

        trackTable = table;
        panel.getViewport().add(table);
        return panel;
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

    public void refreshLibraryAndReloadUI() {
        new SwingWorker<RefreshResult, Void>() {

            @Override
            protected RefreshResult doInBackground() throws Exception {
                List<Track> tracks = DataHolder.tracksLoaded;
                if (tracks == null) {
                    tracks = new ArrayList<>();
                }

                List<List<Coordinate>> coordinates = new ArrayList<>();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList<>();

                if (DataHolder.listsWaypointSegment == null) {
                    DataHolder.listsWaypointSegment = new ArrayList<>();
                }

                StringBuffer sb = new StringBuffer();

                checkNewTrack(coordinates, waypoint, tracks, sb);

                if (coordinates.isEmpty() && tracks != null && !tracks.isEmpty()) {
                    for (Track t : tracks) {
                        if (t != null && t.getCoordinates() != null) coordinates.add(t.getCoordinates());
                        if (t != null && t.getCoordinatesNewKm() != null) waypoint.add(t.getCoordinatesNewKm());
                    }
                }

                RefreshResult rr = new RefreshResult();
                rr.tracks = tracks;
                rr.coordinates = coordinates;
                rr.waypoint = waypoint;
                rr.messages = sb;
                return rr;
            }

            @Override
            protected void done() {
                try {
                    RefreshResult rr = get();

                    SwingUtilities.invokeLater(() -> {
                        try {
                            JScrollPane mapScrollViewer = createMapViewer(rr.coordinates, rr.waypoint, true);
                            JScrollPane detailViewer = new AggregateDetailViewer(rr.tracks, currentFrame);
                            homeAggregateDetailViewer = detailViewer;
                            JScrollPane tableViewer = createTableTracksViewer(rr.tracks);

                            repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                            folderMapScrollViewer = mapScrollViewer;
                            folderDetailViewer = detailViewer;
                            folderTableViewer = tableViewer;

                            DataHolder.tracksLoaded = rr.tracks;

                            if (rr.messages != null && !rr.messages.isEmpty()) {
                                new Scrollable(null, rr.messages.toString(), dimension.getKey() / 2, dimension.getValue() / 2).showMessage();
                            }
                        } catch (Exception e) {
                            log.warn("Can't refresh library UI", e);
                        }
                    });

                } catch (Exception e) {
                    log.warn("refreshLibraryAndReloadUI failed", e);
                }
            }
        }.execute();
    }

    private static class RefreshResult {
        List<Track> tracks;
        List<List<Coordinate>> coordinates;
        List<Map<String, WaypointSegment>> waypoint;
        StringBuffer messages;
    }

    private void wireThemeMenu() {
        JRadioButtonMenuItem system = topMenu.getThemeSystemItem();
        JRadioButtonMenuItem light = topMenu.getThemeLightItem();
        JRadioButtonMenuItem dark = topMenu.getThemeDarkItem();

        if (system != null) system.addActionListener(e -> setTheme(ThemePreference.SYSTEM));
        if (light != null) light.addActionListener(e -> setTheme(ThemePreference.LIGHT));
        if (dark != null) dark.addActionListener(e -> setTheme(ThemePreference.DARK));
    }

    private void setTheme(ThemePreference pref) {
        ThemeManager.setThemePreference(pref);
        ThemeManager.applyTheme(pref, this);

        if (topMenu != null) {
            topMenu.selectThemeRadio(pref);
        }
    }

    private static List<RouteOverlay> toRoutes(
            List<List<ICoordinate>> coordinates,
            List<Map<String, WaypointSegment>> waypoints) {

        List<RouteOverlay> routes = new ArrayList<>();
        if (coordinates == null) return routes;

        for (int i = 0; i < coordinates.size(); i++) {
            List<ICoordinate> coords = coordinates.get(i);
            Map<String, WaypointSegment> wp = (waypoints != null && i < waypoints.size()) ? waypoints.get(i) : null;

            if (coords == null || coords.isEmpty()) continue;

            routes.add(new RouteOverlay(
                    "Track " + (i + 1),
                    coords,
                    wp,
                    RouteColors.baseColorForRoute(i)
            ));
        }
        return routes;
    }

    private void applyMultiTrackRowColors(JTable table, List<Track> tracksToLoad) {
        if (table == null || tracksToLoad == null || tracksToLoad.size() <= 1) {
            clearTrackRowColors(table);
            return;
        }

        // key = Name column (index 1)
        Map<String, Color> colorByName = new HashMap<>();
        for (int i = 0; i < tracksToLoad.size(); i++) {
            Track t = tracksToLoad.get(i);
            String name = (t.getName() != null && !t.getName().isBlank()) ? t.getName() : t.getFileName();
            colorByName.put(name, RouteColors.baseColorForRoute(i));
        }

        TableCellRenderer base = table.getDefaultRenderer(Object.class);
        table.putClientProperty("bikedump.baseObjectRenderer", base); // keep for restore
        table.setDefaultRenderer(Object.class, new org.hifly.bikedump.gui.table.TrackColorRowRenderer(base, colorByName, 1));
        table.repaint();
    }

    private void clearTrackRowColors(JTable table) {
        if (table == null) return;

        Object saved = table.getClientProperty("bikedump.baseObjectRenderer");
        if (saved instanceof TableCellRenderer) {
            table.setDefaultRenderer(Object.class, (TableCellRenderer) saved);
            table.putClientProperty("bikedump.baseObjectRenderer", null);
        } else {
            // fallback: reset to Swing default
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        }
        table.repaint();
    }

    public static void main(String[] args) {
        ThemeManager.initLookAndFeelEarly();
        SwingUtilities.invokeLater(() -> new Bikedump().setVisible(true));
    }
}