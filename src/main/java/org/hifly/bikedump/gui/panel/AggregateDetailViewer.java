package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.events.LinkAdapter;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.TimeUtility;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateDetailViewer extends JScrollPane {

    private List<Track> tracks;
    private JFrame currentFrame;

    public AggregateDetailViewer(List<Track> tracks, JFrame currentFrame) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        this.tracks = tracks;
        this.currentFrame = currentFrame;

        createDetailsViewer();
    }

    private void createDetailsViewer() {
        HTMLEditorPanel textPane = new HTMLEditorPanel();
        LinkAdapter handler = new LinkAdapter(currentFrame);
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

        Map<String, WaypointSegment> fastests = new HashMap(tracks.size());
        Map<String, WaypointSegment> slowests = new HashMap(tracks.size());
        Map<String, WaypointSegment> shortests = new HashMap(tracks.size());
        Map<String, WaypointSegment> longests = new HashMap(tracks.size());
        Map<String, WaypointSegment> lessElevateds = new HashMap(tracks.size());
        Map<String, WaypointSegment> mostElevateds = new HashMap(tracks.size());

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

        String text = "<p>Number of tracks:" + tracks.size() + "<br>";
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

            if(track.getStatsNewKm() !=null) {
                fastests.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Fastest"));
                slowests.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Slowest"));
                shortests.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Shortest"));
                longests.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Longest"));
                lessElevateds.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Less Elevated"));
                mostElevateds.put(track.getName() == null ? track.getFileName() : track.getName(), track.getStatsNewKm().get("Most Elevated"));
            }

        }
        text += "Total distance:" + GPSUtility.roundDoubleStat(totalDistance) + "<br>";
        text += "Avg distance:" + GPSUtility.roundDoubleStat(totalDistance / tracks.size()) + "<br>";
        text += "Total calories:" + totalCalories + "<br>";
        text += "Avg calories:" + GPSUtility.roundDoubleStat(totalCalories / tracks.size()) + "<br>";
        text += "<br><br>";
        text += "Total duration:" + TimeUtility.toStringFromTimeDiff(totalRealTime) + "<br>";
        text += "Total effective duration:" + TimeUtility.toStringFromTimeDiff(totalEffectiveTime) + "<br>";
        text += "Avg duration:" + TimeUtility.toStringFromTimeDiff(totalRealTime / tracks.size()) + "<br>";
        text += "Avg effective duration:" + TimeUtility.toStringFromTimeDiff(totalEffectiveTime / tracks.size()) + "<br>";
        text += "<br><br>";
        text += "Avg calculated speed:" + GPSUtility.roundDoubleStat(totalCalculatedSpeed / tracks.size()) + "<br>";
        text += "Avg effective speed:" + GPSUtility.roundDoubleStat(totalEffectiveSpeed / tracks.size()) + "<br>";
        text += "<br><br>";
        text += "Total device elevation:" + GPSUtility.roundDoubleStat(totalDeviceElevation) + "<br>";
        text += "Total real elevation:" + GPSUtility.roundDoubleStat(totalRealElevation) + "<br>";
        text += "Total device descent:" + GPSUtility.roundDoubleStat(totalDeviceDescent) + "<br>";
        text += "Total real descent:" + GPSUtility.roundDoubleStat(totalRealDescent) + "<br>";
        text += "<br><br>";
        text += "Avg device elevation:" + GPSUtility.roundDoubleStat(totalDeviceElevation / tracks.size()) + "<br>";
        text += "Avg real elevation:" + GPSUtility.roundDoubleStat(totalRealElevation / tracks.size()) + "<br>";
        text += "Avg device descent:" + GPSUtility.roundDoubleStat(totalDeviceDescent / tracks.size()) + "<br>";
        text += "Avg real descent:" + GPSUtility.roundDoubleStat(totalRealDescent / tracks.size()) + "<br>";
        text += "Max altitude:" + GPSUtility.roundDoubleStat(maxAltitude) + " in track:" + trackNameMaxAltitude + "<br>";
        text += "Min altitude:" + GPSUtility.roundDoubleStat(minAltitude) + " in track:" + trackNameMinAltitude + "<br>";
        text += "Total climbing distance:" + GPSUtility.roundDoubleStat(totalClimbingDistance) + "<br>";
        text += "Total climbing time:" + TimeUtility.toStringFromTimeDiff(totalClimbingTime) + "<br>";
        text += "Avg climbing time:" + TimeUtility.toStringFromTimeDiff(totalClimbingTime / tracks.size()) + "<br>";
        text += "Avg climbing speed:" + GPSUtility.roundDoubleStat(totalClimbingSpeed / tracks.size()) + "<br>";
        text += "<br><br>";

        for (Map.Entry<String, WaypointSegment> entry : fastests.entrySet()) {
            if (fastest == null) {
                fastest = entry.getValue();
                fastestString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getAvgSpeed() > fastest.getAvgSpeed()) {
                        fastest = entry.getValue();
                        fastestString = entry.getKey();
                    }
                }
            }
        }

        for (Map.Entry<String, WaypointSegment> entry : slowests.entrySet()) {
            if (slowest == null) {
                slowest = entry.getValue();
                slowestString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getAvgSpeed() < slowest.getAvgSpeed()) {
                        slowest = entry.getValue();
                        slowestString = entry.getKey();
                    }
                }
            }
        }

        for (Map.Entry<String, WaypointSegment> entry : shortests.entrySet()) {
            if (shortest == null) {
                shortest = entry.getValue();
                shortestString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getTimeIncrement() < shortest.getTimeIncrement()) {
                        shortest = entry.getValue();
                        shortestString = entry.getKey();
                    }
                }
            }
        }

        for (Map.Entry<String, WaypointSegment> entry : longests.entrySet()) {
            if (longest == null) {
                longest = entry.getValue();
                longestString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getTimeIncrement() > longest.getTimeIncrement()) {
                        longest = entry.getValue();
                        longestString = entry.getKey();
                    }
                }
            }
        }

        for (Map.Entry<String, WaypointSegment> entry : mostElevateds.entrySet()) {
            if (mostElevated == null) {
                mostElevated = entry.getValue();
                mostElevatedString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getEleGained() > mostElevated.getEleGained()) {
                        mostElevated = entry.getValue();
                        mostElevatedString = entry.getKey();
                    }
                }
            }
        }

        for (Map.Entry<String, WaypointSegment> entry : lessElevateds.entrySet()) {
            if (lessElevated == null) {
                lessElevated = entry.getValue();
                lessElevatedString = entry.getKey();
            } else {
                if (entry.getValue() != null) {
                    if (entry.getValue().getEleGained() < lessElevated.getEleGained()) {
                        lessElevated = entry.getValue();
                        lessElevatedString = entry.getKey();
                    }
                }
            }
        }

        if (fastest != null)
            text += "Fastest Lap:" + fastest.getUnit() + " - " + GPSUtility.roundDoubleStat(fastest.getAvgSpeed()) + "in track:" + fastestString + "<br>";
        else
            text += "Fastest Lap:<br>";
        if (slowest != null)
            text += "Slowest Lap:" + slowest.getUnit() + " - " + GPSUtility.roundDoubleStat(slowest.getAvgSpeed()) + "in track:" + slowestString + "<br>";
        else
            text += "Slowest Lap:<br>";
        if (shortest != null)
            text += "Shortest Lap:" + shortest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(shortest.getTimeIncrement()) + "in track:" + shortestString + "<br>";
        else
            text += "Shortest Lap:<br>";
        if (longest != null)
            text += "Longest Lap:" + longest.getUnit() + " - " + TimeUtility.toStringFromTimeDiff(longest.getTimeIncrement()) + "in track:" + longestString + "<br>";
        else
            text += "Longest Lap:<br>";
        if (mostElevated != null)
            text += "Most elevated Lap:" + mostElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(mostElevated.getEleGained()) + "in track:" + mostElevatedString + "<br>";
        else
            text += "Most elevated Lap:<br>";

        if (lessElevated !=null)
            text += "Less elevated Lap:" + lessElevated.getUnit() + " - " + GPSUtility.roundDoubleStat(lessElevated.getEleGained()) + "in track:" + lessElevatedString + "<br>";
        else
            text += "Less elevated Lap:<br>";

        //write
        textPane.append(null, text);

        this.getViewport().add(textPane);

    }

}
