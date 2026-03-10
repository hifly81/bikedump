package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.integration.strava.StravaClient;
import org.hifly.bikedump.integration.strava.StravaImporter;
import org.hifly.bikedump.integration.strava.StravaOAuthLocalServer;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class StravaDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String OAUTH_HOST = "127.0.0.1";
    private static final int OAUTH_PORT = 8765;
    private static final long OAUTH_TIMEOUT_MILLIS = 2 * 60 * 1000L; // 2 min

    private final StravaPref pref;

    // Status
    private final JLabel statusLabel = new JLabel();

    // Test API
    private final JTextField testAfterField = new JTextField("", 12);   // epoch seconds or empty
    private final JTextField testBeforeField = new JTextField("", 12);  // epoch seconds or empty

    // Import (range)
    private final JTextField maxField = new JTextField("20", 6);
    private final JTextField fromField = new JTextField("2024-01-01", 10);
    private final JTextField toField = new JTextField(LocalDate.now().toString(), 10);

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

        refreshStatus();

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildPanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(sectionTitle("Connection"));
        root.add(buildConnectionPanel());
        root.add(Box.createVerticalStrut(10));

        root.add(sectionTitle("Import rides"));
        root.add(buildImportPanel());

        return root;
    }

    private static JComponent sectionTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JPanel buildConnectionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        p.add(new JLabel("Status:"), c);

        c.gridx = 1;
        p.add(statusLabel, c);

        c.gridx = 0; c.gridy = 1;
        JButton connect = new JButton("Connect");
        connect.addActionListener(e -> onConnect());
        p.add(connect, c);

        c.gridx = 1;
        JButton disconnect = new JButton("Disconnect");
        disconnect.addActionListener(e -> onDisconnect());
        p.add(disconnect, c);

        JLabel hint = new JLabel("<html><small>Connect opens the browser and waits for the callback on "
                + OAUTH_HOST + ":" + OAUTH_PORT + ".</small></html>");
        hint.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        p.add(hint, c);

        return p;
    }

    private JPanel buildImportPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("<html><small>Imports Ride activities as GPX using Strava Streams. Dates are UTC (YYYY-MM-DD).</small></html>");
        hint.setForeground(Color.GRAY);
        p.add(hint);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Max rides:"), c);
        c.gridx = 1;
        form.add(maxField, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("From (YYYY-MM-DD):"), c);
        c.gridx = 1;
        form.add(fromField, c);

        c.gridx = 0; c.gridy = 2;
        form.add(new JLabel("To (YYYY-MM-DD):"), c);
        c.gridx = 1;
        form.add(toField, c);

        p.add(form);

        JButton importBtn = new JButton("Import");
        importBtn.addActionListener(e -> onImportWithRange());
        p.add(importBtn);

        return p;
    }

    private void ensurePref() {
        if (GeoMapStorage.stravaPref == null) {
            GeoMapStorage.stravaPref = new StravaPref();
        }
    }

    private void refreshStatus() {
        ensurePref();
        boolean connected = pref != null && pref.isConnected();
        String text = connected ? "Connected" : "Not connected";
        if (connected && pref.isAccessTokenExpired()) {
            text += " (token expired, will refresh on API call)";
        }
        statusLabel.setText(text);
    }

    private StravaClient buildClientOrWarn() {
        ensurePref();

        String id = pref.getClientId();
        String secret = pref.getClientSecret();

        if (id == null || id.trim().isEmpty() || secret == null || secret.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Missing Strava Client ID/Secret.\nGo to: Options → Strava and save credentials first.",
                    "Strava",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new StravaClient(id.trim(), secret.trim());
    }

    private void ensureStravaExportsFolderIsScanned() {
        if (GeoMapStorage.librarySetting == null) {
            GeoMapStorage.librarySetting = new LibrarySetting();
        }
        if (GeoMapStorage.librarySetting.getScannedDirs() == null) {
            GeoMapStorage.librarySetting.setScannedDirs(new ArrayList<>());
        }

        String stravaDir = GeoMapStorage.getStravaExportsDir();
        boolean found = false;
        for (String d : GeoMapStorage.librarySetting.getScannedDirs()) {
            if (d != null && d.equalsIgnoreCase(stravaDir)) {
                found = true;
                break;
            }
        }
        if (!found) {
            GeoMapStorage.librarySetting.getScannedDirs().add(stravaDir);
        }
        GeoMapStorage.librarySetting.setScanFolder(true);
    }

    private void refreshHomeIfPossible() {
        if (getOwner() instanceof org.hifly.bikedump.gui.Bikedump) {
            ((org.hifly.bikedump.gui.Bikedump) getOwner()).refreshLibraryAndReloadUI();
        }
    }

    private static long startOfDayUtc(LocalDate d) {
        return d.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

    private static long endOfDayUtc(LocalDate d) {
        return d.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) - 1;
    }

    // ---------------- Connect / Disconnect ----------------

    private void onConnect() {
        ensurePref();
        refreshStatus();

        StravaClient client = buildClientOrWarn();
        if (client == null) return;

        String state = Long.toHexString(System.nanoTime()) + Long.toHexString(Double.doubleToLongBits(Math.random()));

        StravaOAuthLocalServer local = new StravaOAuthLocalServer(pref.getRedirectHost(), pref.getCallbackPort());
        String redirectUri = local.getRedirectUri();

        String authUrl;
        try {
            authUrl = client.buildAuthorizeUrl(redirectUri, state);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Can't build authorize URL: " + ex.getMessage(), "Strava", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // run OAuth flow in background so UI doesn't freeze
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                // open browser first
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(authUrl));
                }
                // wait for callback
                StravaOAuthLocalServer.OAuthResult res = local.waitForCallback(OAUTH_TIMEOUT_MILLIS);
                if (res.error != null && !res.error.isBlank()) {
                    throw new RuntimeException("OAuth error: " + res.error);
                }
                if (res.code == null || res.code.isBlank()) {
                    throw new RuntimeException("Missing OAuth code");
                }
                if (res.state == null || !res.state.equals(state)) {
                    throw new RuntimeException("OAuth state mismatch");
                }

                // exchange code for tokens (updates pref)
                client.exchangeCodeForToken(pref, res.code);
                GeoMapStorage.save();
                return "Connected OK";
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    refreshStatus();
                    JOptionPane.showMessageDialog(StravaDialog.this, msg, "Strava", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    refreshStatus();
                    JOptionPane.showMessageDialog(StravaDialog.this, "Connect failed: " + ex.getMessage(), "Strava", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        JOptionPane.showMessageDialog(this,
                "A browser window will open.\nAfter login, Strava will redirect to:\n" + redirectUri
                        + "\n\nBikedump will wait up to " + (OAUTH_TIMEOUT_MILLIS / 1000) + " seconds.",
                "Strava Connect",
                JOptionPane.INFORMATION_MESSAGE);

        worker.execute();
    }

    private void onDisconnect() {
        ensurePref();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Disconnect from Strava?\n(This will clear tokens and reset sync marker.)",
                "Strava Disconnect",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) return;

        pref.setAccessToken(null);
        pref.setRefreshToken(null);
        pref.setExpiresAtEpochSeconds(0);
        pref.setLastSyncAfterEpochSeconds(0);
        GeoMapStorage.save();

        refreshStatus();
        JOptionPane.showMessageDialog(this, "Disconnected from Strava.", "Strava", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- Import ----------------

    private void onImportWithRange() {
        ensurePref();
        refreshStatus();

        if (!pref.isConnected()) {
            JOptionPane.showMessageDialog(this, "Not connected to Strava.", "Strava", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int max;
        LocalDate from;
        LocalDate to;
        try {
            max = Integer.parseInt(maxField.getText().trim());
            from = LocalDate.parse(fromField.getText().trim());
            to = LocalDate.parse(toField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Use:\nMax = number\nDates = YYYY-MM-DD", "Strava", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (max <= 0) {
            JOptionPane.showMessageDialog(this, "Max rides must be > 0", "Strava", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (to.isBefore(from)) {
            JOptionPane.showMessageDialog(this, "'To' must be >= 'From'", "Strava", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long after = startOfDayUtc(from);
        long before = endOfDayUtc(to);

        StravaClient client = buildClientOrWarn();
        if (client == null) return;

        StravaImporter importer = new StravaImporter(client);

        final JDialog working = new JDialog(this, "Strava import", false);
        working.setLayout(new BorderLayout());
        working.add(new JLabel("Importing rides... please wait."), BorderLayout.CENTER);
        working.setSize(320, 110);
        working.setLocationRelativeTo(this);
        working.setVisible(true);

        SwingWorker<StravaImporter.ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected StravaImporter.ImportResult doInBackground() throws Exception {
                return importer.importRidesBetweenWithLog(pref, max, after, before);
            }

            @Override
            protected void done() {
                working.dispose();
                try {
                    StravaImporter.ImportResult res = get();
                    if (res == null) {
                        JOptionPane.showMessageDialog(StravaDialog.this, "ImportResult is null (unexpected).", "Strava", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ensureStravaExportsFolderIsScanned();
                    GeoMapStorage.save();

                    JTextArea ta = new JTextArea(
                            "Imported rides: " + res.tracks.size()
                                    + "\nSaved in: " + GeoMapStorage.getStravaExportsDir()
                                    + "\nRange: " + from + " → " + to
                                    + "\n\nLog:\n" + res.log,
                            20, 70);
                    ta.setEditable(false);

                    JOptionPane.showMessageDialog(StravaDialog.this, new JScrollPane(ta), "Strava import result", JOptionPane.INFORMATION_MESSAGE);

                    if (!res.tracks.isEmpty()) {
                        refreshHomeIfPossible();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(StravaDialog.this, "Import failed: " + ex.getMessage(), "Strava", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}