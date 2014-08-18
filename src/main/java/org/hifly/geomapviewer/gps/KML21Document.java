package org.hifly.geomapviewer.gps;

import com.google.earth.kml.x21.KmlDocument;
import com.google.earth.kml.x21.KmlType;
import org.hifly.geomapviewer.domain.Author;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.SlopeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author
 * @date 30/01/14
 */
public class KML21Document extends GPSDocument {
    private Logger log = LoggerFactory.getLogger(KML21Document.class);

    public KML21Document(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        String trackName = null;
        String sportType = null;

        KmlDocument doc = KmlDocument.Factory.parse(new File(gpsFile));
        KmlType kml = doc.getKml();

        //TODO implementation
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
        resultTrack.setSlopes(SlopeUtility.extractSlope(waypoints, profileSetting));
        resultTrack.setCoordinates(coordinates);
        GpsUtility.GpsStats stats = GpsUtility.extractInfoFromWaypoints(waypoints, totalDistance);
        resultTrack.setCoordinatesNewKm(stats.getWaypointsKm());
        resultTrack.setMaxAltitude(stats.getMaxAltitude());
        resultTrack.setMinAltitude(stats.getMinAltitude());
        resultTrack.setClimbingSpeed(stats.getClimbingSpeed());
        resultTrack.setClimbingTimeMillis(stats.getClimbingTime());
        resultTrack.setClimbingDistance(stats.getClimbingDistance());
        resultTrack.setStatsNewKm(GpsUtility.calculateStatsInUnit(resultTrack.getCoordinatesNewKm()));

        result.add(resultTrack);

        return resultTrack;
    }

}
