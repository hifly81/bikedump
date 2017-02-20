package org.hifly.bikedump.gps;

import com.garmin.xmlschemas.trainingCenterDatabase.v2.*;
import org.hifly.bikedump.domain.Author;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.SlopeUtility;
import org.hifly.bikedump.utility.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TCX2Document extends GPSDocument {
    private Logger log = LoggerFactory.getLogger(TCX2Document.class);

    public TCX2Document(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        String trackName = null;
        String sportType = null;

        TrainingCenterDatabaseDocument doc = TrainingCenterDatabaseDocument.Factory.parse(new File(gpsFile));
        TrainingCenterDatabaseT tcx = doc.getTrainingCenterDatabase();
        TrackT[] tracks = null;
        //tcx can contain courses or activities
        if(tcx.getActivities()== null) {
            CourseT[] courses = tcx.getCourses().getCourseArray();
            tracks = courses[0].getTrackArray();
            totalDistance = courses[0].getLapArray()[0].getDistanceMeters() / 1000;
            totalTime = courses[0].getLapArray()[0].getTotalTimeSeconds();
            trackName = courses[0].getName();
        }
        else {
            ActivityT[] activities = tcx.getActivities().getActivityArray();
            ActivityLapT lap = activities[0].getLapArray(0);
            tracks = lap.getTrackArray();
            sportType = activities[0].getSport().toString();
            startTime = lap.getStartTime().getTime();
            totalDistance = lap.getDistanceMeters() / 1000;
            totalTime = lap.getTotalTimeSeconds();
            calories = lap.getCalories();
        }


        for (TrackT track : tracks) {
            if(track.getTrackpointArray(track.getTrackpointArray().length - 1).getTime() != null)
                endTime = track.getTrackpointArray(track.getTrackpointArray().length - 1).getTime().getTime();
            TrackpointT[] segments = track.getTrackpointArray();
            TrackpointT last = null;
            for (TrackpointT segment : segments) {
                TrackpointT current = segment;
                if(current == null || current.getPosition() == null)
                    continue;
                double currentLat = current.getPosition().getLatitudeDegrees();
                double currentLon = current.getPosition().getLongitudeDegrees();
                //add coordinate element
                addCoordinateElement(currentLat, currentLon);
                Date currentTime = null;
                if(current.getTime() != null)
                    currentTime = current.getTime().getTime();
                if (last != null) {
                    double lastLat = last.getPosition().getLatitudeDegrees();
                    double lastLon = last.getPosition().getLongitudeDegrees();
                    //calculate harvesine  in km
                    double distance = GPSUtility.haversine(currentLat, currentLon, lastLat, lastLon);
                    totalDistanceCalculated += distance;
                    //need to be objects since could be nullable
                    //TODO
                    //this value is tcx can be null, check to other extension
                    BigDecimal currentCalcEle = null;
                    BigDecimal lastCalcEle = null;
                    if(current.xgetAltitudeMeters()==null && last.xgetAltitudeMeters()==null) {
                        currentCalcEle = new BigDecimal(0);
                        lastCalcEle = new BigDecimal(0);
                    }
                    else if(current.xgetAltitudeMeters() == null)
                        currentCalcEle = new BigDecimal(last.getAltitudeMeters());
                    else if(last.xgetAltitudeMeters() == null)
                        lastCalcEle = new BigDecimal(current.getAltitudeMeters());
                    else {
                        currentCalcEle = new BigDecimal(current.getAltitudeMeters());
                        lastCalcEle = new BigDecimal(last.getAltitudeMeters());
                    }

                    Date lastTime = null;
                    if(last.getTime() != null)
                        lastTime = last.getTime().getTime();
                    //add basic gps elements
                    createWaypointElement(currentLat, currentLon, lastLat, lastLon, distance, currentCalcEle, lastCalcEle, currentTime, lastTime, 0, totalDistanceCalculated);
                    //calculate speed between points
                    double timeDiffInHour = TimeUtility.getTimeDiffHour(last.getTime(), current.getTime());
                    addSpeedElement(distance, timeDiffInHour);
                }
                last = current;
            }
            result.add(createTrack(gpsFile,trackName,sportType));

        }
        return result;
    }

    private Track createTrack(
            String fileName,
            String trackName,
            String sportType) {
        Track resultTrack = new Track();
        resultTrack.setFileName(fileName);
        resultTrack.setStartDate(startTime);
        resultTrack.setEndDate(endTime);
        resultTrack.setSportType(sportType);
        //TODO check if TCX can provide a name
        resultTrack.setName(trackName==null?fileName:trackName);
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

        resultTrack.setAltimetricProfile(SlopeUtility.totalAltimetricProfile(waypoints));
        resultTrack.setSlopes(SlopeUtility.extractSlope(waypoints, profileSetting));
        resultTrack.setCoordinates(coordinates);
        GPSUtility.GpsStats stats = GPSUtility.extractInfoFromWaypoints(waypoints, totalDistance);
        resultTrack.setCoordinatesNewKm(stats.getWaypointsKm());
        resultTrack.setMaxAltitude(stats.getMaxAltitude());
        resultTrack.setMinAltitude(stats.getMinAltitude());
        resultTrack.setClimbingSpeed(stats.getClimbingSpeed());
        resultTrack.setClimbingTimeMillis(stats.getClimbingTime());
        resultTrack.setClimbingDistance(stats.getClimbingDistance());
        if(resultTrack.getCoordinatesNewKm() != null)
            resultTrack.setStatsNewKm(GPSUtility.calculateStatsInUnit(resultTrack.getCoordinatesNewKm()));
        result.add(resultTrack);

        return resultTrack;
    }

}
