package org.hifly.bikedump.gps;

import net.opengis.kml.x22.AbstractStyleSelectorType;
import net.opengis.kml.x22.KmlDocument;
import net.opengis.kml.x22.KmlType;
import net.opengis.kml.x22.impl.StyleTypeImpl;
import org.hifly.bikedump.domain.Author;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.SlopeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class KML22Document extends GPSDocument {
    private Logger log = LoggerFactory.getLogger(KML22Document.class);

    public KML22Document(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        String trackName = null;
        String sportType = null;

        KmlDocument doc = KmlDocument.Factory.parse(new File(gpsFile));
        KmlType kml = doc.getKml();
        log.info(kml.getAbstractFeatureGroup().getName());


        AbstractStyleSelectorType[] ass = kml.getAbstractFeatureGroup().getAbstractStyleSelectorGroupArray();
        for(AbstractStyleSelectorType as:ass) {
            log.info(as.getClass().getName());
            if(as.getClass().getName().equals("net.opengis.kml.x22.impl.StyleTypeImpl")) {
                StyleTypeImpl ss = (StyleTypeImpl)as;
                log.info(ss.getId());
            }
        }


      /*  for (TrkType track : kml.getKmlObjectExtensionGroupArray()[0].) {
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
                        double distance = GpsUtility.haversine(currentLat, currentLon,lastLat, lastLon);
                        totalDistance += distance;
                        //need to be objects since could be nullable
                        BigDecimal currentCalcEle = current.getEle();
                        BigDecimal lastCalcEle = last.getEle();
                        Date lastTime = last.getTime().getTime();
                        //add basic gps elements
                        createWaypointElement(gpsFile, currentLat, currentLon, lastLat, lastLon, distance, currentCalcEle, lastCalcEle, currentTime, lastTime, totalDistance);
                        //calculate speed between points
                        double timeDiffInHour = TimeUtility.getTimeDiffHour(last.getTime(), current.getTime());
                        addSpeedElement(currentLat, currentLon, distance, timeDiffInHour);
                    }
                    last = current;
                }

            }
            result.add(createTrack(track, gpx, gpsFile));
        }  */

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
        if(resultTrack.getCoordinatesNewKm() != null) {
            resultTrack.setStatsNewKm(GPSUtility.calculateStatsInUnit(resultTrack.getCoordinatesNewKm()));
        }

        result.add(resultTrack);

        return resultTrack;
    }

    public static void main(String[] args)  {
        KML22Document doc= new KML22Document(null);
        List<Track> tracks = null;
        try {
            tracks = doc.extractTrack("time-stamp-point.kml");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



}
