package org.hifly.geomapviewer.gps;

import com.garmin.xmlschemas.trainingCenterDatabase.v2.*;
import org.hifly.geomapviewer.domain.Author;
import org.hifly.geomapviewer.domain.gps.Coordinate;
import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gui.GeoMapViewer;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @date 30/01/14
 */
public class TCX2Document extends GPSDocument {
    private Logger log = LoggerFactory.getLogger(TCX2Document.class);

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        TrainingCenterDatabaseDocument doc = TrainingCenterDatabaseDocument.Factory.parse(new File(gpsFile));
        TrainingCenterDatabaseT tcx = doc.getTrainingCenterDatabase();
        ActivityT[] activities = tcx.getActivities().getActivityArray();
        ActivityLapT lap = activities[0].getLapArray(0);
        TrackT[] tracks = lap.getTrackArray();
        startTime = lap.getStartTime().getTime();
        totalDistance = lap.getDistanceMeters() / 1000;
        totalTime = lap.getTotalTimeSeconds();
        calories = lap.getCalories();
        for (TrackT track : tracks) {
            endTime = track.getTrackpointArray(track.getTrackpointArray().length - 1).getTime().getTime();
            TrackpointT[] segments = track.getTrackpointArray();
            TrackpointT last = null;
            for (TrackpointT segment : segments) {
                TrackpointT current = segment;
                double currentLat = current.getPosition().getLatitudeDegrees();
                double currentLon = current.getPosition().getLongitudeDegrees();
                //add coordinate element
                addCoordinateElement(currentLat, currentLon);
                Date currentTime = current.getTime().getTime();
                if (last != null) {
                    double lastLat = last.getPosition().getLatitudeDegrees();
                    double lastLon = last.getPosition().getLongitudeDegrees();
                    //calculate harvesine  in km
                    double distance = GpsUtility.haversine(currentLat, currentLon,lastLat, lastLon);
                    totalDistanceCalculated += distance;
                    //need to be objects since could be nullable
                    BigDecimal currentCalcEle = new BigDecimal(current.getAltitudeMeters());
                    BigDecimal lastCalcEle = new BigDecimal(last.getAltitudeMeters());
                    Date lastTime = last.getTime().getTime();
                    //add basic gps elements
                    addGPSElement(currentLat, currentLon, lastLat, lastLon, distance, currentCalcEle, lastCalcEle, currentTime, lastTime);
                    //calculate speed between points
                    double timeDiffInHour = TimeUtility.getTimeDiff(last.getTime(), current.getTime());
                    addSpeedElement(currentLat, currentLon, distance, timeDiffInHour);
                }
                last = current;
            }
            result.add(createTrack());

        }
        return result;
    }

    private Track createTrack() {
        Track resultTrack = new Track();
        resultTrack.setStartDate(startTime);
        resultTrack.setEndDate(endTime);
        resultTrack.setName("");
        resultTrack.setCalories(calories);
        resultTrack.setRealTime((long) (totalTime * 1000));
        resultTrack.setEffectiveTime(totalTimeDiff);
        resultTrack.setCalculatedAvgSpeed(totalSpeed / totalCalculatedSpeedPoints);
        resultTrack.setEffectiveAvgSpeed(totalEffectiveSpeed / totalEffectiveSpeedPoints);
        resultTrack.setMaxSpeed(maxSpeed);
        resultTrack.setTotalDistance(totalDistance);
        resultTrack.setRealElevation(totalElevation);
        resultTrack.setCalculatedElevation(totalCalculatedElevation);
        resultTrack.setCalculatedDescent(totalCalculatedDescent);
        resultTrack.setRealDescent(totalDescent);
        Author author = new Author();
        author.setName("");
        author.setEmail("");
        resultTrack.setAuthor(author);
        resultTrack.setSlopes(GpsUtility.extractSlope(waypoints));
        resultTrack.setCoordinates(coordinates);
        resultTrack.setCoordinatesNewKm(GpsUtility.extractInfoFromWaypoints(waypoints, totalDistance));
        result.add(resultTrack);

        return resultTrack;
    }

}
