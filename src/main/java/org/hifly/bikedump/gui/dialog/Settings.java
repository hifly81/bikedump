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
    private JTextField stravaClientIdField = null;
    private JPasswordField stravaClientSecretField = null;
    private JButton stravaSaveCredentialsButton = null;
    private JButton stravaClearCredentialsButton = null;

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

        // -------------------- Credentials --------------------
        JPanel credPanel = new JPanel();
        credPanel.setLayout(new BoxLayout(credPanel, BoxLayout.Y_AXIS));
        credPanel.setBorder(new TitledBorder(new LineBorder(Color.RED), "Strava App Credentials"));

        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("Client ID:"));
        stravaClientIdField = new JTextField(20);
        stravaClientIdField.setText(GeoMapStorage.stravaPref.getClientId() == null ? "" : GeoMapStorage.stravaPref.getClientId());
        idPanel.add(stravaClientIdField);

        JPanel secretPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secretPanel.add(new JLabel("Client Secret:"));
        stravaClientSecretField = new JPasswordField(20);

        // IMPORTANT: for minimum exposure, do not re-display the real secret
        // If you prefer to show it, replace with: GeoMapStorage.stravaPref.getClientSecret()
        String existingSecret = GeoMapStorage.stravaPref.getClientSecret();
        if (existingSecret != null && !existingSecret.isEmpty()) {
            stravaClientSecretField.setText("********");
        } else {
            stravaClientSecretField.setText("");
        }
        secretPanel.add(stravaClientSecretField);

        JPanel credButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stravaSaveCredentialsButton = new JButton("Save");
        stravaSaveCredentialsButton.addActionListener(new StravaSaveCredentialsListener());
        stravaClearCredentialsButton = new JButton("Clear");
        stravaClearCredentialsButton.addActionListener(new StravaClearCredentialsListener());
        credButtons.add(stravaSaveCredentialsButton);
        credButtons.add(stravaClearCredentialsButton);

        JPanel credInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel credInfo = new JLabel("<html><small>Saved locally in ~/.bikedump/preferences/strava.pref (secret is obfuscated, not encrypted).</small></html>");
        credInfo.setForeground(Color.GRAY);
        credInfoPanel.add(credInfo);

        credPanel.add(idPanel);
        credPanel.add(secretPanel);
        credPanel.add(credButtons);
        credPanel.add(credInfoPanel);

        // -------------------- OAuth callback --------------------
        JPanel callbackPanel = new JPanel();
        callbackPanel.setLayout(new BoxLayout(callbackPanel, BoxLayout.Y_AXIS));
        callbackPanel.setBorder(new TitledBorder(new LineBorder(Color.BLUE), "OAuth Callback (Local)"));

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

        callbackPanel.add(hostPanel);
        callbackPanel.add(portPanel);
        callbackPanel.add(uriPanel);

        // -------------------- Auto Sync --------------------
        JPanel syncPanel = new JPanel();
        syncPanel.setLayout(new BoxLayout(syncPanel, BoxLayout.Y_AXIS));
        syncPanel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY), "Auto Sync"));

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

        JPanel syncInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel syncInfo = new JLabel("<html><small>Auto-sync runs only while the app is open.</small></html>");
        syncInfo.setForeground(Color.GRAY);
        syncInfoPanel.add(syncInfo);

        syncPanel.add(stravaAutoSyncEnabled);
        syncPanel.add(intervalPanel);
        syncPanel.add(syncInfoPanel);

        // Compose tab
        panel.add(credPanel);
        panel.add(callbackPanel);
        panel.add(syncPanel);

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
                GeoMapStorage.librarySetting.setScanFolder(e.getStateChange() != ItemEvent.DESELECTED);
            } else if (source == useOfflineTiles) {
                if (GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                GeoMapStorage.librarySetting.setUseOfflineTiles(e.getStateChange() != ItemEvent.DESELECTED);
            }
        }
    }

    class PathFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {}

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

    // -------------------- Strava listeners --------------------

    class StravaSaveCredentialsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            String clientId = stravaClientIdField.getText() == null ? "" : stravaClientIdField.getText().trim();
            String secretInput = new String(stravaClientSecretField.getPassword()).trim();

            if (clientId.isEmpty()) {
                JOptionPane.showMessageDialog(Settings.this, "Client ID is required.", "Strava", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // If user kept ********, do not overwrite existing secret
            boolean userDidNotChangeSecret = "********".equals(secretInput);

            GeoMapStorage.stravaPref.setClientId(clientId);
            if (!userDidNotChangeSecret) {
                if (secretInput.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Client Secret is required (or keep existing one).", "Strava", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                GeoMapStorage.stravaPref.setClientSecret(secretInput); // will be stored obfuscated in StravaPref
                stravaClientSecretField.setText("********");
            }

            GeoMapStorage.save();
            JOptionPane.showMessageDialog(Settings.this, "Strava credentials saved locally.", "Strava", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class StravaClearCredentialsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (GeoMapStorage.stravaPref == null)
                GeoMapStorage.stravaPref = new StravaPref();

            GeoMapStorage.stravaPref.setClientId("");
            GeoMapStorage.stravaPref.setClientSecret(null);

            stravaClientIdField.setText("");
            stravaClientSecretField.setText("");

            GeoMapStorage.save();
            JOptionPane.showMessageDialog(Settings.this, "Strava credentials cleared.", "Strava", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class StravaHostFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {}

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
        public void focusGained(FocusEvent e) {}

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