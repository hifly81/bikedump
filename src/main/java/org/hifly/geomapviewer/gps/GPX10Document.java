package org.hifly.geomapviewer.gps;

import com.topografix.gpx.x1.x0.*;
import org.hifly.geomapviewer.domain.Author;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.SlopeUtility;
import org.hifly.geomapviewer.utility.TimeUtility;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @date 30/01/14
 */
public class GPX10Document extends GPSDocument {


    public GPX10Document(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        GpxDocument doc = GpxDocument.Factory.parse(new File(gpsFile));
        GpxDocument.Gpx gpx = doc.getGpx();
        for (GpxDocument.Gpx.Trk  track : gpx.getTrkArray()) {
            GpxDocument.Gpx.Trk.Trkseg.Trkpt last = null;
            for (GpxDocument.Gpx.Trk.Trkseg  segment : track.getTrksegArray()) {
                GpxDocument.Gpx.Trk.Trkseg.Trkpt[] trackPoints = segment.getTrkptArray();
                for (int i = 0; i < trackPoints.length; i++) {
                    GpxDocument.Gpx.Trk.Trkseg.Trkpt current = segment.getTrkptArray(i);
                    double currentLat = current.getLat().doubleValue();
                    double currentLon = current.getLon().doubleValue();
                    //add coordinate element
                    addCoordinateElement(currentLat, currentLon);
                    Date currentTime = null;
                    if(current.getTime()!=null)  {
                        current.getTime().getTime();
                    }
                    if (last != null) {
                        double lastLat = last.getLat().doubleValue();
                        double lastLon = last.getLon().doubleValue();
                        double distance = GpsUtility.haversine(currentLat, currentLon,lastLat, lastLon);
                        totalDistance += distance;
                        //need to be objects since could be nullable
                        BigDecimal currentCalcEle = current.getEle();
                        BigDecimal lastCalcEle = last.getEle();
                        Date lastTime = null;
                        if(last.getTime()!=null) {
                            last.getTime().getTime();
                        }
                        //add basic gps elements
                        createWaypointElement(gpsFile, currentLat, currentLon, lastLat, lastLon, distance, currentCalcEle, lastCalcEle, currentTime, lastTime, 0, totalDistance);
                        //calculate speed between points
                        double timeDiffInHour = TimeUtility.getTimeDiffHour(last.getTime(), current.getTime());
                        addSpeedElement(gpsFile, currentLat, currentLon, distance, timeDiffInHour);
                    }
                    last = current;
                }

            }
            result.add(createTrack(track, gpx, gpsFile));
        }
        return result;
    }

    private Track createTrack(GpxDocument.Gpx.Trk track, GpxDocument.Gpx gpx, String fileName) {
        Track resultTrack = new Track();
        resultTrack.setFileName(fileName);
        if(track.getTrksegArray(0).getTrkptArray(0).getTime()!=null) {
            startTime = track.getTrksegArray(0).getTrkptArray(0).getTime().getTime();
        }
        GpxDocument.Gpx.Trk.Trkseg lastSegment = track.getTrksegArray(track.getTrksegArray().length - 1);
        if(lastSegment.getTrkptArray(lastSegment.getTrkptArray().length - 1).getTime()!=null) {
            endTime = lastSegment.getTrkptArray(lastSegment.getTrkptArray().length - 1).getTime().getTime();
        }
        long diffStartEndTime = 0;
        if(endTime!=null && startTime!=null) {
            diffStartEndTime = endTime.getTime() - startTime.getTime();
        }

        resultTrack.setStartDate(startTime);
        resultTrack.setEndDate(endTime);
        resultTrack.setRealTime(diffStartEndTime);
        resultTrack.setName(track.getName());
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
        if(gpx.getAuthor()!=null) {
            author.setName(gpx.getAuthor());
        }
        if(gpx.getEmail()!=null) {
                author.setEmail(gpx.getEmail());
        }
        resultTrack.setAuthor(author);
        resultTrack.setSlopes(SlopeUtility.extractSlope(waypoints,profileSetting));
        resultTrack.setCoordinates(coordinates);
        GpsUtility.GpsStats stats = GpsUtility.extractInfoFromWaypoints(waypoints, totalDistance);
        resultTrack.setCoordinatesNewKm(stats.getWaypointsKm());
        resultTrack.setMaxAltitude(stats.getMaxAltitude());
        resultTrack.setMinAltitude(stats.getMinAltitude());
        resultTrack.setClimbingSpeed(stats.getClimbingSpeed());
        resultTrack.setClimbingTimeMillis(stats.getClimbingTime());
        resultTrack.setClimbingDistance(stats.getClimbingDistance());
        resultTrack.setStatsNewKm(GpsUtility.calculateStatsInUnit(resultTrack.getCoordinatesNewKm()));

        return resultTrack;
    }

}
