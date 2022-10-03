package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.events.LinkAdapter;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.TimeUtility;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;


//TODO use a templating manager e.g freemarker
public class DetailViewer extends JScrollPane {

    private static final long serialVersionUID = 22L;

    private Track track;
    private JFrame currentFrame;
    private StringBuffer text4HTML = new StringBuffer();
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
        HTMLEditorPanel textPane = new HTMLEditorPanel();
        LinkAdapter handler = new LinkAdapter(currentFrame);
        textPane.addMouseListener(handler);


        String imgMountain = null;
        String imgSave = null;
        String imgSun = null;
        String imgSunset = null;
        try {
            URL imgMountainUrl = getClass().getResource("/img/mountain.png");
            imgMountain = imgMountainUrl.toExternalForm();

            URL imgSaveUrl = getClass().getResource("/img/save.png");
            imgSave = imgSaveUrl.toExternalForm();

            URL imgSunUrl = getClass().getResource("/img/sun.png");
            imgSun = imgSunUrl.toExternalForm();

            URL imgSunsetUrl = getClass().getResource("/img/sunset.png");
            imgSunset = imgSunsetUrl.toExternalForm();


        } catch (Exception e) {
            e.printStackTrace();
        }


        appendTextToBuffer("<p><b>" + track.getName() + "</b><br>");
        appendTextToReportBuffer(track.getName());
        String sportTypeSection = "Sport type:" + (track.getSportType() == null?"":track.getSportType());
        appendTextToBuffer(sportTypeSection + "<br>");
        appendTextToReportBuffer(sportTypeSection);
        String avgTemperatureSection = "Avg Temperature:" + track.getAvgTemperature();
        appendTextToBuffer(avgTemperatureSection + "<br>");
        appendTextToReportBuffer(avgTemperatureSection);
        String timeSection = TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getStartDate()) + "&nbsp; &nbsp;" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getEndDate());
        appendTextToBuffer(timeSection + "<br>");
        appendTextToReportBuffer(timeSection);
        Map.Entry<String, String> sunTime = TimeUtility.getSunriseSunsetTime(
                track.getCoordinates().get(0).getDecimalLatitude(),
                track.getCoordinates().get(0).getDecimalLongitude(),
                track.getStartDate());
        if (sunTime != null) {
            //write
            textPane.append(null, flushBuffer());
            textPane.addImg(imgSun, sunTime.getKey());
            textPane.addImg(imgSunset, sunTime.getValue());
        }
        String durationSection = "Duration:" + TimeUtility.toStringFromTimeDiff(track.getRealTime());
        appendTextToBuffer(durationSection + "<br>");
        appendTextToReportBuffer(durationSection);
        String effectiveDurationSection = "Effective duration:" + TimeUtility.toStringFromTimeDiff(track.getEffectiveTime());
        appendTextToBuffer(effectiveDurationSection + "<br>");
        appendTextToReportBuffer(effectiveDurationSection);
        String distanceSection = "Distance:" + GPSUtility.roundDoubleStat(track.getTotalDistance());
        appendTextToBuffer(distanceSection + "<br>");
        appendTextToReportBuffer(distanceSection);
        String calculatedSpeedSection = "Calculated Speed:" + GPSUtility.roundDoubleStat(track.getCalculatedAvgSpeed());
        appendTextToBuffer(calculatedSpeedSection + "<br>");
        appendTextToReportBuffer(calculatedSpeedSection);
        String effectiveSpeedSection = "Effective Speed:" + GPSUtility.roundDoubleStat(track.getEffectiveAvgSpeed());
        appendTextToBuffer(effectiveSpeedSection + "<br>");
        appendTextToReportBuffer(effectiveSpeedSection);
        String maxSpeedSection = "Max Speed:" + GPSUtility.roundDoubleStat(track.getMaxSpeed());
        appendTextToBuffer("Max Speed:" + GPSUtility.roundDoubleStat(track.getMaxSpeed()) + "<br>");
        appendTextToReportBuffer(maxSpeedSection);
        String caloriesSection = "Calories:" + track.getCalories();
        appendTextToBuffer(caloriesSection + "<br>");
        appendTextToReportBuffer(caloriesSection);
        String avgHeartSection = "Avg Heart:" + GPSUtility.roundDoubleStat(track.getHeartFrequency());
        appendTextToBuffer("Avg Heart:" + GPSUtility.roundDoubleStat(track.getHeartFrequency()) + "<br>");
        appendTextToReportBuffer(avgHeartSection);
        String maxHeartSection = "Max Heart:" + GPSUtility.roundDoubleStat(track.getHeartMax());
        appendTextToBuffer("Max Heart:" + GPSUtility.roundDoubleStat(track.getHeartMax()) + "<br>");
        appendTextToReportBuffer(maxHeartSection);


        appendTextToBuffer("<hr>");

        appendTextToBuffer("<p><b>Altimetic Profile</b></p>");
        String deviceElevationSection = "Device elevation:" + GPSUtility.roundDoubleStat(track.getCalculatedElevation());
        appendTextToBuffer(deviceElevationSection + "<br>");
        appendTextToReportBuffer(deviceElevationSection);
        String realElevationSection = "Real elevation:" + GPSUtility.roundDoubleStat(track.getRealElevation());
        appendTextToBuffer(realElevationSection + "<br>");
        appendTextToReportBuffer(realElevationSection);
        String deviceDescentSection = "Device descent:" + GPSUtility.roundDoubleStat(track.getCalculatedDescent());
        appendTextToBuffer(deviceDescentSection + "<br>");
        appendTextToReportBuffer(deviceDescentSection);
        String realDescentSection = "Real descent:" + GPSUtility.roundDoubleStat(track.getRealDescent());
        appendTextToBuffer(realDescentSection + "<br>");
        appendTextToReportBuffer(realDescentSection);
        String maxAltitudeSection = "Max altitude:" + GPSUtility.roundDoubleStat(track.getMaxAltitude());
        appendTextToBuffer(maxAltitudeSection + "<br>");
        appendTextToReportBuffer(maxAltitudeSection);
        String minAltitudeSection = "Min altitude:" + GPSUtility.roundDoubleStat(track.getMinAltitude());
        appendTextToBuffer(minAltitudeSection + "<br>");
        appendTextToReportBuffer(minAltitudeSection);
        String climbingDistanceSection = "Climbing distance:" + GPSUtility.roundDoubleStat(track.getClimbingDistance());
        appendTextToBuffer(climbingDistanceSection + "<br>");
        appendTextToReportBuffer(climbingDistanceSection);
        String climbingTimeSection = "Climbing time:" + TimeUtility.toStringFromTimeDiff(track.getClimbingTimeMillis());
        appendTextToBuffer(climbingTimeSection + "<br>");
        appendTextToReportBuffer(climbingTimeSection);
        String climbingSpeedSection = "Climbing speed:" + GPSUtility.roundDoubleStat(track.getClimbingSpeed());
        appendTextToBuffer(climbingSpeedSection + "<br>");
        appendTextToReportBuffer(climbingSpeedSection);

        textPane.append(null, flushBuffer());
        try {
            textPane.addHyperlinkImg(
                    new URL("http://bikedump.com?climbProfile=" + track.getFileName()), imgMountain, "View climb profile", Color.BLUE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        handler.setAltimetricProfile(track.getAltimetricProfile());

        String slopesSize = track.getSlopes() != null ? String.valueOf(track.getSlopes().size()) : "0";
        String climbsSection = "Climbs (" + slopesSize + " )";
        appendTextToBuffer("<p><b>" + climbsSection + "</b><br><br>");
        appendTextToReportBuffer(climbsSection);

        textPane.append(null, flushBuffer());

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
                try {
                    //TODO change URL format and include img;
                    textPane.addHyperlinkImg(
                            new URL("http://bikedump.com?slopeIndex=" + z), imgMountain, "View climb detail", Color.BLUE);
                    textPane.addHyperlinkImg(
                            new URL("http://bikedump.com/save/?slopeIndex=" + z), imgSave, "Save climb", Color.BLUE);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String climbDistanceSection = "Distance:" + GPSUtility.roundDoubleStat(slope.getDistance());
                appendTextToBuffer(climbDistanceSection + "<br>");
                appendTextToReportBuffer(climbDistanceSection);
                String climbStartDistanceSection = "Start km:" + GPSUtility.roundDoubleStat(slope.getStartDistance());
                appendTextToBuffer(climbStartDistanceSection + "<br>");
                appendTextToReportBuffer(climbStartDistanceSection);
                String climbEndDistanceSection = "End km:" + GPSUtility.roundDoubleStat(slope.getEndDistance());
                appendTextToBuffer(climbEndDistanceSection + "<br>");
                appendTextToReportBuffer(climbEndDistanceSection);
                if (slope.getEndDate() != null) {
                    String climbDurationSection = "Duration:" + TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime());
                    appendTextToBuffer(climbDurationSection + "<br>");
                    appendTextToReportBuffer(climbDurationSection);
                } else {
                    String climbDurationSection = "Duration:";
                    appendTextToBuffer(climbDurationSection + "<br>");
                    appendTextToReportBuffer(climbDurationSection);
                }

                String climbElevationSection = "Elevation:" + GPSUtility.roundDoubleStat(slope.getElevation());
                appendTextToBuffer(climbElevationSection + "<br>");
                appendTextToReportBuffer(climbElevationSection);
                String climbGradientSection = "Gradient:" + GPSUtility.roundDoubleStat(slope.getGradient()) + " %";
                appendTextToBuffer(climbGradientSection + "<br>");
                appendTextToReportBuffer(climbGradientSection);
                String climbStartElevationSection = "Start elevation:" + GPSUtility.roundDoubleStat(slope.getStartElevation());
                appendTextToBuffer(climbStartElevationSection + "<br>");
                appendTextToReportBuffer(climbStartElevationSection);
                String climbEndElevationSection = "End elevation:" + GPSUtility.roundDoubleStat(slope.getEndElevation());
                appendTextToBuffer(climbEndElevationSection + "<br>");
                appendTextToReportBuffer(climbEndElevationSection);
                String climbAvgSpeedSection = "Avg speed:" + GPSUtility.roundDoubleStat(slope.getAvgSpeed());
                appendTextToBuffer(climbAvgSpeedSection + "<br>");
                appendTextToReportBuffer(climbAvgSpeedSection);
                String climbPowerSection = "Power:" + GPSUtility.roundDoubleStat(slope.getPower());
                appendTextToBuffer(climbPowerSection + "<br>");
                appendTextToReportBuffer(climbPowerSection);
                String climbVamSection = "VAM:" + GPSUtility.roundDoubleStat(slope.getVam());
                appendTextToBuffer(climbVamSection + "<br>");
                appendTextToReportBuffer(climbVamSection);
                appendTextToBuffer("<br>");
                appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

                //write
                textPane.append(null, flushBuffer());

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
            handler.setSlopes(listSlopes);

            if (track.getSlopes().size() > 0) {
                String totalClimbDistanceSection = "Total distance:" + GPSUtility.roundDoubleStat(totalSlopeDistance);
                appendTextToBuffer(totalClimbDistanceSection + "<br>");
                appendTextToReportBuffer(totalClimbDistanceSection);
                String totalClimbElevationSection = "Total elevation:" + GPSUtility.roundDoubleStat(totalSlopeElevation);
                appendTextToBuffer(totalClimbElevationSection + "<br>");
                appendTextToReportBuffer(totalClimbElevationSection);
                String totalClimbDurationSection = "Total duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration);
                appendTextToBuffer(totalClimbDurationSection + "<br>");
                appendTextToReportBuffer(totalClimbDurationSection);
                String totalClimbAvgSection = "Avg distance:" + GPSUtility.roundDoubleStat(totalSlopeDistance / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgSection);
                String totalClimbAvgElevationSection = "Avg elevation:" + GPSUtility.roundDoubleStat(totalSlopeElevation / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgElevationSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgElevationSection);
                String totalClimbAvgDurationSection = "Avg duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration / 2);
                appendTextToBuffer(totalClimbAvgDurationSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgDurationSection);
                String totalClimbAvgGradient = "Avg gradient:" + GPSUtility.roundDoubleStat(totalSlopeGradient / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgGradient + "<br>");
                appendTextToReportBuffer(totalClimbAvgGradient);
                String totalClimbAvgSpeedSection = "Avg speed:" + GPSUtility.roundDoubleStat(totalAvgSpeed / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgSpeedSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgSpeedSection);
                String totalClimbAvgPowerSection = "Avg power:" + GPSUtility.roundDoubleStat(totalPower / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgPowerSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgPowerSection);
                String totalClimbAvgVamSection = "Avg vam:" + GPSUtility.roundDoubleStat(totalVam / track.getSlopes().size());
                appendTextToBuffer(totalClimbAvgVamSection + "<br>");
                appendTextToReportBuffer(totalClimbAvgVamSection);
            }
        }


        appendTextToBuffer("</p></p><hr>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

        String lapsDetailSection = "Details for lap";
        WaypointSegment fastest = track.getStatsNewKm().get("Fastest");
        WaypointSegment slowest = track.getStatsNewKm().get("Slowest");
        WaypointSegment shortest = track.getStatsNewKm().get("Shortest");
        WaypointSegment longest = track.getStatsNewKm().get("Longest");
        WaypointSegment lessElevated = track.getStatsNewKm().get("Less Elevated");
        WaypointSegment mostElevated = track.getStatsNewKm().get("Most Elevated");
        if (fastest != null) {
            String fastestLapSection = "Fastest Lap:" + fastest.getUnit() + " - " + GPSUtility.roundDoubleStat(fastest.getAvgSpeed());
            appendTextToBuffer(fastestLapSection + "<br>");
            appendTextToReportBuffer(fastestLapSection);
        } else {
            String fastestLapSection = "Fastest Lap:";
            appendTextToBuffer(fastestLapSection + "<br>");
            appendTextToReportBuffer(fastestLapSection);
        }
        if (slowest != null) {
            String slowestLapSection = "Slowest Lap:" + slowest.getUnit() + " - " + GPSUtility.roundDoubleStat(slowest.getAvgSpeed());
            appendTextToBuffer(slowestLapSection + "<br>");
            appendTextToReportBuffer(slowestLapSection);
        } else {
            String slowestLapSection = "Slowest Lap:";
            appendTextToBuffer(slowestLapSection + "<br>");
            appendTextToReportBuffer(slowestLapSection);
        }
        if (shortest != null) {
            String shortestLapSection = "Shortest Lap:" + shortest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement());
            appendTextToBuffer(shortestLapSection + "<br>");
            appendTextToReportBuffer(shortestLapSection);
        } else {
            String shortestLapSection = "Shortest Lap:";
            appendTextToBuffer(shortestLapSection + "<br>");
            appendTextToReportBuffer(shortestLapSection);
        }
        if (longest != null) {
            String longestLapSection = "Longest Lap:" + longest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement());
            appendTextToBuffer(longestLapSection + "<br>");
            appendTextToReportBuffer(longestLapSection);
        } else {
            String longestLapSection = "Longest Lap:";
            appendTextToBuffer(longestLapSection + "Longest Lap:<br>");
            appendTextToReportBuffer(longestLapSection);
        }

        if(mostElevated != null) {
            String mostElevatedLapSection = "Most elevated Lap:" + mostElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(mostElevated.getEleGained());
            appendTextToBuffer(mostElevatedLapSection + "<br>");
            appendTextToReportBuffer(mostElevatedLapSection);
            String lessElevatedLap = "Less elevated Lap:" + lessElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(lessElevated.getEleGained());
            appendTextToBuffer(lessElevatedLap + "<br>");
            appendTextToReportBuffer(lessElevatedLap);
            appendTextToBuffer("<br>");
            appendTextToBuffer("<p><b>" + lapsDetailSection + "</b><br>");
            appendTextToReportBuffer(lapsDetailSection);
        }

        int km = 1;
        for (Map.Entry<String, WaypointSegment> entry : track.getCoordinatesNewKm().entrySet()) {
            appendTextToBuffer(km + ")<br>");
            appendTextToReportBuffer(km + ")");
            String lapTimeRelevationSection = "Time relevation:" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", entry.getValue().getTimeSpent());
            appendTextToBuffer(lapTimeRelevationSection + "<br>");
            appendTextToReportBuffer(lapTimeRelevationSection);
            String lapTimeSection = "Time lap:" + TimeUtility.toStringFromTimeDiff(entry.getValue().getTimeIncrement());
            appendTextToBuffer(lapTimeSection + "<br>");
            appendTextToReportBuffer(lapTimeSection);
            String lapAvgSpeedSection = "Avg speed:" + GPSUtility.roundDoubleStat(entry.getValue().getAvgSpeed());
            appendTextToBuffer(lapAvgSpeedSection + "<br>");
            appendTextToReportBuffer(lapAvgSpeedSection);
            String fontElevation = "<font color=\"red\">";
            String fontDescent = "<font color=\"green\">";
            if (entry.getValue().getEleGained() > 0) {
                appendTextToBuffer("Elevation gained:" + fontElevation + GPSUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>");
            } else {
                appendTextToBuffer("Elevation gained:" + fontDescent + GPSUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>");
            }
            appendTextToReportBuffer("Elevation gained:" + GPSUtility.roundDoubleStat(entry.getValue().getEleGained()));
            String heartSection = "Min heart:" + GPSUtility.roundDoubleStat(entry.getValue().getMinHeart()) + "-" + "Max heart:" + GPSUtility.roundDoubleStat(entry.getValue().getMaxHeart());
            appendTextToBuffer(heartSection + "<br><br>");
            appendTextToReportBuffer(heartSection);
            appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
            km++;
        }
        appendTextToBuffer("</p></p>");

        //write
        textPane.append(null, flushBuffer());

        this.getViewport().add(textPane);

    }

    private void appendTextToBuffer(String text) {
        text4HTML.append(text);
    }

    private void appendTextToReportBuffer(String text) {
        text4Report.append(text);
        //this is a separator for each line
        text4Report.append(ELEMENT_SEPARATOR_REPORT);
    }

    private String flushBuffer() {
        String text = text4HTML.toString();
        text4HTML = new StringBuffer();
        return text;
    }

    public String getText4Report() {
        return text4Report.toString();
    }

}
