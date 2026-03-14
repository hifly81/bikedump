package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.TimeUtility;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateDetailViewer extends JScrollPane {

    private static final long serialVersionUID = 21L;

    private final List<Track> tracks;
    private final JFrame currentFrame;

    public AggregateDetailViewer(List<Track> tracks, JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.tracks = tracks;
        this.currentFrame = currentFrame;

        setViewportView(buildUI());

        // Ensure current LAF applies to everything inside this scrollpane
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    private JComponent buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (tracks == null || tracks.isEmpty()) {
            root.add(new JLabel("No tracks loaded."));
            return root;
        }

        // ---------------- compute stats (kept from old code) ----------------
        double totalDistance = 0;
        double totalCalories = 0;
        double totalDeviceElevation = 0;
        double totalRealElevation = 0;
        double totalDeviceDescent = 0;
        double totalRealDescent = 0;
        double totalCalculatedSpeed = 0;
        double totalEffectiveSpeed = 0;
        double totalClimbingDistance = 0;
        double totalClimbingSpeed = 0;
        long totalRealTime = 0;
        long totalEffectiveTime = 0;
        long totalClimbingTime = 0;
        double maxAltitude = 0;
        double minAltitude = 100000;
        String trackNameMaxAltitude = null;
        String trackNameMinAltitude = null;

        Map<String, WaypointSegment> fastests = new HashMap<>(tracks.size());
        Map<String, WaypointSegment> slowests = new HashMap<>(tracks.size());
        Map<String, WaypointSegment> shortests = new HashMap<>(tracks.size());
        Map<String, WaypointSegment> longests = new HashMap<>(tracks.size());
        Map<String, WaypointSegment> lessElevateds = new HashMap<>(tracks.size());
        Map<String, WaypointSegment> mostElevateds = new HashMap<>(tracks.size());

        for (Track track : tracks) {
            totalDistance += GPSUtility.roundDoubleStat(track.getTotalDistance());
            totalCalories += track.getCalories();
            totalDeviceElevation += GPSUtility.roundDoubleStat(track.getCalculatedElevation());
            totalRealElevation += GPSUtility.roundDoubleStat(track.getRealElevation());
            totalDeviceDescent += GPSUtility.roundDoubleStat(track.getCalculatedDescent());
            totalRealDescent += GPSUtility.roundDoubleStat(track.getRealDescent());
            totalCalculatedSpeed += GPSUtility.roundDoubleStat(track.getCalculatedAvgSpeed());
            totalEffectiveSpeed += GPSUtility.roundDoubleStat(track.getEffectiveAvgSpeed());
            totalRealTime += track.getRealTime();
            totalEffectiveTime += track.getEffectiveTime();

            double maxAltitudeTemp = track.getMaxAltitude();
            double minAltitudeTemp = track.getMinAltitude();
            if (maxAltitudeTemp > maxAltitude) {
                maxAltitude = maxAltitudeTemp;
                trackNameMaxAltitude = track.getName();
            }
            if (minAltitudeTemp < minAltitude) {
                minAltitude = minAltitudeTemp;
                trackNameMinAltitude = track.getName();
            }

            totalClimbingDistance += track.getClimbingDistance();
            totalClimbingSpeed += track.getClimbingSpeed();
            totalClimbingTime += track.getClimbingTimeMillis();

            if (track.getStatsNewKm() != null) {
                String key = track.getName() == null ? track.getFileName() : track.getName();
                fastests.put(key, track.getStatsNewKm().get("Fastest"));
                slowests.put(key, track.getStatsNewKm().get("Slowest"));
                shortests.put(key, track.getStatsNewKm().get("Shortest"));
                longests.put(key, track.getStatsNewKm().get("Longest"));
                lessElevateds.put(key, track.getStatsNewKm().get("Less Elevated"));
                mostElevateds.put(key, track.getStatsNewKm().get("Most Elevated"));
            }
        }

        WaypointSegment fastest = pickFastest(fastests);
        String fastestTrack = pickTrackForSegment(fastests, fastest);

        WaypointSegment slowest = pickSlowest(slowests);
        String slowestTrack = pickTrackForSegment(slowests, slowest);

        WaypointSegment shortest = pickShortest(shortests);
        String shortestTrack = pickTrackForSegment(shortests, shortest);

        WaypointSegment longest = pickLongest(longests);
        String longestTrack = pickTrackForSegment(longests, longest);

        WaypointSegment mostElevated = pickMostElevated(mostElevateds);
        String mostElevatedTrack = pickTrackForSegment(mostElevateds, mostElevated);

        WaypointSegment lessElevated = pickLessElevated(lessElevateds);
        String lessElevatedTrack = pickTrackForSegment(lessElevateds, lessElevated);

        int n = tracks.size();

        // ---------------- UI boxes ----------------
        root.add(sectionTitle("Summary"));

        root.add(statBox("Tracks", rows(
                row("Number of tracks", String.valueOf(n))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Distance & Calories", rows(
                row("Total distance", GPSUtility.roundDoubleStat(totalDistance) + ""),
                row("Avg distance", GPSUtility.roundDoubleStat(totalDistance / n) + ""),
                row("Total calories", String.valueOf(totalCalories)),
                row("Avg calories", String.valueOf(GPSUtility.roundDoubleStat(totalCalories / n)))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Time", rows(
                row("Total duration", TimeUtility.toStringFromTimeDiff(totalRealTime)),
                row("Total effective duration", TimeUtility.toStringFromTimeDiff(totalEffectiveTime)),
                row("Avg duration", TimeUtility.toStringFromTimeDiff(totalRealTime / n)),
                row("Avg effective duration", TimeUtility.toStringFromTimeDiff(totalEffectiveTime / n))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Speed", rows(
                row("Avg calculated speed", String.valueOf(GPSUtility.roundDoubleStat(totalCalculatedSpeed / n))),
                row("Avg effective speed", String.valueOf(GPSUtility.roundDoubleStat(totalEffectiveSpeed / n)))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Elevation", rows(
                row("Total device elevation", String.valueOf(GPSUtility.roundDoubleStat(totalDeviceElevation))),
                row("Total real elevation", String.valueOf(GPSUtility.roundDoubleStat(totalRealElevation))),
                row("Total device descent", String.valueOf(GPSUtility.roundDoubleStat(totalDeviceDescent))),
                row("Total real descent", String.valueOf(GPSUtility.roundDoubleStat(totalRealDescent))),
                row("Avg device elevation", String.valueOf(GPSUtility.roundDoubleStat(totalDeviceElevation / n))),
                row("Avg real elevation", String.valueOf(GPSUtility.roundDoubleStat(totalRealElevation / n))),
                row("Avg device descent", String.valueOf(GPSUtility.roundDoubleStat(totalDeviceDescent / n))),
                row("Avg real descent", String.valueOf(GPSUtility.roundDoubleStat(totalRealDescent / n))),
                row("Max altitude", GPSUtility.roundDoubleStat(maxAltitude) + (trackNameMaxAltitude != null ? " (" + trackNameMaxAltitude + ")" : "")),
                row("Min altitude", GPSUtility.roundDoubleStat(minAltitude) + (trackNameMinAltitude != null ? " (" + trackNameMinAltitude + ")" : ""))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Climbing", rows(
                row("Total climbing distance", String.valueOf(GPSUtility.roundDoubleStat(totalClimbingDistance))),
                row("Total climbing time", TimeUtility.toStringFromTimeDiff(totalClimbingTime)),
                row("Avg climbing time", TimeUtility.toStringFromTimeDiff(totalClimbingTime / n)),
                row("Avg climbing speed", String.valueOf(GPSUtility.roundDoubleStat(totalClimbingSpeed / n)))
        )));
        root.add(Box.createVerticalStrut(8));

        root.add(statBox("Laps", rows(
                row("Fastest lap", formatLapSpeed(fastest, fastestTrack)),
                row("Slowest lap", formatLapSpeed(slowest, slowestTrack)),
                row("Shortest lap", formatLapTime(shortest, shortestTrack)),
                row("Longest lap", formatLapTime(longest, longestTrack)),
                row("Most elevated lap", formatLapElevation(mostElevated, mostElevatedTrack)),
                row("Less elevated lap", formatLapElevation(lessElevated, lessElevatedTrack))
        )));

        root.add(Box.createVerticalGlue());
        return root;
    }

    // ---------------- small UI helpers ----------------

    private static JComponent sectionTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel statBox(String title, JPanel content) {
        JPanel box = new JPanel(new BorderLayout());
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        Border outer = UIManager.getBorder("TitledBorder.border");
        Border titled = BorderFactory.createTitledBorder(outer, title);
        Border pad = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        box.setBorder(BorderFactory.createCompoundBorder(titled, pad));

        box.add(content, BorderLayout.CENTER);
        return box;
    }

    private static JPanel rows(JComponent... rows) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;

        for (JComponent r : rows) {
            c.gridx = 0;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(r, c);
            c.gridy++;
        }

        return p;
    }

    private static JComponent row(String label, String value) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = GridBagConstraints.WEST;

        JLabel l = new JLabel(label + ":");
        c.gridx = 0;
        c.weightx = 0;
        p.add(l, c);

        JLabel v = new JLabel(value == null ? "" : value);
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(v, c);

        return p;
    }

    // ---------------- lap helpers (kept simple) ----------------

    private static String formatLapSpeed(WaypointSegment s, String trackName) {
        if (s == null) return "";
        String t = trackName == null ? "" : " (" + trackName + ")";
        return s.getUnit() + " - " + GPSUtility.roundDoubleStat(s.getAvgSpeed()) + t;
    }

    private static String formatLapTime(WaypointSegment s, String trackName) {
        if (s == null) return "";
        String t = trackName == null ? "" : " (" + trackName + ")";
        return s.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(s.getTimeIncrement()) + t;
    }

    private static String formatLapElevation(WaypointSegment s, String trackName) {
        if (s == null) return "";
        String t = trackName == null ? "" : " (" + trackName + ")";
        return s.getUnit() + " - " + GPSUtility.roundDoubleStat(s.getEleGained()) + t;
    }

    private static WaypointSegment pickFastest(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getAvgSpeed() > best.getAvgSpeed()) best = s;
        }
        return best;
    }

    private static WaypointSegment pickSlowest(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getAvgSpeed() < best.getAvgSpeed()) best = s;
        }
        return best;
    }

    private static WaypointSegment pickShortest(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getTimeIncrement() < best.getTimeIncrement()) best = s;
        }
        return best;
    }

    private static WaypointSegment pickLongest(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getTimeIncrement() > best.getTimeIncrement()) best = s;
        }
        return best;
    }

    private static WaypointSegment pickMostElevated(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getEleGained() > best.getEleGained()) best = s;
        }
        return best;
    }

    private static WaypointSegment pickLessElevated(Map<String, WaypointSegment> map) {
        WaypointSegment best = null;
        for (WaypointSegment s : map.values()) {
            if (s == null) continue;
            if (best == null || s.getEleGained() < best.getEleGained()) best = s;
        }
        return best;
    }

    private static String pickTrackForSegment(Map<String, WaypointSegment> map, WaypointSegment seg) {
        if (seg == null) return null;
        for (Map.Entry<String, WaypointSegment> e : map.entrySet()) {
            if (e.getValue() == seg) return e.getKey();
        }
        return null;
    }
}