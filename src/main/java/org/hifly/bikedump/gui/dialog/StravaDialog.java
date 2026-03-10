package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.integration.strava.StravaClient;
import org.hifly.bikedump.integration.strava.StravaImporter;
import org.hifly.bikedump.integration.strava.StravaOAuthLocalServer;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class StravaDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // TODO: set these properly (or read from env/props)
    private static final String STRAVA_CLIENT_ID = "PUT_CLIENT_ID_HERE";
    private static final String STRAVA_CLIENT_SECRET = "PUT_CLIENT_SECRET_HERE";

    private final StravaPref pref;

    private JLabel status;
    private JCheckBox autoSync;
    private JComboBox<String> interval;

    public StravaDialog(Frame owner) {
        super(owner, true);
        setTitle("Strava");
        this.pref = GeoMapStorage.stravaPref;

        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> setVisible(false));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);
        add(south, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        refreshUi();
    }

    private JPanel buildPanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        status = new JLabel("");
        root.add(status);

        root.add(Box.createVerticalStrut(10));

        JButton connect = new JButton("Connect to Strava (browser)");
        connect.addActionListener(e -> onConnect());
        root.add(connect);

        JButton disconnect = new JButton("Disconnect");
        disconnect.addActionListener(e -> {
            pref.disconnect();
            GeoMapStorage.save();
            refreshUi();
        });
        root.add(disconnect);

        root.add(Box.createVerticalStrut(10));

        JButton importNow = new JButton("Import latest rides now");
        importNow.addActionListener(e -> onImportNow());
        root.add(importNow);

        root.add(Box.createVerticalStrut(10));

        autoSync = new JCheckBox("Enable auto-sync");
        autoSync.addActionListener(e -> {
            pref.setAutoSyncEnabled(autoSync.isSelected());
            GeoMapStorage.save();
        });
        root.add(autoSync);

        interval = new JComboBox<>(new String[]{
                "30 minutes",
                "1 hour",
                "6 hours",
                "24 hours"
        });
        interval.addActionListener(e -> {
            pref.setAutoSyncIntervalMillis(parseIntervalMillis((String) interval.getSelectedItem()));
            GeoMapStorage.save();
        });
        root.add(interval);

        return root;
    }

    private void refreshUi() {
        status.setText(pref.isConnected() ? "Status: Connected" : "Status: Not connected");
        autoSync.setSelected(pref.isAutoSyncEnabled());

        // set combo based on millis
        long ms = pref.getAutoSyncIntervalMillis();
        if (ms <= 30 * 60 * 1000L) interval.setSelectedItem("30 minutes");
        else if (ms <= 60 * 60 * 1000L) interval.setSelectedItem("1 hour");
        else if (ms <= 6 * 60 * 60 * 1000L) interval.setSelectedItem("6 hours");
        else interval.setSelectedItem("24 hours");
    }

    private long parseIntervalMillis(String label) {
        if (label == null) return 6 * 60 * 60 * 1000L;
        switch (label) {
            case "30 minutes": return 30 * 60 * 1000L;
            case "1 hour": return 60 * 60 * 1000L;
            case "6 hours": return 6 * 60 * 60 * 1000L;
            case "24 hours": return 24 * 60 * 60 * 1000L;
            default: return 6 * 60 * 60 * 1000L;
        }
    }

    private void onConnect() {
        try {
            StravaClient client = new StravaClient(STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET);
            String state = UUID.randomUUID().toString();
            String host = pref.getRedirectHost();
            int port = pref.getCallbackPort();

            StravaOAuthLocalServer server = new StravaOAuthLocalServer(host, port);
            String authUrl = client.buildAuthorizeUrl(server.getRedirectUri(), state);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                JOptionPane.showMessageDialog(this, "Open this URL in your browser:\n" + authUrl);
            }

            StravaOAuthLocalServer.OAuthResult res = server.waitForCallback(2 * 60 * 1000L);
            if (res.error != null) {
                JOptionPane.showMessageDialog(this, "Strava auth error: " + res.error, "Strava", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (res.code == null || res.code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Missing code in callback.", "Strava", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (res.state == null || !state.equals(res.state)) {
                JOptionPane.showMessageDialog(this, "Invalid OAuth state.", "Strava", JOptionPane.ERROR_MESSAGE);
                return;
            }

            client.exchangeCodeForToken(pref, res.code);
            GeoMapStorage.save();
            refreshUi();
            JOptionPane.showMessageDialog(this, "Connected to Strava.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connect failed: " + ex.getMessage(), "Strava", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onImportNow() {
        if (!pref.isConnected()) {
            JOptionPane.showMessageDialog(this, "Not connected to Strava.", "Strava", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            StravaClient client = new StravaClient(STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET);
            StravaImporter importer = new StravaImporter(client);

            List<Track> tracks = importer.importNewRides(pref, 20);
            GeoMapStorage.save();

            JOptionPane.showMessageDialog(this, "Imported rides: " + tracks.size() + "\nSaved in: " + GeoMapStorage.getStravaExportsDir());
            // TODO: qui puoi agganciare la UI per caricare automaticamente i nuovi file in tabella
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Strava", JOptionPane.ERROR_MESSAGE);
        }
    }
}