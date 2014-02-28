package org.hifly.geomapviewer.gui.frame;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.TimeUtility;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 24/02/14
 */
public class AggregateDetailViewer extends JScrollPane {

    private List<Track> tracks;
    private JFrame currentFrame;

    public AggregateDetailViewer(List<Track> tracks,JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        this.tracks = tracks;
        this.currentFrame = currentFrame;

        createDetailsViewer();
    }

    private void createDetailsViewer() {
        HTMLEditorPanel textPane = new HTMLEditorPanel();
        LinkController handler = new LinkController(currentFrame);
        textPane.addMouseListener(handler);

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

        Map<String,WaypointSegment> fastests = new HashMap(tracks.size());
        Map<String,WaypointSegment> slowests = new HashMap(tracks.size());
        Map<String,WaypointSegment> shortests = new HashMap(tracks.size());
        Map<String,WaypointSegment> longests = new HashMap(tracks.size());
        Map<String,WaypointSegment> lessElevateds = new HashMap(tracks.size());
        Map<String,WaypointSegment> mostElevateds = new HashMap(tracks.size());

        WaypointSegment fastest = null;
        String fastestString = null;
        WaypointSegment slowest = null;
        String slowestString = null;
        WaypointSegment shortest = null;
        String shortestString = null;
        WaypointSegment longest = null;
        String longestString = null;
        WaypointSegment lessElevated = null;
        String lessElevatedString = null;
        WaypointSegment mostElevated = null;
        String mostElevatedString = null;

        String text = "<p>Number of tracks:"+tracks.size()+"<br>";
        for(Track track:tracks) {
            totalDistance+= GpsUtility.roundDoubleStat(track.getTotalDistance());
            totalCalories+=track.getCalories();
            totalDeviceElevation+=GpsUtility.roundDoubleStat(track.getCalculatedElevation());
            totalRealElevation+=GpsUtility.roundDoubleStat(track.getRealElevation());
            totalDeviceDescent+=GpsUtility.roundDoubleStat(track.getCalculatedDescent());
            totalRealDescent+=GpsUtility.roundDoubleStat(track.getRealDescent());
            totalCalculatedSpeed+=GpsUtility.roundDoubleStat(track.getCalculatedAvgSpeed());
            totalEffectiveSpeed+=GpsUtility.roundDoubleStat(track.getEffectiveAvgSpeed());
            totalRealTime+=track.getRealTime();
            totalEffectiveTime+=track.getEffectiveTime();
            double maxAltitudeTemp = track.getMaxAltitude();
            double minAltitudeTemp =  track.getMinAltitude();
            if(maxAltitudeTemp>maxAltitude) {
                maxAltitude = maxAltitudeTemp;
                trackNameMaxAltitude = track.getName();
            }
            if(minAltitudeTemp<minAltitude) {
                minAltitude = minAltitudeTemp;
                trackNameMinAltitude = track.getName();
            }
            totalClimbingDistance+=track.getClimbingDistance();
            totalClimbingSpeed+=track.getClimbingSpeed();
            totalClimbingTime+=track.getClimbingTimeMillis();

            fastests.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Fastest"));
            slowests.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Slowest"));
            shortests.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Shortest"));
            longests.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Longest"));
            lessElevateds.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Less Elevated"));
            mostElevateds.put(track.getName()==null?track.getFileName():track.getName(),track.getStatsNewKm().get("Most Elevated"));

        }
        text+= "Total distance:"+GpsUtility.roundDoubleStat(totalDistance)+"<br>";
        text+= "Avg distance:"+GpsUtility.roundDoubleStat(totalDistance)/tracks.size()+"<br>";
        text+= "Total calories:"+totalCalories+"<br>";
        text+= "Avg calories:"+totalCalories/tracks.size()+"<br>";
        text+= "<br><br>";
        text+= "Total duration:"+ TimeUtility.toStringFromTimeDiff(totalRealTime)+"<br>";
        text+= "Total effective duration:"+TimeUtility.toStringFromTimeDiff(totalEffectiveTime)+"<br>";
        text+= "Avg duration:"+TimeUtility.toStringFromTimeDiff(totalRealTime/tracks.size())+"<br>";
        text+= "Avg effective duration:"+TimeUtility.toStringFromTimeDiff(totalEffectiveTime/tracks.size())+"<br>";
        text+= "<br><br>";
        text+= "Avg calculated speed:"+GpsUtility.roundDoubleStat(totalCalculatedSpeed)/tracks.size()+"<br>";
        text+= "Avg effective speed:"+GpsUtility.roundDoubleStat(totalEffectiveSpeed)/tracks.size()+"<br>";
        text+= "<br><br>";
        text+= "Total device elevation:"+GpsUtility.roundDoubleStat(totalDeviceElevation)+"<br>";
        text+= "Total real elevation:"+GpsUtility.roundDoubleStat(totalRealElevation)+"<br>";
        text+= "Total device descent:"+GpsUtility.roundDoubleStat(totalDeviceDescent)+"<br>";
        text+= "Total real descent:"+GpsUtility.roundDoubleStat(totalRealDescent)+"<br>";
        text+= "<br><br>";
        text+= "Avg device elevation:"+GpsUtility.roundDoubleStat(totalDeviceElevation)/tracks.size()+"<br>";
        text+= "Avg real elevation:"+GpsUtility.roundDoubleStat(totalRealElevation)/tracks.size()+"<br>";
        text+= "Avg device descent:"+GpsUtility.roundDoubleStat(totalDeviceDescent)/tracks.size()+"<br>";
        text+= "Avg real descent:"+GpsUtility.roundDoubleStat(totalRealDescent)/tracks.size()+"<br>";
        text+= "Max altitude:"+GpsUtility.roundDoubleStat(maxAltitude)+" in track:"+trackNameMaxAltitude+"<br>";
        text+= "Min altitude:"+GpsUtility.roundDoubleStat(minAltitude)+" in track:"+trackNameMinAltitude+"<br>";
        text += "Total climbing distance:" + GpsUtility.roundDoubleStat(totalClimbingDistance) + "<br>";
        text += "Total climbing time:" + TimeUtility.toStringFromTimeDiff(totalClimbingTime) + "<br>";
        text += "Avg climbing time:" + TimeUtility.toStringFromTimeDiff(totalClimbingTime/tracks.size()) + "<br>";
        text += "Avg climbing speed:" + GpsUtility.roundDoubleStat(totalClimbingSpeed)/tracks.size() + "<br>";
        text += "<br><br>";

        for (Map.Entry<String,WaypointSegment> entry : fastests.entrySet()) {
            if(fastest==null) {
                fastest = entry.getValue();
                fastestString = entry.getKey();
            }
            else {
                if(entry.getValue().getAvgSpeed()>fastest.getAvgSpeed()) {
                    fastest = entry.getValue();
                    fastestString = entry.getKey();
                }
            }
        }

        for (Map.Entry<String,WaypointSegment> entry : slowests.entrySet()) {
            if(slowest==null) {
                slowest = entry.getValue();
                slowestString = entry.getKey();
            }
            else {
                if(entry.getValue().getAvgSpeed()<slowest.getAvgSpeed()) {
                    slowest = entry.getValue();
                    slowestString = entry.getKey();
                }
            }
        }

        for (Map.Entry<String,WaypointSegment> entry : shortests.entrySet()) {
            if(shortest==null) {
                shortest = entry.getValue();
                shortestString = entry.getKey();
            }
            else {
                if(entry.getValue().getTimeIncrement()<shortest.getTimeIncrement()) {
                    shortest = entry.getValue();
                    shortestString = entry.getKey();
                }
            }
        }

        for (Map.Entry<String,WaypointSegment> entry : longests.entrySet()) {
            if(longest==null) {
                longest = entry.getValue();
                longestString = entry.getKey();
            }
            else {
                if(entry.getValue().getTimeIncrement()>longest.getTimeIncrement()) {
                    longest = entry.getValue();
                    longestString = entry.getKey();
                }
            }
        }

        for (Map.Entry<String,WaypointSegment> entry : mostElevateds.entrySet()) {
            if(mostElevated==null) {
                mostElevated = entry.getValue();
                mostElevatedString = entry.getKey();
            }
            else {
                if(entry.getValue().getEleGained()>mostElevated.getEleGained()) {
                    mostElevated = entry.getValue();
                    mostElevatedString = entry.getKey();
                }
            }
        }

        for (Map.Entry<String,WaypointSegment> entry : lessElevateds.entrySet()) {
            if(lessElevated==null) {
                lessElevated = entry.getValue();
                lessElevatedString = entry.getKey();
            }
            else {
                if(entry.getValue().getEleGained()<lessElevated.getEleGained()) {
                    lessElevated = entry.getValue();
                    lessElevatedString = entry.getKey();
                }
            }
        }

        if(fastest!=null) {
            text += "Fastest Lap:" + fastest.getKm() + " - " + GpsUtility.roundDoubleStat(fastest.getAvgSpeed()) + "in track:"+fastestString+"<br>";
        }
        else {
            text += "Fastest Lap:<br>";
        }
        if(slowest!=null) {
            text += "Slowest Lap:" + slowest.getKm() + " - " + GpsUtility.roundDoubleStat(slowest.getAvgSpeed()) + "in track:"+slowestString+"<br>";
        }
        else {
            text += "Slowest Lap:<br>";
        }
        if(shortest!=null) {
            text += "Shortest Lap:" + shortest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement()) + "in track:"+shortestString+"<br>";
        }
        else {
            text += "Shortest Lap:<br>";
        }
        if(longest!=null) {
            text += "Longest Lap:" + longest.getKm() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement()) + "in track:"+longestString+"<br>";
        }
        else {
            text += "Longest Lap:<br>";
        }


        text += "Most elevated Lap:" + mostElevated.getKm() + " - " + GpsUtility.roundDoubleStat(mostElevated.getEleGained()) + "in track:"+mostElevatedString+"<br>";
        text += "Less elevated Lap:" + lessElevated.getKm() + " - " + GpsUtility.roundDoubleStat(lessElevated.getEleGained()) + "in track:"+lessElevatedString+"<br>";
        text += "</p></p><hr>";

        //write
        textPane.append(null, text);
        text = "";

        Map<Integer,List<List<Waypoint>>> mapTrackWaypoints = new HashMap(tracks.size());
        for (int k = 0; k < tracks.size(); k++) {
            Track track = tracks.get(k);

            text += "<p><b>"+track.getName()==null?track.getFileName():track.getName()+" Slopes" + "(" + track.getSlopes().size() + ")</b><br><br>";
            long totalSlopeDuration = 0;
            double totalSlopeElevation = 0;
            double totalSlopeDistance = 0;
            double totalSlopeGradient = 0;
            double totalAvgSpeed = 0;
            double totalPower = 0;

            List<List<Waypoint>> listWaypoint = new ArrayList();
            for (int z = 0; z < track.getSlopes().size(); z++) {
                SlopeSegment slope = track.getSlopes().get(z);
                listWaypoint.add(slope.getWaypoints());
                try {
                    //write
                    textPane.append(null, text);
                    text = "";

                    //TODO change URL format;
                    textPane.addHyperlink(
                            new URL("http://geomapviewer.com?trackIndex="+k+"&waypointIndex=" + z), "profile", Color.BLUE);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                text += "Distance:" + GpsUtility.roundDoubleStat(slope.getDistance()) + " km<br>";
                text += "Start km:" + GpsUtility.roundDoubleStat(slope.getStartDistance()) + " km<br>";
                text += "End km:" + GpsUtility.roundDoubleStat(slope.getEndDistance()) + " km<br>";
                if (slope.getEndDate() != null) {
                    text += "Duration:" + TimeUtility.toStringFromTimeDiff(slope.getEndDate().getTime() - slope.getStartDate().getTime()) + "<br>";
                } else {
                    text += "Duration:<br>";
                }

                text += "Elevation:" + GpsUtility.roundDoubleStat(slope.getElevation()) + " m<br>";
                text += "Gradient:" + GpsUtility.roundDoubleStat(slope.getGradient()) + " %<br>";
                text += "Start elevetion m:" + GpsUtility.roundDoubleStat(slope.getStartElevation()) + " m<br>";
                text += "End elevation km:" + GpsUtility.roundDoubleStat(slope.getEndElevation()) + " m<br>";
                text += "Avg speed:" + GpsUtility.roundDoubleStat(slope.getAvgSpeed()) + " km/h<br>";
                text += "Power:" + GpsUtility.roundDoubleStat(slope.getPower()) + " watt<br>";
                text += "<br><br>";
                //write
                textPane.append(null, text);
                text = "";
                totalSlopeDistance += slope.getDistance();
                totalSlopeElevation += slope.getElevation();
                totalSlopeGradient += slope.getGradient();
                totalAvgSpeed += slope.getAvgSpeed();
                totalPower+= slope.getPower();
                if (slope.getEndDate() != null) {
                    totalSlopeDuration += (slope.getEndDate().getTime() - slope.getStartDate().getTime());
                }
            }
            mapTrackWaypoints.put(k,listWaypoint);


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
            }
            text += "</p></p><hr>";
        }

        handler.setTrackWaypoints(mapTrackWaypoints);

        text+= "</p></p><hr>";

        //write
        textPane.append(null, text);

        this.getViewport().add(textPane);

    }

}
