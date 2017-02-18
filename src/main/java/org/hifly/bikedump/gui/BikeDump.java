package org.hifly.bikedump.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.bikedump.controller.GPSController;
import org.hifly.bikedump.controller.StravaController;
import org.hifly.bikedump.domain.*;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.domain.strava.StravaSetting;
import org.hifly.bikedump.gui.dialog.ScrollableDialog;
import org.hifly.bikedump.gui.dialog.SettingDialog;
import org.hifly.bikedump.gui.dialog.TipOfTheDay;
import org.hifly.bikedump.gui.dialog.strava.StravaDialog;
import org.hifly.bikedump.gui.events.QuitAppHandler;
import org.hifly.bikedump.gui.events.TableSelectionHandler;
import org.hifly.bikedump.gui.menu.GeoFileChooser;
import org.hifly.bikedump.gui.menu.GeoFolderChooser;
import org.hifly.bikedump.gui.menu.GeoMapMenu;
import org.hifly.bikedump.gui.menu.GeoToolbar;
import org.hifly.bikedump.gui.panel.AggregateDetailViewer;
import org.hifly.bikedump.gui.panel.DetailViewer;
import org.hifly.bikedump.gui.panel.MapViewer;
import org.hifly.bikedump.gui.panel.TrackTable;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.timer.NewTrackTimer;
import org.hifly.bikedump.utility.GUIUtility;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

//TODO use resource bundles i18n
public class BikeDump extends JFrame implements JMapViewerEventListener {

    private ProfileSetting profileSetting;
    private SettingDialog settingDialog = null;
    private StravaSetting stravaSetting = null;
    private StravaDialog stravaDialog = null;
    private BikeDump currentFrame = this;
    protected MapViewer mapViewer;
    private JSplitPane mainPanel = new JSplitPane();
    private JLabel zoomLabel, zoomValue, measureLabel, measureValue;
    private Map.Entry<Integer, Integer> dimension;
    private JScrollPane folderMapScrollViewer, folderDetailViewer, folderTableViewer;
    public TrackTable trackTable = null;
    private String textForReport;

    public BikeDump() {
        super();
        //panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey(), dimension.getValue());
        setTitle("BikeDump v0.1");
        setName("BikeDump v0.1");
        //layout
        setLayout(new BorderLayout());
        //exit behaviour
        addWindowListener(new QuitAppHandler());

        //settings
        initSettings();

        //dialogs
        initDialogs();

        //create toolbar
        GeoToolbar toolBar = new GeoToolbar(currentFrame);
        add(toolBar, BorderLayout.PAGE_START);

        //create menu and its events
        GeoMapMenu mainMenu = new GeoMapMenu(currentFrame);
        final GeoFileChooser fileChooser = new GeoFileChooser();
        final GeoFolderChooser folderChooser = new GeoFolderChooser();

        //import file action
        JMenuItem itemImportFile = mainMenu.getItemImportFile();
        itemImportFile.addActionListener(event -> {
            int returnVal = fileChooser.showOpenDialog(BikeDump.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                reloadTrackFromFile(file);
            }
        });

        //import folder action
        JMenuItem itemImportFolder = mainMenu.getItemImportFolder();
        itemImportFolder.addActionListener(event -> {
            int returnVal = folderChooser.showOpenDialog(BikeDump.this);
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
                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (GeoMapStorage.librarySetting.getScannedDirs() == null)
                    GeoMapStorage.librarySetting.setScannedDirs(new ArrayList());
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
                JScrollPane tableViewer = createTableTracksViewer(tracks);

                repaintPanels(tableViewer, mapScrollViewer, detailViewer);

                folderMapScrollViewer = mapScrollViewer;
                folderDetailViewer = detailViewer;
                folderTableViewer = tableViewer;

                DataHolder.tracksLoaded = tracks;
            }
        });

        //strava sync menu item
        JMenuItem itemStravaSync = mainMenu.getIteamStravaSync();
        itemStravaSync.addActionListener(event -> {
            stravaDialog.setLocationRelativeTo(currentFrame);
            stravaDialog.setVisible(true);

        });

        //profile setting menu item
        JMenuItem itemProfileSetting = mainMenu.getItemOptionsSetting();
        itemProfileSetting.addActionListener(event -> {
            settingDialog.setLocationRelativeTo(currentFrame);
            settingDialog.setVisible(true);

        });

        //add menu
        setJMenuBar(mainMenu);

        new SwingWorker<Void, String>() {
            final JLabel label = new JLabel("Loading... ", JLabel.CENTER);
            @Override
            protected Void doInBackground() throws Exception {
                add(label, BorderLayout.CENTER);
                label.setVisible(true);

                List<Track> tracks = new ArrayList();
                List<List<Coordinate>> coordinates = new ArrayList();
                List<Map<String, WaypointSegment>> waypoint = new ArrayList();

                if(stravaSetting != null && stravaSetting.getCurrentAthleteSelected() != null) {
                    Map<String, String> stravaActivities = stravaSetting.getCurrentAthleteSelected().getActivitiesSelected();
                    for (Map.Entry<String, String> entry : stravaActivities.entrySet()) {
                        Track trackFromStrava =
                                StravaController.getInstance(stravaSetting.getCurrentAthleteSelected().getAccessToken()).getTrackFromStravaFile(System.getProperty("user.home") + "/.geomapviewer/strava/" + stravaSetting.getCurrentAthleteSelected().getAccessToken() + "/" + entry.getKey() + ".prop");
                        tracks.add(trackFromStrava);
                    }
                }

                //load saved elements
                if (GeoMapStorage.tracksLibrary != null) {
                    if (DataHolder.listsWaypointSegment == null)
                        DataHolder.listsWaypointSegment = new ArrayList();
                    StringBuffer sb = new StringBuffer();
                    for (Map.Entry<String, TrackPref> entry : GeoMapStorage.tracksLibrary.entrySet()) {
                        File file = new File(entry.getKey());
                        addTrackToCache(coordinates, waypoint, tracks, file, sb);
                    }

                    //TODO check for new files at regular interval --> TimerTask
                    checkNewTrack(coordinates, waypoint, tracks, sb);

                }

                JScrollPane mapScrollViewer = createMapViewer(coordinates, waypoint, true);
                JScrollPane detailViewer = new AggregateDetailViewer(tracks, currentFrame);
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
                TipOfTheDay tip = new TipOfTheDay();
                tip.setVisible(true);

                new NewTrackTimer(currentFrame);


            }
        }.execute();

    }

    public void repaintPanels(
            JScrollPane treeViewer,
            JScrollPane mapScrollViewer,
            JScrollPane detailViewer) {

        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeViewer, mapScrollViewer);
        split1.setDividerLocation(dimension.getKey() / 3);
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
                for(TrackSelected trackSelected: DataHolder.tracksSelected) {
                    int index = DataHolder.tracksLoaded.indexOf(new Track(trackSelected.getFilename()));
                    if(index != -1) {
                        Track track = DataHolder.tracksLoaded.get(index);
                        if (track != null)
                            selectedTracks.add(track);
                    }
                }

                if (!selectedTracks.isEmpty()) {
                    if (selectedTracks.size() == 1) {
                        Track track = selectedTracks.get(0);
                        if(track.isFromStrava())
                            reloadTrackPanelFromStrava(track, stravaSetting.getCurrentAthleteSelected().getAccessToken());
                        else
                            reloadTrackFromFile(new File(selectedTracks.get(0).getFileName()));
                    } else {
                        List<Track> tracksToLoad = new ArrayList();
                        for(Track track: selectedTracks)
                            if (track.isFromStrava())
                                tracksToLoad.add(StravaController.getInstance(stravaSetting.getCurrentAthleteSelected().getAccessToken()).getFullInfoFromStrava(track, profileSetting));
                            else
                                tracksToLoad.add(track);

                        JScrollPane detailViewer = new AggregateDetailViewer(tracksToLoad, currentFrame);
                        JScrollPane tableViewer = createTableTracksViewer(tracksToLoad);
                        repaintPanels(tableViewer, folderMapScrollViewer, detailViewer);
                    }
                    table.clearSelection();
                    table.transferFocus();
                    DataHolder.tracksSelected.clear();

                }

            }
        }
    }

    public void checkNewTrack(List<List<Coordinate>> coordinates, List<Map<String, WaypointSegment>> waypoint, List<Track> tracks, StringBuffer sb) {
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

        //List of loader results
        if (sb.length() > 0) {
            ScrollableDialog dialog = new ScrollableDialog(null, sb.toString(), dimension.getKey() / 2, dimension.getValue() / 2);
            dialog.showMessage();
        }
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            if (measureValue != null)
                measureValue.setText(String.format("%s", mapViewer.getMeterPerPixel()));
            if (zoomValue != null)
                zoomValue.setText(String.format("%s", mapViewer.getZoom()));
        }
    }

    private void initSettings() {
        //saved pref
        profileSetting = GeoMapStorage.profileSetting;
        if (profileSetting == null)
            profileSetting = new ProfileSetting();

        stravaSetting = GeoMapStorage.stravaSetting;
        if (stravaSetting == null) {
            stravaSetting = new StravaSetting();
            stravaSetting.setStravaAthletes(new ArrayList());
        }

    }

    private void initDialogs() {
        //define dialogs
        settingDialog = new SettingDialog(currentFrame, profileSetting);
        settingDialog.pack();

        stravaDialog = new StravaDialog(currentFrame, stravaSetting);
        stravaDialog.pack();
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
            if (resultList.size() > 2)
                mapViewer = new MapViewer(null, null, 10, resultList.get(0).get(0).getLat(), resultList.get(0).get(0).getLon());
            else
                mapViewer = new MapViewer(resultList, waypoints, 20);
        }
        else
            mapViewer = new MapViewer(resultList, waypoints, 20);
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
        button.addActionListener(e -> mapViewer.setDisplayToFitMapMarkers());

        JComboBox tileSourceSelector = new JComboBox(new TileSource[]{new OsmTileSource.Mapnik(),
                new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource()});
        tileSourceSelector.addItemListener(e -> mapViewer.setTileSource((TileSource) e.getItem()));

        JComboBox tileLoaderSelector;
        try {
            tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmFileCacheTileLoader(mapViewer),
                    new OsmTileLoader(mapViewer)});
        } catch (IOException e) {
            tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmTileLoader(mapViewer)});
        }
        tileLoaderSelector.addItemListener(e -> mapViewer.setTileLoader((TileLoader) e.getItem()));
        mapViewer.setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());

        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);

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
        showZoomControls.setSelected(mapViewer.getZoomContolsVisible());
        showZoomControls.addActionListener(e -> mapViewer.setZoomContolsVisible(showZoomControls.isSelected()));
        panelBottom.add(showZoomControls);

        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(e -> mapViewer.setScrollWrapEnabled(scrollWrapEnabled.isSelected()));

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

    private void reloadTrackPanelFromStrava(Track track, String accessToken) {
        track = StravaController.getInstance(accessToken).getFullInfoFromStrava(track, profileSetting);
        reloadTrack(new AbstractMap.SimpleImmutableEntry(track, new StringBuffer("")));
    }

    private void reloadTrackFromFile(File file) {
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        Map.Entry<Track, StringBuffer> resultTrack;
        if (ext.equalsIgnoreCase("gpx"))
            resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
        else if (ext.equalsIgnoreCase("tcx"))
            resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
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

        //TODO check if file already exists in Cache

        if (file.exists()) {

            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            Track track;
            Map.Entry<Track, StringBuffer> resultTrack;
            //TODO check not based on extension
            if (ext.equalsIgnoreCase("gpx"))
                resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
            else if (ext.equalsIgnoreCase("tcx"))
                resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
            else
                return;

            if (resultTrack != null && resultTrack.getValue().toString().equals("")) {
                track = resultTrack.getKey();
                if (track != null) {
                    //add to map
                    DataHolder.mapFilePathTrack.put(track.getName(), track.getFileName());
                    //add to opened files map
                    if (GeoMapStorage.tracksLibrary == null)
                        GeoMapStorage.tracksLibrary = new HashMap();
                    TrackPref trackPref = new TrackPref();
                    Profile profile = profileSetting.getSelectedProfile();
                    trackPref.setProfile(profile);

                    GeoMapStorage.tracksLibrary.put(track.getFileName(), trackPref);
                    coordinates.add(track.getCoordinates());
                    waypoint.add(track.getCoordinatesNewKm());
                    tracks.add(track);
                    if(track.getCoordinatesNewKm() !=null && !track.getCoordinatesNewKm().isEmpty()) {
                        List<WaypointSegment> listWaypoints =
                                new ArrayList(track.getCoordinatesNewKm().values());
                        DataHolder.listsWaypointSegment.add(listWaypoints);
                    }
                }
            } else
                sb.append(resultTrack.getValue().toString()).append("\n");
        }

    }

    private JScrollPane createTableTracksViewer(List<Track> tracks) {
        JScrollPane panel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        final TrackTable table = new TrackTable(tracks);
        table.getSelectionModel().addListSelectionListener(
                //TODO if track is already selected dont load again
                //TODO if a list of track is shown, when load the single track don't redraw the table list
                new TableSelectionHandler(table, new HashSet()));

        table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadSelectedTracks(table);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
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

    public static void main(String[] args) throws Exception {
        BikeDump viewer = new BikeDump();
        viewer.setVisible(true);
    }

}
