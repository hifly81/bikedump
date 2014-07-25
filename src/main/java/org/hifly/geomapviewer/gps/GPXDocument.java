package org.hifly.geomapviewer.gps;

import com.topografix.gpx.x1.x1.*;
import org.hifly.geomapviewer.domain.Author;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.SlopeUtility;
import org.hifly.geomapviewer.utility.TimeUtility;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @date 30/01/14
 */
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
                    Date currentTime = current.getTime().getTime();
                    if (last != null) {
                        double lastLat = last.getLat().doubleValue();
                        double lastLon = last.getLon().doubleValue();
                        double distance = GpsUtility.haversine(currentLat, currentLon, lastLat, lastLon);
                        totalDistance += distance;
                        //need to be objects since could be nullable
                        BigDecimal currentCalcEle = current.getEle();
                        BigDecimal lastCalcEle = last.getEle();
                        Date lastTime = last.getTime().getTime();
                        //add basic gps elements
                        addGPSElement(gpsFile, currentLat, currentLon, lastLat, lastLon, distance, currentCalcEle, lastCalcEle, currentTime, lastTime, totalDistance);
                        //calculate speed between points
                        double timeDiffInHour = TimeUtility.getTimeDiffHour(last.getTime(), current.getTime());
                        addSpeedElement(gpsFile, currentLat, currentLon, distance, timeDiffInHour);
                        //TODO calculate heart
                        ExtensionsType ext = current.getExtensions();
                        if (ext != null) {
                            int i1 = ext.toString().indexOf("<gpxtpx:hr>") + 11;
                            int i2 = ext.toString().indexOf("</gpxtpx:hr>");
                            try {
                                double heart = Double.valueOf(ext.toString().substring(i1, i2));
                                addHeart(heart);
                                if(heart > maxHeart) {
                                    maxHeart = heart;
                                }
                            }
                            catch (Exception ex) {}

                        }
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
        startTime = track.getTrksegArray(0).getTrkptArray(0).getTime().getTime();
        TrksegType lastSegment = track.getTrksegArray(track.getTrksegArray().length - 1);
        endTime = lastSegment.getTrkptArray(lastSegment.getTrkptArray().length - 1).getTime().getTime();
        long diffStartEndTime = endTime.getTime() - startTime.getTime();

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
        resultTrack.setSlopes(SlopeUtility.extractSlope(waypoints, profileSetting));
        resultTrack.setCoordinates(coordinates);
        GpsUtility.GpsStats stats = GpsUtility.extractInfoFromWaypoints(waypoints, totalDistance);
        resultTrack.setCoordinatesNewKm(stats.getWaypointsKm());
        resultTrack.setMaxAltitude(stats.getMaxAltitude());
        resultTrack.setMinAltitude(stats.getMinAltitude());
        resultTrack.setClimbingSpeed(stats.getClimbingSpeed());
        resultTrack.setClimbingTimeMillis(stats.getClimbingTime());
        resultTrack.setClimbingDistance(stats.getClimbingDistance());
        resultTrack.setStatsNewKm(GpsUtility.calculateStatsFromKm(resultTrack.getCoordinatesNewKm()));

        return resultTrack;
    }


}



