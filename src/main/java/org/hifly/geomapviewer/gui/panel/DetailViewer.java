package org.hifly.geomapviewer.gui.panel;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.gui.events.LinkController;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.TimeUtility;

import javax.swing.*;
import java.awt.*;
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

    public DetailViewer(Track track, JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        this.track = track;
        this.currentFrame = currentFrame;

        createDetailViewer();
    }

    private void createDetailViewer() {
        HTMLEditorPanel textPane = new HTMLEditorPanel();
        LinkController handler = new LinkController(currentFrame);
        textPane.addMouseListener(handler);

        String text = "<p><b>" + track.getName() + "</b><br>";
        text+= "Sport type:"+track.getSportType()+"<br>";
        text += TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss",track.getStartDate()) + "&nbsp &nbsp;" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", track.getEndDate()) + "<br>";
        Map.Entry<String, String> sunTime = TimeUtility.getSunriseSunsetTime(
                track.getCoordinates().get(0).getDecimalLatitude(),
                track.getCoordinates().get(0).getDecimalLongitude(),
                track.getStartDate());
        if(sunTime!=null) {
            text += "Sunrise:" + sunTime.getKey() + "<br>";
            text += "Sunset:" + sunTime.getValue() + "<br>";
        }
        else {
            text += "Sunrise:<br>";
            text += "Sunset:<br>";
        }
        text += "Duration:" + TimeUtility.toStringFromTimeDiff(track.getRealTime()) + "<br>";
        text += "Effective duration:" + TimeUtility.toStringFromTimeDiff(track.getEffectiveTime()) + "<br>";
        text += "Distance:" + GpsUtility.roundDoubleStat(track.getTotalDistance()) + "<br>";
        text += "Calories:" + track.getCalories() + "<br>";
        text += "<br><br>";
        text += "Calculated Speed:" + GpsUtility.roundDoubleStat(track.getCalculatedAvgSpeed()) + "<br>";
        text += "Effective Speed:" + GpsUtility.roundDoubleStat(track.getEffectiveAvgSpeed()) + "<br>";
        text += "Max Speed:" + GpsUtility.roundDoubleStat(track.getMaxSpeed()) + "<br>";
        text += "<br><br>";
        text += "Device elevation:" + GpsUtility.roundDoubleStat(track.getCalculatedElevation()) + "<br>";
        text += "Real elevation:" + GpsUtility.roundDoubleStat(track.getRealElevation()) + "<br>";
        text += "Device descent:" + GpsUtility.roundDoubleStat(track.getCalculatedDescent()) + "<br>";
        text += "Real descent:" + GpsUtility.roundDoubleStat(track.getRealDescent()) + "<br>";
        text += "Max altitude:" + GpsUtility.roundDoubleStat(track.getMaxAltitude()) + "<br>";
        text += "Min altitude:" + GpsUtility.roundDoubleStat(track.getMinAltitude()) + "<br>";
        text += "Climbing distance:" + GpsUtility.roundDoubleStat(track.getClimbingDistance()) + "<br>";
        text += "Climbing time:" + TimeUtility.toStringFromTimeDiff(track.getClimbingTimeMillis()) + "<br>";
        text += "Climbing speed:" + GpsUtility.roundDoubleStat(track.getClimbingSpeed()) + "<br>";
        text += "<br><br>";
        WaypointSegment fastest = track.getStatsNewKm().get("Fastest");
        WaypointSegment slowest = track.getStatsNewKm().get("Slowest");
        WaypointSegment shortest = track.getStatsNewKm().get("Shortest");
        WaypointSegment longest = track.getStatsNewKm().get("Longest");
        WaypointSegment lessElevated = track.getStatsNewKm().get("Less Elevated");
        WaypointSegment mostElevated = track.getStatsNewKm().get("Most Elevated");
        if(fastest!=null) {
            text += "Fastest Lap:" + fastest.getKm() + " - " + GpsUtility.roundDoubleStat(fastest.getAvgSpeed()) + "<br>";
        }
        else {
            text += "Fastest Lap:<br>";
        }
        if(slowest!=null) {
            text += "Slowest Lap:" + slowest.getKm() + " - " + GpsUtility.roundDoubleStat(slowest.getAvgSpeed()) + "<br>";
        }
        else {
            text += "Slowest Lap:<br>";
        }
        if(shortest!=null) {
            text += "Shortest Lap:" + shortest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement()) + "<br>";
        }
        else {
            text += "Shortest Lap:<br>";
        }
        if(longest!=null) {
            text += "Longest Lap:" + longest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement()) + "<br>";
        }
        else {
            text += "Longest Lap:<br>";
        }


        text += "Most elevated Lap:" + mostElevated.getKm() + " - " + GpsUtility.roundDoubleStat(mostElevated.getEleGained()) + "<br>";
        text += "Less elevated Lap:" + lessElevated.getKm() + " - " + GpsUtility.roundDoubleStat(lessElevated.getEleGained()) + "<br>";
        text += "<br><br></p><hr>";
        text += "<p><b>Climbs" + "(" + track.getSlopes().size() + ")</b><br><br>";

        //write
        textPane.append(null, text);
        text = "";


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
                //TODO change URL format;
                textPane.addHyperlink(
                        new URL("http://geomapviewer.com?slopeIndex=" + z), "profile", Color.BLUE);
                //write
                text = "&nbsp;";
                textPane.append(null, text);
                text = "";
                textPane.addHyperlink(
                        new URL("http://geomapviewer.com/save/?slopeIndex=" + z), "save", Color.BLUE);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            text += "Distance:" + GpsUtility.roundDoubleStat(slope.getDistance()) + " km<br>";
            text += "Start km:" + GpsUtility.roundDoubleStat(slope.getStartDistance()) + " km<br>";
            text += "End km:" + GpsUtility.roundDoubleStat(slope.getEndDistance()) + " km<br>";
            if(slope.getEndDate()!=null) {
                text += "Duration:" + TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime()) + "<br>";
            }
            else {
                text += "Duration:<br>";
            }

            text += "Elevation:" + GpsUtility.roundDoubleStat(slope.getElevation()) + " m<br>";
            text += "Gradient:" + GpsUtility.roundDoubleStat(slope.getGradient()) + " %<br>";
            text += "Start elevetion m:" + GpsUtility.roundDoubleStat(slope.getStartElevation()) + " m<br>";
            text += "End elevation km:" + GpsUtility.roundDoubleStat(slope.getEndElevation()) + " m<br>";
            text += "Avg speed:" + GpsUtility.roundDoubleStat(slope.getAvgSpeed()) + " km/h<br>";
            text += "Power:" + GpsUtility.roundDoubleStat(slope.getPower()) + " watt<br>";
            text += "VAM:" + GpsUtility.roundDoubleStat(slope.getVam()) + " m/h<br>";
            text += "<br><br>";
            //write
            textPane.append(null, text);
            text = "";
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
            text += "Total distance:" + GpsUtility.roundDoubleStat(totalSlopeDistance) + "<br>";
            text += "Total elevation:" + GpsUtility.roundDoubleStat(totalSlopeElevation) + "<br>";
            text += "Total duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration) + "<br>";
            text += "Avg distance:" + GpsUtility.roundDoubleStat(totalSlopeDistance) / track.getSlopes().size() + "<br>";
            text += "Avg elevation:" + GpsUtility.roundDoubleStat(totalSlopeElevation) / track.getSlopes().size() + "<br>";
            text += "Avg duration:" + TimeUtility.toStringFromTimeDiff(totalSlopeDuration / 2) + "<br>";
            text += "Avg gradient:" + GpsUtility.roundDoubleStat(totalSlopeGradient) / track.getSlopes().size() + "<br>";
            text += "Avg speed:" + GpsUtility.roundDoubleStat(totalAvgSpeed) / track.getSlopes().size() + "<br>";
            text += "Avg power:" + GpsUtility.roundDoubleStat(totalPower) / track.getSlopes().size() + "<br>";
            text += "Avg vam:" + GpsUtility.roundDoubleStat(totalVam) / track.getSlopes().size() + "<br>";
        }
        text += "</p></p><hr>";
        text += "<p><b>Details for lap</b><br><br>";
        int km = 1;
        for (Map.Entry<String, WaypointSegment> entry : track.getCoordinatesNewKm().entrySet()) {
            text += km + ")<br>";
            text += "Time relevation:" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss",entry.getValue().getTimeSpent()) + "<br>";
            text += "Time lap:" + TimeUtility.toStringFromTimeDiff(entry.getValue().getTimeIncrement()) + "<br>";
            text += "Avg speed:" + GpsUtility.roundDoubleStat(entry.getValue().getAvgSpeed()) + " km/h<<br>";
            String fontElevation = "<font color=\"red\">";
            String fontDescent = "<font color=\"green\">";
            if (entry.getValue().getEleGained() > 0) {
                text += "Elevation gained:" + fontElevation + GpsUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>";
            } else {
                text += "Elevation gained:" + fontDescent + GpsUtility.roundDoubleStat(entry.getValue().getEleGained()) + " m</font><br>";
            }
            text += "<br><br>";
            km++;
        }
        text += "</p></p><hr>";

        //write
        textPane.append(null, text);

        this.getViewport().add(textPane);

    }
}
