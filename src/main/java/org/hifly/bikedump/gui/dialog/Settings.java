package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serial;

public class Settings extends JDialog {

    @Serial
    private static final long serialVersionUID = 14L;

    private JCheckBox scanFoldersCheck;
    private JCheckBox useOfflineTiles = null;
    private JTextField offlineTilesPathField = null;

    private JTextField stravaClientIdField = null;
    private JPasswordField stravaClientSecretField = null;

    private JTextField stravaHostField = null;
    private JTextField stravaPortField = null;

    private JComboBox<String> stravaAutoSyncInterval = null;

    private JTextField osmApiKeyField = null;

    public Settings(Frame frame) {
        super(frame, true);

        setTitle("Options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(), "General settings");
        tabbedPane.addTab("Maps", null, createMapsSettingPanel(), "Map settings");
        tabbedPane.addTab("Strava", null, createStravaSettingPanel(), "Strava settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createGeneralSettingPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        scanFoldersCheck = new JCheckBox("Scan imported folders");
        scanFoldersCheck.addItemListener(new CheckListener());
        scanFoldersCheck.setSelected(GeoMapStorage.librarySetting != null && GeoMapStorage.librarySetting.isScanFolder());
        root.add(scanFoldersCheck, c);

        c.gridy++;

        JCheckBox elevationCorrection = new JCheckBox("Elevation Correction");
        elevationCorrection.addItemListener(new CheckListener());
        elevationCorrection.setSelected(true); // TODO: persist if you want
        root.add(elevationCorrection, c);

        c.gridy++;

        JCheckBox showTipsAtStartup = new JCheckBox("Show Tips at Startup");
        showTipsAtStartup.setSelected(true); // TODO: persist if you want
        root.add(showTipsAtStartup, c);

        // push everything up
        c.gridy++;
        c.weighty = 1.0;
        root.add(Box.createVerticalGlue(), c);

        return root;
    }

    public JPanel createMapsSettingPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        useOfflineTiles = new JCheckBox("Use offline map tiles");
        useOfflineTiles.addItemListener(new CheckListener());
        useOfflineTiles.setSelected(GeoMapStorage.librarySetting != null && GeoMapStorage.librarySetting.isUseOfflineTiles());

        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        root.add(useOfflineTiles, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        root.add(new JLabel("Tiles directory:"), c);

        offlineTilesPathField = new JTextField(28);
        String currentPath = GeoMapStorage.librarySetting != null ? GeoMapStorage.librarySetting.getOfflineTilesPath() : "";
        offlineTilesPathField.setText(currentPath != null ? currentPath : "");
        offlineTilesPathField.addFocusListener(new PathFieldListener());

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        root.add(offlineTilesPathField, c);

        JButton browseOfflineTilesButton = new JButton("Browse...");
        browseOfflineTilesButton.addActionListener(new BrowseActionListener());

        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        root.add(browseOfflineTilesButton, c);

        c.gridx = 0;
        c.gridy++;
        root.add(new JLabel("OSM API key:"), c);

        osmApiKeyField = new JTextField(28);
        String k = GeoMapStorage.librarySetting != null ? GeoMapStorage.librarySetting.getOsmApiKey() : "";
        osmApiKeyField.setText(k != null ? k : "");

        // save on focus lost (simple)
        osmApiKeyField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ensureLibrarySetting();
                GeoMapStorage.librarySetting.setOsmApiKey(osmApiKeyField.getText());
                GeoMapStorage.save();
            }
        });

        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        root.add(osmApiKeyField, c);

        // push up
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.weighty = 1.0;
        root.add(Box.createVerticalGlue(), c);

        return root;
    }

    private void ensureLibrarySetting() {
        if (GeoMapStorage.librarySetting == null) {
            GeoMapStorage.librarySetting = new LibrarySetting();
        }
    }
    public JPanel createStravaSettingPanel() {
        if (GeoMapStorage.stravaPref == null) {
            GeoMapStorage.stravaPref = new StravaPref();
        }

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(sectionTitle("Strava App Credentials"));
        root.add(buildStravaCredentialsPanel());
        root.add(Box.createVerticalStrut(10));

        root.add(sectionTitle("OAuth Callback (Local)"));
        root.add(buildStravaCallbackPanel());
        root.add(Box.createVerticalStrut(10));

        root.add(sectionTitle("Auto Sync"));
        root.add(buildStravaAutoSyncPanel());

        root.add(Box.createVerticalGlue());

        return root;
    }

    private JComponent sectionTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildStravaCredentialsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        p.add(new JLabel("Client ID:"), c);

        stravaClientIdField = new JTextField(22);
        stravaClientIdField.setText(GeoMapStorage.stravaPref.getClientId() == null ? "" : GeoMapStorage.stravaPref.getClientId());

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(stravaClientIdField, c);

        // Client Secret
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Client Secret:"), c);

        stravaClientSecretField = new JPasswordField(22);

        String existingSecret = GeoMapStorage.stravaPref.getClientSecret();
        if (existingSecret != null && !existingSecret.isEmpty()) {
            stravaClientSecretField.setText("********");
        } else {
            stravaClientSecretField.setText("");
        }

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(stravaClientSecretField, c);

        // Buttons row
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton stravaSaveCredentialsButton = new JButton("Save");
        stravaSaveCredentialsButton.addActionListener(new StravaSaveCredentialsListener());

        JButton stravaClearCredentialsButton = new JButton("Clear");
        stravaClearCredentialsButton.addActionListener(new StravaClearCredentialsListener());

        buttons.add(stravaSaveCredentialsButton);
        buttons.add(stravaClearCredentialsButton);
        p.add(buttons, c);

        // Info row
        c.gridy++;
        JLabel info = new JLabel("<html><small>Saved locally in ~/.bikedump/preferences/strava.pref (secret is obfuscated, not encrypted).</small></html>");
        info.setForeground(Color.GRAY);
        p.add(info, c);

        return p;
    }

    private JPanel buildStravaCallbackPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        p.add(new JLabel("Redirect host:"), c);

        stravaHostField = new JTextField(22);
        String host = GeoMapStorage.stravaPref.getRedirectHost();
        stravaHostField.setText(host == null || host.isEmpty() ? "127.0.0.1" : host);
        stravaHostField.addFocusListener(new StravaHostFieldListener());

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(stravaHostField, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Callback port:"), c);

        stravaPortField = new JTextField(8);
        int port = GeoMapStorage.stravaPref.getCallbackPort();
        stravaPortField.setText(String.valueOf(port <= 0 ? 8765 : port));
        stravaPortField.addFocusListener(new StravaPortFieldListener());

        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        p.add(stravaPortField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        JLabel uriLabel = new JLabel();
        uriLabel.setForeground(Color.GRAY);
        uriLabel.setText(buildRedirectUriText());

        stravaHostField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { uriLabel.setText(buildRedirectUriText()); }
        });
        stravaPortField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { uriLabel.setText(buildRedirectUriText()); }
        });

        p.add(uriLabel, c);

        return p;
    }

    private JPanel buildStravaAutoSyncPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        JCheckBox stravaAutoSyncEnabled = new JCheckBox("Enable auto-sync");
        stravaAutoSyncEnabled.setSelected(GeoMapStorage.stravaPref.isAutoSyncEnabled());
        stravaAutoSyncEnabled.addItemListener(new StravaSyncCheckListener());

        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(stravaAutoSyncEnabled, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Interval:"), c);

        stravaAutoSyncInterval = new JComboBox<>(new String[]{
                "30 minutes",
                "1 hour",
                "6 hours",
                "24 hours"
        });
        stravaAutoSyncInterval.setSelectedItem(millisToIntervalLabel(GeoMapStorage.stravaPref.getAutoSyncIntervalMillis()));
        stravaAutoSyncInterval.addActionListener(new StravaIntervalActionListener());

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.NONE;
        p.add(stravaAutoSyncInterval, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel info = new JLabel("<html><small>Auto-sync runs only while the app is open.</small></html>");
        info.setForeground(Color.GRAY);
        p.add(info, c);

        return p;
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