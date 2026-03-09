package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.graph.WaypointElevationGraph;
import org.hifly.bikedump.graph.WaypointGraph;
import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.gui.dialog.GraphViewer;
import org.hifly.bikedump.gui.panel.components.MetricCard;
import org.hifly.bikedump.gui.panel.components.SectionPanel;
import org.hifly.bikedump.gui.panel.components.UiSpacing;
import org.hifly.bikedump.storage.ClimbStorage;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.TimeUtility;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Component-based detail viewer (no HTML) with cards and collapsible sections.
// Keeps the existing getText4Report() output format intact for PDF reporting.
public class DetailViewer extends JScrollPane {

    private static final long serialVersionUID = 22L;

    private final Track track;
    private final JFrame currentFrame;

    private StringBuffer text4Report = new StringBuffer();

    private final String ELEMENT_SEPARATOR_REPORT = "$$$";
    private final String ELEMENT_SEPARATOR_SECTION_REPORT = "%%%";

    public DetailViewer(Track track, JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.track = track;
        this.currentFrame = currentFrame;

        createDetailViewer();
    }

    private void createDetailViewer() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(UiSpacing.pad(UiSpacing.GAP_MD));
        root.setOpaque(true);

        // --- Header ---
        JLabel title = new JLabel(track.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 4f));
        root.add(title);

        root.add(UiSpacing.vgap(6));

        String avgTemperatureSection = "Avg Temperature: " + track.getAvgTemperature();
        JLabel tempLabel = new JLabel(avgTemperatureSection);
        root.add(tempLabel);
        appendTextToReportBuffer(track.getName());
        appendTextToReportBuffer(avgTemperatureSection);

        String timeSection = TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getStartDate())
                + "   " + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getEndDate());
        JLabel timeLabel = new JLabel(timeSection);
        timeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        root.add(timeLabel);
        appendTextToReportBuffer(timeSection);

        root.add(UiSpacing.vgap(UiSpacing.GAP_MD));

        // --- Summary metrics grid ---
        JPanel metrics = new JPanel(new GridLayout(0, 2, UiSpacing.GAP_SM, UiSpacing.GAP_SM));
        metrics.setOpaque(false);

        String durationSection = "Duration: " + TimeUtility.toStringFromTimeDiff(track.getRealTime());
        String effectiveDurationSection = "Effective duration: " + TimeUtility.toStringFromTimeDiff(track.getEffectiveTime());
        String distanceSection = "Distance: " + GPSUtility.roundDoubleStat(track.getTotalDistance());
        String calculatedSpeedSection = "Calculated Speed: " + GPSUtility.roundDoubleStat(track.getCalculatedAvgSpeed());
        String effectiveSpeedSection = "Effective Speed: " + GPSUtility.roundDoubleStat(track.getEffectiveAvgSpeed());
        String maxSpeedSection = "Max Speed: " + GPSUtility.roundDoubleStat(track.getMaxSpeed());
        String caloriesSection = "Calories: " + track.getCalories();
        String avgHeartSection = "Avg Heart: " + GPSUtility.roundDoubleStat(track.getHeartFrequency());
        String maxHeartSection = "Max Heart: " + GPSUtility.roundDoubleStat(track.getHeartMax());

        metrics.add(new MetricCard("Duration", TimeUtility.toStringFromTimeDiff(track.getRealTime()), null));
        metrics.add(new MetricCard("Effective duration", TimeUtility.toStringFromTimeDiff(track.getEffectiveTime()), null));
        metrics.add(new MetricCard("Distance", GPSUtility.roundDoubleStat(track.getTotalDistance()), null));
        metrics.add(new MetricCard("Calculated speed", GPSUtility.roundDoubleStat(track.getCalculatedAvgSpeed()), null));
        metrics.add(new MetricCard("Effective speed", GPSUtility.roundDoubleStat(track.getEffectiveAvgSpeed()), null));
        metrics.add(new MetricCard("Max speed", GPSUtility.roundDoubleStat(track.getMaxSpeed()), null));
        metrics.add(new MetricCard("Calories", String.valueOf(track.getCalories()), null));
        metrics.add(new MetricCard("Heart", GPSUtility.roundDoubleStat(track.getHeartFrequency()),
                "Max: " + GPSUtility.roundDoubleStat(track.getHeartMax())));

        root.add(metrics);

        appendTextToReportBuffer(durationSection);
        appendTextToReportBuffer(effectiveDurationSection);
        appendTextToReportBuffer(distanceSection);
        appendTextToReportBuffer(calculatedSpeedSection);
        appendTextToReportBuffer(effectiveSpeedSection);
        appendTextToReportBuffer(maxSpeedSection);
        appendTextToReportBuffer(caloriesSection);
        appendTextToReportBuffer(avgHeartSection);
        appendTextToReportBuffer(maxHeartSection);

        root.add(UiSpacing.vgap(UiSpacing.GAP_LG));

        // --- Altimetric profile section ---
        JPanel altBody = new JPanel();
        altBody.setOpaque(false);
        altBody.setLayout(new BoxLayout(altBody, BoxLayout.Y_AXIS));

        JPanel altMetrics = new JPanel(new GridLayout(0, 2, UiSpacing.GAP_SM, UiSpacing.GAP_SM));
        altMetrics.setOpaque(false);

        String deviceElevationSection = "Device elevation: " + GPSUtility.roundDoubleStat(track.getCalculatedElevation());
        String realElevationSection = "Real elevation: " + GPSUtility.roundDoubleStat(track.getRealElevation());
        String deviceDescentSection = "Device descent: " + GPSUtility.roundDoubleStat(track.getCalculatedDescent());
        String realDescentSection = "Real descent: " + GPSUtility.roundDoubleStat(track.getRealDescent());
        String maxAltitudeSection = "Max altitude: " + GPSUtility.roundDoubleStat(track.getMaxAltitude());
        String minAltitudeSection = "Min altitude: " + GPSUtility.roundDoubleStat(track.getMinAltitude());
        String climbingDistanceSection = "Climbing distance: " + GPSUtility.roundDoubleStat(track.getClimbingDistance());
        String climbingTimeSection = "Climbing time: " + TimeUtility.toStringFromTimeDiff(track.getClimbingTimeMillis());
        String climbingSpeedSection = "Climbing speed: " + GPSUtility.roundDoubleStat(track.getClimbingSpeed());

        altMetrics.add(new MetricCard("Device elevation", GPSUtility.roundDoubleStat(track.getCalculatedElevation()), null));
        altMetrics.add(new MetricCard("Real elevation", GPSUtility.roundDoubleStat(track.getRealElevation()), null));
        altMetrics.add(new MetricCard("Device descent", GPSUtility.roundDoubleStat(track.getCalculatedDescent()), null));
        altMetrics.add(new MetricCard("Real descent", GPSUtility.roundDoubleStat(track.getRealDescent()), null));
        altMetrics.add(new MetricCard("Max altitude", GPSUtility.roundDoubleStat(track.getMaxAltitude()), null));
        altMetrics.add(new MetricCard("Min altitude", GPSUtility.roundDoubleStat(track.getMinAltitude()), null));
        altMetrics.add(new MetricCard("Climbing distance", GPSUtility.roundDoubleStat(track.getClimbingDistance()), null));
        altMetrics.add(new MetricCard("Climbing time", TimeUtility.toStringFromTimeDiff(track.getClimbingTimeMillis()), null));
        altMetrics.add(new MetricCard("Climbing speed", GPSUtility.roundDoubleStat(track.getClimbingSpeed()), null));

        altBody.add(altMetrics);

        root.add(new SectionPanel("Altimetric Profile", altBody, true));

        appendTextToReportBuffer(deviceElevationSection);
        appendTextToReportBuffer(realElevationSection);
        appendTextToReportBuffer(deviceDescentSection);
        appendTextToReportBuffer(realDescentSection);
        appendTextToReportBuffer(maxAltitudeSection);
        appendTextToReportBuffer(minAltitudeSection);
        appendTextToReportBuffer(climbingDistanceSection);
        appendTextToReportBuffer(climbingTimeSection);
        appendTextToReportBuffer(climbingSpeedSection);

        root.add(UiSpacing.vgap(UiSpacing.GAP_LG));

        // --- Climbs section ---
        String slopesSize = track.getSlopes() != null ? String.valueOf(track.getSlopes().size()) : "0";
        String climbsSection = "Climbs (" + slopesSize + " )";
        appendTextToReportBuffer(climbsSection);

        JPanel climbsBody = new JPanel();
        climbsBody.setOpaque(false);
        climbsBody.setLayout(new BoxLayout(climbsBody, BoxLayout.Y_AXIS));

        long totalSlopeDuration = 0;
        double totalSlopeElevation = 0;
        double totalSlopeDistance = 0;
        double totalSlopeGradient = 0;
        double totalAvgSpeed = 0;
        double totalPower = 0;
        double totalVam = 0;

        if (track.getSlopes() != null && !track.getSlopes().isEmpty()) {
            List<SlopeSegment> listSlopes = new ArrayList<>();

            for (int z = 0; z < track.getSlopes().size(); z++) {
                SlopeSegment slope = track.getSlopes().get(z);
                listSlopes.add(slope);

                JPanel climbRow = new JPanel(new BorderLayout());
                climbRow.setOpaque(false);
                climbRow.setBorder(UiSpacing.pad(0, 0, UiSpacing.GAP_SM, 0));

                JLabel climbTitle = new JLabel("Climb " + (z + 1)
                        + (slope.getName() != null && !slope.getName().isBlank() ? " - " + slope.getName() : ""));
                climbTitle.setFont(climbTitle.getFont().deriveFont(Font.BOLD));
                climbRow.add(climbTitle, BorderLayout.NORTH);

                JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, UiSpacing.GAP_SM, 0));
                rowButtons.setOpaque(false);

                final int slopeIndex = z;

                JButton viewDetail = new JButton("View climb detail");
                viewDetail.addActionListener(e -> viewClimbDetail(listSlopes, slopeIndex));

                JButton saveClimb = new JButton("Save climb");
                saveClimb.addActionListener(e -> saveClimb(listSlopes, slopeIndex));

                rowButtons.add(viewDetail);
                rowButtons.add(saveClimb);

                climbRow.add(rowButtons, BorderLayout.CENTER);

                JPanel climbMetrics = new JPanel(new GridLayout(0, 2, UiSpacing.GAP_SM, UiSpacing.GAP_SM));
                climbMetrics.setOpaque(false);

                String climbDistanceSection = "Distance: " + GPSUtility.roundDoubleStat(slope.getDistance());
                String climbStartDistanceSection = "Start km: " + GPSUtility.roundDoubleStat(slope.getStartDistance());
                String climbEndDistanceSection = "End km: " + GPSUtility.roundDoubleStat(slope.getEndDistance());

                String climbDurationSection;
                if (slope.getEndDate() != null) {
                    climbDurationSection = "Duration: " + TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime());
                } else {
                    climbDurationSection = "Duration: ";
                }

                String climbElevationSection = "Elevation: " + GPSUtility.roundDoubleStat(slope.getElevation());
                String climbGradientSection = "Gradient: " + GPSUtility.roundDoubleStat(slope.getGradient()) + " %";
                String climbStartElevationSection = "Start elevation: " + GPSUtility.roundDoubleStat(slope.getStartElevation());
                String climbEndElevationSection = "End elevation: " + GPSUtility.roundDoubleStat(slope.getEndElevation());
                String climbAvgSpeedSection = "Avg speed: " + GPSUtility.roundDoubleStat(slope.getAvgSpeed());
                String climbPowerSection = "Power: " + GPSUtility.roundDoubleStat(slope.getPower());
                String climbVamSection = "VAM: " + GPSUtility.roundDoubleStat(slope.getVam());

                climbMetrics.add(new MetricCard("Distance", GPSUtility.roundDoubleStat(slope.getDistance()), null));
                climbMetrics.add(new MetricCard("Gradient", GPSUtility.roundDoubleStat(slope.getGradient()) + " %", null));
                climbMetrics.add(new MetricCard("Elevation", GPSUtility.roundDoubleStat(slope.getElevation()), null));
                climbMetrics.add(new MetricCard("Avg speed", GPSUtility.roundDoubleStat(slope.getAvgSpeed()), null));
                climbMetrics.add(new MetricCard("Power", GPSUtility.roundDoubleStat(slope.getPower()), null));
                climbMetrics.add(new MetricCard("VAM", GPSUtility.roundDoubleStat(slope.getVam()), null));
                climbMetrics.add(new MetricCard("Start km", GPSUtility.roundDoubleStat(slope.getStartDistance()), null));
                climbMetrics.add(new MetricCard("End km", GPSUtility.roundDoubleStat(slope.getEndDistance()), null));
                climbMetrics.add(new MetricCard("Start elevation", GPSUtility.roundDoubleStat(slope.getStartElevation()), null));
                climbMetrics.add(new MetricCard("End elevation", GPSUtility.roundDoubleStat(slope.getEndElevation()), null));
                climbMetrics.add(new MetricCard("Duration", slope.getEndDate() != null
                        ? TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime())
                        : "", null));

                climbsBody.add(climbRow);
                climbsBody.add(climbMetrics);
                climbsBody.add(UiSpacing.vgap(UiSpacing.GAP_MD));

                appendTextToReportBuffer(climbDistanceSection);
                appendTextToReportBuffer(climbStartDistanceSection);
                appendTextToReportBuffer(climbEndDistanceSection);
                appendTextToReportBuffer(climbDurationSection);
                appendTextToReportBuffer(climbElevationSection);
                appendTextToReportBuffer(climbGradientSection);
                appendTextToReportBuffer(climbStartElevationSection);
                appendTextToReportBuffer(climbEndElevationSection);
                appendTextToReportBuffer(climbAvgSpeedSection);
                appendTextToReportBuffer(climbPowerSection);
                appendTextToReportBuffer(climbVamSection);
                appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

                totalSlopeDistance += slope.getDistance();
                totalSlopeElevation += slope.getElevation();
                totalSlopeGradient += slope.getGradient();
                totalAvgSpeed += slope.getAvgSpeed();
                totalPower += slope.getPower();
                totalVam += slope.getVam();
                if (slope.getEndDate() != null) {
                    totalSlopeDuration += (slope.getEndDate().getTime() - slope.getStartDate().getTime());
                }
            }

            if (track.getSlopes().size() > 0) {
                JPanel totals = new JPanel(new GridLayout(0, 2, UiSpacing.GAP_SM, UiSpacing.GAP_SM));
                totals.setOpaque(false);

                totals.add(new MetricCard("Total distance", GPSUtility.roundDoubleStat(totalSlopeDistance), null));
                totals.add(new MetricCard("Total elevation", GPSUtility.roundDoubleStat(totalSlopeElevation), null));
                totals.add(new MetricCard("Total duration", TimeUtility.toStringFromTimeDiff(totalSlopeDuration), null));
                totals.add(new MetricCard("Avg distance", GPSUtility.roundDoubleStat(totalSlopeDistance / track.getSlopes().size()), null));
                totals.add(new MetricCard("Avg elevation", GPSUtility.roundDoubleStat(totalSlopeElevation / track.getSlopes().size()), null));
                totals.add(new MetricCard("Avg gradient", GPSUtility.roundDoubleStat(totalSlopeGradient / track.getSlopes().size()), null));
                totals.add(new MetricCard("Avg speed", GPSUtility.roundDoubleStat(totalAvgSpeed / track.getSlopes().size()), null));
                totals.add(new MetricCard("Avg power", GPSUtility.roundDoubleStat(totalPower / track.getSlopes().size()), null));
                totals.add(new MetricCard("Avg VAM", GPSUtility.roundDoubleStat(totalVam / track.getSlopes().size()), null));

                climbsBody.add(new JLabel("Totals"));
                climbsBody.add(UiSpacing.vgap(6));
                climbsBody.add(totals);

                // report text, same as legacy
                String totalClimbDistanceSection = "Total distance: " + GPSUtility.roundDoubleStat(totalSlopeDistance);
                String totalClimbElevationSection = "Total elevation: " + GPSUtility.roundDoubleStat(totalSlopeElevation);
                String totalClimbDurationSection = "Total duration: " + TimeUtility.toStringFromTimeDiff(totalSlopeDuration);
                String totalClimbAvgSection = "Avg distance: " + GPSUtility.roundDoubleStat(totalSlopeDistance / track.getSlopes().size());
                String totalClimbAvgElevationSection = "Avg elevation: " + GPSUtility.roundDoubleStat(totalSlopeElevation / track.getSlopes().size());
                // Keep legacy behavior (original code used totalSlopeDuration / 2)
                String totalClimbAvgDurationSection = "Avg duration: " + TimeUtility.toStringFromTimeDiff(totalSlopeDuration / 2);
                String totalClimbAvgGradient = "Avg gradient: " + GPSUtility.roundDoubleStat(totalSlopeGradient / track.getSlopes().size());
                String totalClimbAvgSpeedSection = "Avg speed: " + GPSUtility.roundDoubleStat(totalAvgSpeed / track.getSlopes().size());
                String totalClimbAvgPowerSection = "Avg power: " + GPSUtility.roundDoubleStat(totalPower / track.getSlopes().size());
                String totalClimbAvgVamSection = "Avg vam: " + GPSUtility.roundDoubleStat(totalVam / track.getSlopes().size());

                appendTextToReportBuffer(totalClimbDistanceSection);
                appendTextToReportBuffer(totalClimbElevationSection);
                appendTextToReportBuffer(totalClimbDurationSection);
                appendTextToReportBuffer(totalClimbAvgSection);
                appendTextToReportBuffer(totalClimbAvgElevationSection);
                appendTextToReportBuffer(totalClimbAvgDurationSection);
                appendTextToReportBuffer(totalClimbAvgGradient);
                appendTextToReportBuffer(totalClimbAvgSpeedSection);
                appendTextToReportBuffer(totalClimbAvgPowerSection);
                appendTextToReportBuffer(totalClimbAvgVamSection);
            }
        }

        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
        root.add(new SectionPanel(climbsSection, climbsBody, false));

        root.add(UiSpacing.vgap(UiSpacing.GAP_LG));

        // --- Lap section ---
        JPanel lapsBody = new JPanel();
        lapsBody.setOpaque(false);
        lapsBody.setLayout(new BoxLayout(lapsBody, BoxLayout.Y_AXIS));

        String lapsDetailSection = "Details for lap";
        WaypointSegment fastest = track.getStatsNewKm().get("Fastest");
        WaypointSegment slowest = track.getStatsNewKm().get("Slowest");
        WaypointSegment shortest = track.getStatsNewKm().get("Shortest");
        WaypointSegment longest = track.getStatsNewKm().get("Longest");
        WaypointSegment lessElevated = track.getStatsNewKm().get("Less Elevated");
        WaypointSegment mostElevated = track.getStatsNewKm().get("Most Elevated");

        addLapStatLine(lapsBody, "Fastest Lap: ", fastest != null ? fastest.getUnit() + " - " + GPSUtility.roundDoubleStat(fastest.getAvgSpeed()) : "");
        addLapStatLine(lapsBody, "Slowest Lap: ", slowest != null ? slowest.getUnit() + " - " + GPSUtility.roundDoubleStat(slowest.getAvgSpeed()) : "");
        addLapStatLine(lapsBody, "Shortest Lap: ", shortest != null ? shortest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement()) : "");
        addLapStatLine(lapsBody, "Longest Lap: ", longest != null ? longest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement()) : "");

        if (mostElevated != null) {
            addLapStatLine(lapsBody, "Most elevated Lap: ", mostElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(mostElevated.getEleGained()));
            addLapStatLine(lapsBody, "Less elevated Lap: ", lessElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(lessElevated.getEleGained()));

            JLabel detailLabel = new JLabel(lapsDetailSection);
            detailLabel.setFont(detailLabel.getFont().deriveFont(Font.BOLD));
            lapsBody.add(UiSpacing.vgap(UiSpacing.GAP_MD));
            lapsBody.add(detailLabel);

            appendTextToReportBuffer(lapsDetailSection);
        }

        int km = 1;
        for (Map.Entry<String, WaypointSegment> entry : track.getCoordinatesNewKm().entrySet()) {
            WaypointSegment seg = entry.getValue();

            JPanel lapCard = new JPanel(new GridLayout(0, 2, UiSpacing.GAP_SM, UiSpacing.GAP_SM));
            lapCard.setOpaque(false);

            JLabel lapTitle = new JLabel(km + ")");
            lapTitle.setFont(lapTitle.getFont().deriveFont(Font.BOLD));
            lapsBody.add(UiSpacing.vgap(UiSpacing.GAP_SM));
            lapsBody.add(lapTitle);

            String lapTimeRelevationSection = "Time relevation: " + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", seg.getTimeSpent());
            String lapTimeSection = "Time lap: " + TimeUtility.toStringFromTimeDiff(seg.getTimeIncrement());
            String lapAvgSpeedSection = "Avg speed: " + GPSUtility.roundDoubleStat(seg.getAvgSpeed());
            String elevationLine = "Elevation gained: " + GPSUtility.roundDoubleStat(seg.getEleGained());
            String heartSection = "Min heart: " + GPSUtility.roundDoubleStat(seg.getMinHeart()) + " - " + "Max heart:" + GPSUtility.roundDoubleStat(seg.getMaxHeart());

            lapCard.add(new MetricCard("Time relevation", TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", seg.getTimeSpent()), null));
            lapCard.add(new MetricCard("Time lap", TimeUtility.toStringFromTimeDiff(seg.getTimeIncrement()), null));
            lapCard.add(new MetricCard("Avg speed", GPSUtility.roundDoubleStat(seg.getAvgSpeed()), null));
            lapCard.add(new MetricCard("Elevation gained", GPSUtility.roundDoubleStat(seg.getEleGained()), "m"));
            lapCard.add(new MetricCard("Heart", GPSUtility.roundDoubleStat(seg.getMinHeart()),
                    "Max: " + GPSUtility.roundDoubleStat(seg.getMaxHeart())));

            lapsBody.add(lapCard);

            appendTextToReportBuffer(km + ")");
            appendTextToReportBuffer(lapTimeRelevationSection);
            appendTextToReportBuffer(lapTimeSection);
            appendTextToReportBuffer(lapAvgSpeedSection);
            appendTextToReportBuffer(elevationLine);
            appendTextToReportBuffer(heartSection);
            appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

            km++;
        }

        root.add(new SectionPanel("Lap details", lapsBody, false));

        setViewportView(root);
        revalidate();
        repaint();
    }

    private void viewClimbDetail(List<SlopeSegment> slopes, int slopeIndex) {
        if (slopes == null || slopeIndex < 0 || slopeIndex >= slopes.size()) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "Invalid climb index: " + slopeIndex,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SlopeSegment slope = slopes.get(slopeIndex);
        List<Waypoint> slopeWaypoints = slope.getWaypoints();

        WaypointGraph waypointElevationGraph =
                new WaypointElevationGraph(slopeWaypoints, true, false, true);
        new GraphViewer(currentFrame, Arrays.asList(waypointElevationGraph));
    }

    private void saveClimb(List<SlopeSegment> slopes, int slopeIndex) {
        if (slopes == null || slopeIndex < 0 || slopeIndex >= slopes.size()) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "Invalid climb index: " + slopeIndex,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SlopeSegment slope = slopes.get(slopeIndex);

        String climbName = (String) JOptionPane.showInputDialog(
                currentFrame,
                "Name for the climb",
                "Save Climb",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                slope.getName() != null ? slope.getName() : ""
        );

        if (climbName == null) {
            // user cancelled
            return;
        }

        climbName = climbName.trim();
        if (climbName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "Climb name cannot be empty.",
                    "Save Climb",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // same behavior as LinkAdapter
        slope.setName(climbName);
        ClimbStorage.saveClimb(slope, climbName);

        // refresh menu (same hack as LinkAdapter)
        try {
            if (currentFrame instanceof Bikedump bikedump && bikedump.topMenu != null) {
                bikedump.topMenu.getClimbs().validate();
                bikedump.topMenu.getClimbs().repaint();
            }
        } catch (Exception ignored) {
        }

        JOptionPane.showMessageDialog(
                currentFrame,
                "Saved climb: " + climbName,
                "Save Climb",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addLapStatLine(JPanel parent, String label, String value) {
        String line = label + value;
        JLabel l = new JLabel(line);
        parent.add(l);
        appendTextToReportBuffer(line);
    }

    private void appendTextToReportBuffer(String text) {
        text4Report.append(text);
        text4Report.append(ELEMENT_SEPARATOR_REPORT);
    }

    public String getText4Report() {
        return text4Report.toString();
    }
}