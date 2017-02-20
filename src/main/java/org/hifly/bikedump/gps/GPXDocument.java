package org.hifly.bikedump.gps;

import com.topografix.gpx.x1.x1.*;
import com.topografix.gpx.x1.x1.impl.ExtensionsTypeImpl;
import org.apache.xmlbeans.XmlObject;
import org.hifly.bikedump.domain.Author;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.SlopeUtility;
import org.hifly.bikedump.utility.TimeUtility;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class GPXDocument extends GPSDocument {

    public GPXDocument(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        GpxDocument doc = GpxDocument.Factory.parse(new File(gpsFile));
        GpxType gpx = doc.getGpx();
        for (TrkType track : gpx.getTrkArray()) {
            WptType last = null;
            for (TrksegType segment : track.getTrksegArray()) {
                WptType[] trackPoints = segment.getTrkptArray();
                for (int i = 0; i < trackPoints.length; i++) {
                    WptType current = segment.getTrkptArray(i);
                    double currentLat = current.getLat().doubleValue();
                    double currentLon = current.getLon().doubleValue();
                    //add coordinate element
                    addCoordinateElement(currentLat, currentLon);
                    Date currentTime = current.getTime() != null ? current.getTime().getTime():null;
                    if (last != null) {
                        double lastLat = last.getLat().doubleValue();
                        double lastLon = last.getLon().doubleValue();
                        double distance = GPSUtility.haversine(currentLat, currentLon, lastLat, lastLon);
                        totalDistance += distance;
                        //need to be objects since could be nullable
                        BigDecimal currentCalcEle = current.getEle();
                        BigDecimal lastCalcEle = last.getEle();
                        Date lastTime = null;
                        if(last.getTime() != null)
                            lastTime = last.getTime().getTime();
                        double heart = 0;
                        //calculate heart element
                        //FIXME indexOf evalution is really slow: optimize
                        /*ExtensionsTypeImpl ext = (ExtensionsTypeImpl)current.getExtensions();
                        if (ext != null) {
                            XmlObject[] array = ext.selectChildren(null, "gpxtpx:hr");
                            int i1 = ext.toString().indexOf("<gpxtpx:hr>") + 11;
                            int i2 = ext.toString().indexOf("</gpxtpx:hr>");
                            try {
                                heart = Double.valueOf(ext.toString().substring(i1, i2));
                                addHeart(heart);
                                if(heart > maxHeart)
                                    maxHeart = heart;
                            }
                            catch (Exception ex) {}

                        }    */
                        //add basic gps elements
                        createWaypointElement(
                                currentLat, currentLon, lastLat, lastLon, distance,
                                currentCalcEle, lastCalcEle, currentTime, lastTime, heart, totalDistance);
                        //calculate speed between points
                        double timeDiffInHour = TimeUtility.getTimeDiffHour(last.getTime(), current.getTime());
                        addSpeedElement(distance, timeDiffInHour);
                    }
                    last = current;
                }

            }
            result.add(createTrack(track, gpx, gpsFile));
        }
        return result;
    }

    private Track createTrack(TrkType track, GpxType gpx, String fileName) {
        Track resultTrack = new Track();
        resultTrack.setFileName(fileName);
        long diffStartEndTime = 0;
        try {
            startTime = track.getTrksegArray(0).getTrkptArray(0).getTime().getTime();
            TrksegType lastSegment = track.getTrksegArray(track.getTrksegArray().length - 1);
            endTime = lastSegment.getTrkptArray(lastSegment.getTrkptArray().length - 1).getTime().getTime();
            diffStartEndTime = endTime.getTime() - startTime.getTime();
        }
        catch(Exception ex) {
           //TODO define exception
        }

        resultTrack.setSportType(track.getType());
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
        resultTrack.setHeartFrequency(totalHeart / heart);
        resultTrack.setHeartMax(maxHeart);

        Author author = new Author();
        if (gpx.getMetadata() != null) {
            if (gpx.getMetadata().getAuthor() != null) {
                author.setName(gpx.getMetadata().getAuthor().getName());
            }
            if (gpx.getMetadata().getAuthor() != null && gpx.getMetadata().getAuthor().getEmail() != null) {
                author.setEmail(
                        gpx.getMetadata().getAuthor().getEmail().getId()
                                + "@"
                                + gpx.getMetadata().getAuthor().getEmail().getDomain()
                );
            }
        }
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

        return resultTrack;
    }


}
