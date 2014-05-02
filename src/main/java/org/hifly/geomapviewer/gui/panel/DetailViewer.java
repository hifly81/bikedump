package org.hifly.geomapviewer.gui.panel;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.gui.events.LinkAdapter;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.TimeUtility;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * @author
 * @date 24/02/14
 */
//TODO use a templating manager e.g freemarker
public class DetailViewer extends JScrollPane {

    private Track track;
    private JFrame currentFrame;
    private StringBuffer text4HTML = new StringBuffer();
    private StringBuffer text4Report = new StringBuffer();
    private HTMLEditorPanel textPane;

    private final String ELEMENT_SEPARATOR_REPORT = "$$$";
    private final String ELEMENT_SEPARATOR_SECTION_REPORT = "%%%";


    public DetailViewer(Track track, JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        this.track = track;
        this.currentFrame = currentFrame;

        createDetailViewer();
    }

    private void createDetailViewer() {
        textPane = new HTMLEditorPanel();
        LinkAdapter handler = new LinkAdapter(currentFrame);
        textPane.addMouseListener(handler);


        String imgMountain = null;
        String imgSave = null;
        String imgSun = null;
        String imgSunset = null;
        try {
            URL imgMountainUrl = getClass().getResource("/img/mountain.png");
            imgMountain =  imgMountainUrl.toExternalForm();

            URL imgSaveUrl = getClass().getResource("/img/save.png");
            imgSave =  imgSaveUrl.toExternalForm();

            URL imgSunUrl = getClass().getResource("/img/sun.png");
            imgSun =  imgSunUrl.toExternalForm();

            URL imgSunsetUrl = getClass().getResource("/img/sunset.png");
            imgSunset =  imgSunsetUrl.toExternalForm();

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



        appendTextToBuffer( "<p><b>" + track.getName() + "</b><br>");
        appendTextToReportBuffer(track.getName());
        String sportTypeSection = "Sport type:" + track.getSportType();
        appendTextToBuffer(sportTypeSection + "<br>");
        appendTextToReportBuffer(sportTypeSection);
        String timeSection = TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getStartDate()) + "&nbsp; &nbsp;" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getEndDate());
        appendTextToBuffer(timeSection + "<br>");
        appendTextToReportBuffer(timeSection);
        Map.Entry<String, String> sunTime = TimeUtility.getSunriseSunsetTime(
                track.getCoordinates().get(0).getDecimalLatitude(),
                track.getCoordinates().get(0).getDecimalLongitude(),
                track.getStartDate());
        if(sunTime!=null) {
            //write
            textPane.append(null, flushBuffer());
            textPane.addImg(imgSun,sunTime.getKey());
            textPane.addImg(imgSunset,sunTime.getValue());
        }
        String durationSection = "Duration:" + TimeUtility.toStringFromTimeDiff(track.getRealTime());
        appendTextToBuffer(durationSection + "<br>");
        appendTextToReportBuffer(durationSection);
        String effectiveDurationSection =  "Effective duration:" + TimeUtility.toStringFromTimeDiff(track.getEffectiveTime());
        appendTextToBuffer(effectiveDurationSection + "<br>");
        appendTextToReportBuffer(effectiveDurationSection);
        String distanceSection = "Distance:" + GpsUtility.roundDoubleStat(track.getTotalDistance());
        appendTextToBuffer(distanceSection + "<br>");
        appendTextToReportBuffer(distanceSection);
        String caloriesSection =  "Calories:" + track.getCalories();
        appendTextToBuffer(caloriesSection + "<br>");
        appendTextToReportBuffer(caloriesSection);
        appendTextToBuffer("<br><br>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
        String calculatedSpeedSection = "Calculated Speed:" + GpsUtility.roundDoubleStat(track.getCalculatedAvgSpeed());
        appendTextToBuffer(calculatedSpeedSection + "<br>");
        appendTextToReportBuffer(calculatedSpeedSection);
        String effectiveSpeedSection = "Effective Speed:" + GpsUtility.roundDoubleStat(track.getEffectiveAvgSpeed());
        appendTextToBuffer(effectiveSpeedSection + "<br>");
        appendTextToReportBuffer(effectiveSpeedSection);
        String maxSpeedSection = "Max Speed:" + GpsUtility.roundDoubleStat(track.getMaxSpeed());
        appendTextToBuffer("Max Speed:" + GpsUtility.roundDoubleStat(track.getMaxSpeed()) + "<br>");
        appendTextToReportBuffer(maxSpeedSection);
        appendTextToBuffer("<br><br>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
        String deviceElevationSection = "Device elevation:" + GpsUtility.roundDoubleStat(track.getCalculatedElevation());
        appendTextToBuffer(deviceElevationSection + "<br>");
        appendTextToReportBuffer(deviceElevationSection);
        String realElevationSection =  "Real elevation:" + GpsUtility.roundDoubleStat(track.getRealElevation());
        appendTextToBuffer(realElevationSection + "<br>");
        appendTextToReportBuffer(realElevationSection);
        String deviceDescentSection = "Device descent:" + GpsUtility.roundDoubleStat(track.getCalculatedDescent());
        appendTextToBuffer(deviceDescentSection + "<br>");
        appendTextToReportBuffer(deviceDescentSection);
        String realDescentSection =  "Real descent:" + GpsUtility.roundDoubleStat(track.getRealDescent());
        appendTextToBuffer(realDescentSection + "<br>");
        appendTextToReportBuffer(realDescentSection);
        String maxAltitudeSection = "Max altitude:" + GpsUtility.roundDoubleStat(track.getMaxAltitude());
        appendTextToBuffer(maxAltitudeSection + "<br>");
        appendTextToReportBuffer(maxAltitudeSection);
        String minAltitudeSection = "Min altitude:" + GpsUtility.roundDoubleStat(track.getMinAltitude());
        appendTextToBuffer(minAltitudeSection + "<br>");
        appendTextToReportBuffer(minAltitudeSection);
        String climbingDistanceSection = "Climbing distance:" + GpsUtility.roundDoubleStat(track.getClimbingDistance());
        appendTextToBuffer(climbingDistanceSection + "<br>");
        appendTextToReportBuffer(climbingDistanceSection);
        String climbingTimeSection = "Climbing time:" + TimeUtility.toStringFromTimeDiff(track.getClimbingTimeMillis());
        appendTextToBuffer(climbingTimeSection + "<br>");
        appendTextToReportBuffer(climbingTimeSection);
        String climbingSpeedSection = "Climbing speed:" + GpsUtility.roundDoubleStat(track.getClimbingSpeed());
        appendTextToBuffer(climbingSpeedSection + "<br>");
        appendTextToReportBuffer(climbingSpeedSection);
        appendTextToBuffer("<br><br>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
        WaypointSegment fastest = track.getStatsNewKm().get("Fastest");
        WaypointSegment slowest = track.getStatsNewKm().get("Slowest");
        WaypointSegment shortest = track.getStatsNewKm().get("Shortest");
        WaypointSegment longest = track.getStatsNewKm().get("Longest");
        WaypointSegment lessElevated = track.getStatsNewKm().get("Less Elevated");
        WaypointSegment mostElevated = track.getStatsNewKm().get("Most Elevated");
        if(fastest!=null) {
            String fastestLapSection = "Fastest Lap:" + fastest.getKm() + " - " + GpsUtility.roundDoubleStat(fastest.getAvgSpeed());
            appendTextToBuffer(fastestLapSection + "<br>");
            appendTextToReportBuffer(fastestLapSection);
        }
        else {
            String fastestLapSection = "Fastest Lap:";
            appendTextToBuffer(fastestLapSection+"<br>");
            appendTextToReportBuffer(fastestLapSection);
        }
        if(slowest!=null) {
            String slowestLapSection = "Slowest Lap:" + slowest.getKm() + " - " + GpsUtility.roundDoubleStat(slowest.getAvgSpeed());
            appendTextToBuffer(slowestLapSection + "<br>");
            appendTextToReportBuffer(slowestLapSection);
        }
        else {
            String slowestLapSection = "Slowest Lap:";
            appendTextToBuffer(slowestLapSection+"<br>");
            appendTextToReportBuffer(slowestLapSection);
        }
        if(shortest!=null) {
            String shortestLapSection = "Shortest Lap:" + shortest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement());
            appendTextToBuffer(shortestLapSection + "<br>");
            appendTextToReportBuffer(shortestLapSection);
        }
        else {
            String shortestLapSection = "Shortest Lap:";
            appendTextToBuffer(shortestLapSection+"<br>");
            appendTextToReportBuffer(shortestLapSection);
        }
        if(longest!=null) {
            String longestLapSection = "Longest Lap:" + longest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement());
            appendTextToBuffer(longestLapSection + "<br>");
            appendTextToReportBuffer(longestLapSection);
        }
        else {
            String longestLapSection = "Longest Lap:";
            appendTextToBuffer(longestLapSection+"Longest Lap:<br>");
            appendTextToReportBuffer(longestLapSection);
        }

        String mostElevatedLapSection = "Most elevated Lap:" + mostElevated.getKm() + " - " + GpsUtility.roundDoubleStat(mostElevated.getEleGained());
        appendTextToBuffer(mostElevatedLapSection + "<br>");
        appendTextToReportBuffer(mostElevatedLapSection);
        String lessElevatedLap = "Less elevated Lap:" + lessElevated.getKm() + " - " + GpsUtility.roundDoubleStat(lessElevated.getEleGained());
        appendTextToBuffer(lessElevatedLap + "<br>");
        appendTextToReportBuffer(lessElevatedLap);
        appendTextToBuffer("<br><br></p><hr>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

        String climbsSection = "Climbs" + "(" + track.getSlopes().size() + ")";
        appendTextToBuffer("<p><b>Climbs" + climbsSection+"</b><br><br>");
        appendTextToReportBuffer(climbsSection);
        //write
        textPane.append(null, flushBuffer());


        long totalSlopeDuration = 0;
        double totalSlopeElevation = 0;
        double totalSlopeDistance = 0;
        double totalSlopeGradient = 0;
        double totalAvgSpeed = 0;
        double totalPower = 0;
        double totalVam = 0;

        List<SlopeSegment> listSlopes = new ArrayList();
        for (int z=0;z<track.getSlopes().size();z++) {
            SlopeSegment slope = track.getSlopes().get(z);
            listSlopes.add(slope);
            try {
                //TODO change URL format and include img;
                textPane.addHyperlinkImg(
                        new URL("http://geomapviewer.com?slopeIndex=" + z),imgMountain, Color.BLUE);
                textPane.addHyperlinkImg(
                        new URL("http://geomapviewer.com/save/?slopeIndex=" + z), imgSave, Color.BLUE);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String climbDistanceSection = "Distance:" + GpsUtility.roundDoubleStat(slope.getDistance());
            appendTextToBuffer(climbDistanceSection+"<br>");
            appendTextToReportBuffer(climbDistanceSection);
            String climbStartDistanceSection = "Start km:" + GpsUtility.roundDoubleStat(slope.getStartDistance());
            appendTextToBuffer(climbStartDistanceSection+"<br>");
            appendTextToReportBuffer(climbStartDistanceSection);
            String climbEndDistanceSection = "End km:" + GpsUtility.roundDoubleStat(slope.getEndDistance());
            appendTextToBuffer(climbEndDistanceSection+"<br>");
            appendTextToReportBuffer(climbEndDistanceSection);
            if(slope.getEndDate()!=null) {
                String climbDurationSection = "Duration:" + TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime());
                appendTextToBuffer(climbDurationSection + "<br>");
                appendTextToReportBuffer(climbDurationSection);
            }
            else {
                String climbDurationSection = "Duration:";
                appendTextToBuffer(climbDurationSection+"<br>");
                appendTextToReportBuffer(climbDurationSection);
            }

            String climbElevationSection = "Elevation:" + GpsUtility.roundDoubleStat(slope.getElevation());
            appendTextToBuffer(climbElevationSection+"<br>");
            appendTextToReportBuffer(climbElevationSection);
            String climbGradientSection = "Gradient:" + GpsUtility.roundDoubleStat(slope.getGradient()) + " %";
            appendTextToBuffer(climbGradientSection+"<br>");
            appendTextToReportBuffer(climbGradientSection);
            String climbStartElevationSection = "Start elevation:" + GpsUtility.roundDoubleStat(slope.getStartElevation());
            appendTextToBuffer(climbStartElevationSection+"<br>");
            appendTextToReportBuffer(climbStartElevationSection);
            String climbEndElevationSection = "End elevation:" + GpsUtility.roundDoubleStat(slope.getEndElevation());
            appendTextToBuffer(climbEndElevationSection + "<br>");
            appendTextToReportBuffer(climbEndElevationSection);
            String climbAvgSpeedSection = "Avg speed:" + GpsUtility.roundDoubleStat(slope.getAvgSpeed());
            appendTextToBuffer(climbAvgSpeedSection+"<br>");
            appendTextToReportBuffer(climbAvgSpeedSection);
            String climbPowerSection = "Power:" + GpsUtility.roundDoubleStat(slope.getPower());
            appendTextToBuffer(climbPowerSection+"<br>");
            appendTextToReportBuffer(climbPowerSection);
            String climbVamSection =  "VAM:" + GpsUtility.roundDoubleStat(slope.getVam());
            appendTextToBuffer(climbVamSection+"<br>");
            appendTextToReportBuffer(climbVamSection);
            appendTextToBuffer("<br><br>");
            appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

            //write
            textPane.append(null, flushBuffer());

            totalSlopeDistance += slope.getDistance();
            totalSlopeElevation += slope.getElevation();
            totalSlopeGradient += slope.getGradient();
            totalAvgSpeed += slope.getAvgSpeed();
            totalPower+= slope.getPower();
            totalVam+= slope.getVam();
            if(slope.getEndDate()!=null) {
                totalSlopeDuration += (slope.getEndDate().getTime() - slope.getStartDate().getTime());
            }
        }
        handler.setSlopes(listSlopes);

        if (track.getSlopes().size() > 0) {
            String totalClimbDistanceSection = "Total distance:" + GpsUtility.roundDoubleStat(totalSlopeDistance);
            appendTextToBuffer(totalClimbDistanceSection + "<br>");
            appendTextToReportBuffer(totalClimbDistanceSection);
            String totalClimbElevationSection = "Total elevation:" + GpsUtility.roundDoubleStat(totalSlopeElevation);
            appendTextToBuffer(totalClimbElevationSection + "<br>");
            appendTextToReportBuffer(totalClimbElevationSection);
            String totalClimbDurationSection = "Total duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration);
            appendTextToBuffer(totalClimbDurationSection + "<br>");
            appendTextToReportBuffer(totalClimbDurationSection);
            String totalClimbAvgSection = "Avg distance:" + GpsUtility.roundDoubleStat(totalSlopeDistance / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgSection + "<br>");
            appendTextToReportBuffer(totalClimbAvgSection);
            String totalClimbAvgElevationSection = "Avg elevation:" + GpsUtility.roundDoubleStat(totalSlopeElevation / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgElevationSection + "<br>");
            appendTextToReportBuffer(totalClimbAvgElevationSection);
            String totalClimbAvgDurationSection = "Avg duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration / 2);
            appendTextToBuffer(totalClimbAvgDurationSection + "<br>");
            appendTextToReportBuffer(totalClimbAvgDurationSection);
            String totalClimbAvgGradient = "Avg gradient:" + GpsUtility.roundDoubleStat(totalSlopeGradient / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgGradient + "<br>");
            appendTextToReportBuffer(totalClimbAvgGradient);
            String totalClimbAvgSpeedSection =  "Avg speed:" + GpsUtility.roundDoubleStat(totalAvgSpeed / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgSpeedSection+ "<br>");
            appendTextToReportBuffer(totalClimbAvgSpeedSection);
            String totalClimbAvgPowerSection =  "Avg power:" + GpsUtility.roundDoubleStat(totalPower / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgPowerSection + "<br>");
            appendTextToReportBuffer(totalClimbAvgPowerSection);
            String totalClimbAvgVamSection = "Avg vam:" + GpsUtility.roundDoubleStat(totalVam / track.getSlopes().size());
            appendTextToBuffer(totalClimbAvgVamSection + "<br>");
            appendTextToReportBuffer(totalClimbAvgVamSection);
        }
        appendTextToBuffer("</p></p><hr>");
        appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);

        String lapsDetailSection = "Details for lap";
        appendTextToBuffer("<p><b>"+lapsDetailSection+"</b><br><br>");
        appendTextToReportBuffer(lapsDetailSection);
        int km = 1;
        for (Map.Entry<String, WaypointSegment> entry : track.getCoordinatesNewKm().entrySet()) {
            appendTextToBuffer(km + ")<br>");
            appendTextToReportBuffer(km + ")");
            String lapTimeRelevationSection = "Time relevation:" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss",entry.getValue().getTimeSpent());
            appendTextToBuffer(lapTimeRelevationSection + "<br>");
            appendTextToReportBuffer(lapTimeRelevationSection);
            String lapTimeSection = "Time lap:" + TimeUtility.toStringFromTimeDiff(entry.getValue().getTimeIncrement());
            appendTextToBuffer(lapTimeSection + "<br>");
            appendTextToReportBuffer(lapTimeSection);
            String lapAvgSpeedSection = "Avg speed:" + GpsUtility.roundDoubleStat(entry.getValue().getAvgSpeed());
            appendTextToBuffer(lapAvgSpeedSection+"<br>");
            appendTextToReportBuffer(lapAvgSpeedSection);
            String fontElevation = "<font color=\"red\">";
            String fontDescent = "<font color=\"green\">";
            if (entry.getValue().getEleGained() > 0) {
                appendTextToBuffer("Elevation gained:" + fontElevation + GpsUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>");
            } else {
                appendTextToBuffer("Elevation gained:" + fontDescent + GpsUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>");
            }
            appendTextToReportBuffer("Elevation gained:" + GpsUtility.roundDoubleStat(entry.getValue().getEleGained()));
            appendTextToBuffer("<br><br>");
            appendTextToReportBuffer(ELEMENT_SEPARATOR_SECTION_REPORT);
            km++;
        }
        appendTextToBuffer("</p></p><hr>");

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
        text4HTML.append("");
        return text;
    }

    public String getText4Report() {
        return text4Report.toString();
    }

}
