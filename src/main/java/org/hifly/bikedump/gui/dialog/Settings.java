package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class Settings extends JDialog {

    private static final long serialVersionUID = 14L;

    private JCheckBox scanFoldersCheck, elevationCorrection, showTipsAtStartup, useOfflineTiles = null;
    private JTextField offlineTilesPathField = null;
    private JButton browseOfflineTilesButton = null;

    // --- Strava UI ---
    private JTextField stravaHostField = null;
    private JTextField stravaPortField = null;
    private JCheckBox stravaAutoSyncEnabled = null;
    private JComboBox<String> stravaAutoSyncInterval = null;

    public Settings(Frame frame) {
        super(frame, true);

        setTitle("Options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(), "General settings");
        tabbedPane.addTab("Library", null, createLibrarySettingPanel(), "Library settings");
        tabbedPane.addTab("Strava", null, createStravaSettingPanel(), "Strava settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createGeneralSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "General");
        panel1.setBorder(titleBorder);

        elevationCorrection = new JCheckBox("Elevation Correction");
        elevationCorrection.addItemListener(new CheckListener());
        //TODO implement save/restore from pref
        elevationCorrection.setSelected(true);

        showTipsAtStartup = new JCheckBox("Show Tips at Startup");
        //TODO implement save/restore from pref
        showTipsAtStartup.setSelected(true);

        panel1.add(elevationCorrection);
        panel1.add(showTipsAtStartup);

        panel.add(panel1);

        return panel;
    }

    public JPanel createLibrarySettingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Library settings panel
        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Library");
        panel1.setBorder(titleBorder);

        scanFoldersCheck = new JCheckBox("Scan imported folders");
        scanFoldersCheck.addItemListener(new CheckListener());
        scanFoldersCheck.setSelected(GeoMapStorage.librarySetting == null ? false : GeoMapStorage.librarySetting.isScanFolder());

        panel1.add(scanFoldersCheck);

        // Offline tiles settings panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        Border tilesBorder = new TitledBorder(new LineBorder(Color.BLUE), "Offline Map Tiles");
        panel2.setBorder(tilesBorder);

        useOfflineTiles = new JCheckBox("Use offline map tiles");
        useOfflineTiles.addItemListener(new CheckListener());
        useOfflineTiles.setSelected(GeoMapStorage.librarySetting == null ? false : GeoMapStorage.librarySetting.isUseOfflineTiles());

        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathPanel.add(new JLabel("Tiles directory:"));
        offlineTilesPathField = new JTextField(30);
        String currentPath = GeoMapStorage.librarySetting != null ? GeoMapStorage.librarySetting.getOfflineTilesPath() : "";
        offlineTilesPathField.setText(currentPath != null ? currentPath : "");
        offlineTilesPathField.addFocusListener(new PathFieldListener());

        browseOfflineTilesButton = new JButton("Browse...");
        browseOfflineTilesButton.addActionListener(new BrowseActionListener());

        pathPanel.add(offlineTilesPathField);
        pathPanel.add(browseOfflineTilesButton);

        panel2.add(useOfflineTiles);
        panel2.add(pathPanel);

        // Add info text
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><small>Directory should contain tiles in format: {z}/{x}/{y}.png</small></html>");
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);
        panel2.add(infoPanel);

        panel.add(panel1);
        panel.add(panel2);

        return panel;
    }

    public JPanel createStravaSettingPanel() {
        if (GeoMapStorage.stravaPref == null) {
            GeoMapStorage.stravaPref = new StravaPref();
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // OAuth callback settings
        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "OAuth Callback (Local)");
        panel1.setBorder(titleBorder);
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostPanel.add(new JLabel("Redirect host:"));
        stravaHostField = new JTextField(20);
        String host = GeoMapStorage.stravaPref.getRedirectHost();
        stravaHostField.setText(host == null || host.isEmpty() ? "127.0.0.1" : host);
        stravaHostField.addFocusListener(new StravaHostFieldListener());
        hostPanel.add(stravaHostField);

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portPanel.add(new JLabel("Callback port:"));
        stravaPortField = new JTextField(8);
        int port = GeoMapStorage.stravaPref.getCallbackPort();
        stravaPortField.setText(String.valueOf(port <= 0 ? 8765 : port));
        stravaPortField.addFocusListener(new StravaPortFieldListener());
        portPanel.add(stravaPortField);

        JPanel uriPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel uriLabel = new JLabel();
        uriLabel.setForeground(Color.GRAY);
        uriLabel.setText(buildRedirectUriText());
        uriPanel.add(uriLabel);

        // update label live when fields change
        stravaHostField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                uriLabel.setText(buildRedirectUriText());
            }
        });
        stravaPortField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                uriLabel.setText(buildRedirectUriText());
            }
        });

        panel1.add(hostPanel);
        panel1.add(portPanel);
        panel1.add(uriPanel);

        // Auto-sync settings
        JPanel panel2 = new JPanel();
        Border syncBorder = new TitledBorder(new LineBorder(Color.BLUE), "Auto Sync");
        panel2.setBorder(syncBorder);
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

        stravaAutoSyncEnabled = new JCheckBox("Enable auto-sync");
        stravaAutoSyncEnabled.setSelected(GeoMapStorage.stravaPref.isAutoSyncEnabled());
        stravaAutoSyncEnabled.addItemListener(new StravaSyncCheckListener());

        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.add(new JLabel("Interval:"));
        stravaAutoSyncInterval = new JComboBox<>(new String[]{
                "30 minutes",
                "1 hour",
                "6 hours",
                "24 hours"
        });
        stravaAutoSyncInterval.setSelectedItem(millisToIntervalLabel(GeoMapStorage.stravaPref.getAutoSyncIntervalMillis()));
        stravaAutoSyncInterval.addActionListener(new StravaIntervalActionListener());
        intervalPanel.add(stravaAutoSyncInterval);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><small>Note: auto-sync runs in background only while the app is open.</small></html>");
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);

        panel2.add(stravaAutoSyncEnabled);
        panel2.add(intervalPanel);
        panel2.add(infoPanel);

        panel.add(panel1);
        panel.add(panel2);

        return panel;
    }

    private String buildRedirectUriText() {
        String host = (stravaHostField != null ? stravaHostField.getText() : null);
        if (host == null || host.trim().isEmpty()) host = "127.0.0.1";

        int port = 8765;
        try {
            String v = (stravaPortField != null ? stravaPortField.getText() : null);
            if (v != null && !v.trim().isEmpty()) port = Integer.parseInt(v.trim());
        } catch (Exception ignored) {
        }

        return "<html><small>Redirect URI: http://" + host + ":" + port + "/strava/callback</small></html>";
    }

    private static String millisToIntervalLabel(long ms) {
        if (ms <= 30 * 60 * 1000L) return "30 minutes";
        if (ms <= 60 * 60 * 1000L) return "1 hour";
        if (ms <= 6 * 60 * 60 * 1000L) return "6 hours";
        return "24 hours";
    }

    private static long intervalLabelToMillis(String label) {
        if (label == null) return 6 * 60 * 60 * 1000L;
        switch (label) {
            case "30 minutes": return 30 * 60 * 1000L;
            case "1 hour": return 60 * 60 * 1000L;
            case "6 hours": return 6 * 60 * 60 * 1000L;
            case "24 hours": return 24 * 60 * 60 * 1000L;
            default: return 6 * 60 * 60 * 1000L;
        }
    }

    class CheckListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();

            if (source == scanFoldersCheck) {
                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    GeoMapStorage.librarySetting.setScanFolder(false);
                else
                    GeoMapStorage.librarySetting.setScanFolder(true);
            } else if (source == useOfflineTiles) {
                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    GeoMapStorage.librarySetting.setUseOfflineTiles(false);
                else
                    GeoMapStorage.librarySetting.setUseOfflineTiles(true);
            }
        }
    }

    class PathFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            // Nothing to do
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (GeoMapStorage.librarySetting == null)
                GeoMapStorage.librarySetting = new LibrarySetting();
            GeoMapStorage.librarySetting.setOfflineTilesPath(offlineTilesPathField.getText());
        }
    }

    class BrowseActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Offline Tiles Directory");

            String currentPath = offlineTilesPathField.getText();
            if (currentPath != null && !currentPath.isEmpty()) {
                chooser.setCurrentDirectory(new java.io.File(currentPath));
            }

            int result = chooser.showOpenDialog(Settings.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String selectedPath = chooser.getSelectedFile().getAbsolutePath();
                offlineTilesPathField.setText(selectedPath);

                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                GeoMapStorage.librarySetting.setOfflineTilesPath(selectedPath);
            }
        }
    }

    // --- Strava listeners ---

    class StravaHostFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { }

        @Override
        public void focusLost(FocusEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            String host = stravaHostField.getText();
            if (host == null || host.trim().isEmpty()) host = "127.0.0.1";
            GeoMapStorage.stravaPref.setRedirectHost(host.trim());

            GeoMapStorage.save();
        }
    }

    class StravaPortFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { }

        @Override
        public void focusLost(FocusEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            String raw = stravaPortField.getText();
            int port;
            try {
                port = Integer.parseInt(raw.trim());
                if (port < 1 || port > 65535) throw new NumberFormatException("range");
            } catch (Exception ex) {
                // revert to current saved port (or default)
                int current = GeoMapStorage.stravaPref.getCallbackPort();
                if (current <= 0) current = 8765;
                stravaPortField.setText(String.valueOf(current));
                return;
            }

            GeoMapStorage.stravaPref.setCallbackPort(port);
            GeoMapStorage.save();
        }
    }

    class StravaSyncCheckListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            GeoMapStorage.stravaPref.setAutoSyncEnabled(e.getStateChange() != ItemEvent.DESELECTED);
            GeoMapStorage.save();
        }
    }

    class StravaIntervalActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            String label = (String) stravaAutoSyncInterval.getSelectedItem();
            GeoMapStorage.stravaPref.setAutoSyncIntervalMillis(intervalLabelToMillis(label));
            GeoMapStorage.save();
        }
    }
}