package org.hifly.bikedump.noCommit;

/**
 * @author
 * @date 31/01/14
 */
import com.garmin.xmlschemas.trainingCenterDatabase.v2.*;

import java.io.*;

/**
 * A tile with elevation data.
 *
 * @author Robert "robekas", Christian Pesch
 */

public class ElevationTile2 {
    /** 1200 Intervals means 1201 positions per line and column */
    private static final int SRTM3_INTERVALS = 1200;
    private static final int SRTM3_FILE_SIZE = (SRTM3_INTERVALS + 1) * (SRTM3_INTERVALS + 1) * 2;
    private static final int SRTM1_INTERVALS = 3600;
    public static final int SRTM1_FILE_SIZE = (SRTM1_INTERVALS + 1) * (SRTM1_INTERVALS + 1) * 2;
    private static final int INVALID_VALUE_LIMIT = -15000; // Won't interpolate below this elevation in Meters, guess is: -0x8000

    private final RandomAccessFile file;

    public ElevationTile2(RandomAccessFile file) {
        this.file = file;
    }

    private int getIntervalCount() throws IOException {
        long fileLength = file.length();
        if(fileLength == SRTM3_FILE_SIZE)
            return SRTM3_INTERVALS;
        else if(fileLength == SRTM1_FILE_SIZE)
            return SRTM1_INTERVALS;
        else
            throw new IOException("Elevation tile " + file + " has invalid size " + fileLength);
    }

    /**
     * Calculate the elevation for the destination position according the
     * theorem on intersecting lines ("Strahlensatz").
     *
     * @param dHeight12 the delta height/elevation of two sub tile positions
     * @param dLength12 the length of an sub tile interval (1 / intervals)
     * @param dDiff     the distance of the real point from the sub tile position
     * @return the delta elevation (relative to sub tile position)
     */
    private double calculateElevation(double dHeight12, double dLength12, double dDiff) {
        return (dHeight12 * dDiff) / dLength12;
    }

    public Double getElevationFor(Double longitude, Double latitude) throws IOException {
        if (file == null || longitude == null || latitude == null)
            return null;

        // cut off the decimal places
        int longitudeAsInt = longitude.intValue();
        int latitudeAsInt = latitude.intValue();

        if (longitude < 0) {                                        // If it's west longitude (negative value)
            longitudeAsInt = (longitudeAsInt - 1) * -1;             // Make a positive number (left edge)
            longitude = ((double) longitudeAsInt + longitude) + (double) longitudeAsInt; // Make positive double longitude (needed for later calculation)
        }

        if (latitude < 0) {                                        // If it's a south gps (negative value)
            latitudeAsInt = (latitudeAsInt - 1) * -1;              // Make a positive number (bottom edge)
            latitude = ((double) latitudeAsInt + latitude) + (double) latitudeAsInt; // Make positive double gps (needed for later calculation)
        }

        int intervalCount = getIntervalCount();
        int longitudeIntervalIndex = (int) ((longitude - (double) longitudeAsInt) * intervalCount);
        int latitudeIntervalIndex = (int) ((latitude - (double) latitudeAsInt) * intervalCount);

        if (longitudeIntervalIndex >= intervalCount) {
            longitudeIntervalIndex = intervalCount - 1;
        }

        if (latitudeIntervalIndex >= intervalCount) {
            latitudeIntervalIndex = intervalCount - 1;
        }

        double dOffLon = longitude - (double) longitudeAsInt;                    // The longitude value offset within a tile
        double dOffLat = latitude - (double) latitudeAsInt;                      // The gps value offset within a tile

        double dLeftTop;                                            // The left top position of a sub tile
        double dLeftBottom;                                         // The left bottom position of a sub tile
        double dRightTop;                                           // The right top position of a sub tile
        double dRightBottom;                                        // The right bootm position of a sub tile
        int pos;                                                    // The index of the elevation into the hgt file

        pos = (((intervalCount - latitudeIntervalIndex) - 1) * (intervalCount + 1)) + longitudeIntervalIndex; // The index for the left top elevation
        file.seek(pos * 2);                                // We have 16-bit values for elevation, so multiply by 2
        dLeftTop = file.readShort();                       // Now read the left top elevation from hgt file

        pos = ((intervalCount - latitudeIntervalIndex) * (intervalCount + 1)) + longitudeIntervalIndex; // The index for the left bottom elevation
        file.seek(pos * 2);                                // We have 16-bit values for elevation, so multiply by 2
        dLeftBottom = file.readShort();                    // Now read the left bottom elevation from hgt file

        pos = (((intervalCount - latitudeIntervalIndex) - 1) * (intervalCount + 1)) + longitudeIntervalIndex + 1; // The index for the right top elevation
        file.seek(pos * 2);                                // We have 16-bit values for elevation, so multiply by 2
        dRightTop = file.readShort();                      // Now read the right top elevation from hgt file

        pos = ((intervalCount - latitudeIntervalIndex) * (intervalCount + 1)) + longitudeIntervalIndex + 1; // The index for the right bottom elevation
        file.seek(pos * 2);                                // We have 16-bit values for elevation, so multiply by 2
        dRightBottom = file.readShort();                   // Now read the right bottom top elevation from hgt file

        // if one of the read elevation values is not valid, we cannot interpolate
        if ((dLeftTop < INVALID_VALUE_LIMIT) || (dLeftBottom < INVALID_VALUE_LIMIT) ||
                (dRightTop < INVALID_VALUE_LIMIT) || (dRightBottom < INVALID_VALUE_LIMIT)) {
            return null;
        }

        // the delta between top lat value and requested gps (offset within a sub tile)
        double dDeltaLon = dOffLon - (double) longitudeIntervalIndex * (1.0 / (double) intervalCount);
        // the delta between left lon value and requested longitude (offset within a sub tile)
        double dDeltaLat = dOffLat - (double) latitudeIntervalIndex * (1.0 / (double) intervalCount);

        // the interpolated elevation calculated from left top to left bottom
        double dLonHeightLeft = dLeftBottom - calculateElevation(dLeftBottom - dLeftTop, 1.0 / (double) intervalCount, dDeltaLat);
        // the interpolated elevation calculated from right top to right bottom
        double dLonHeightRight = dRightBottom - calculateElevation(dRightBottom - dRightTop, 1.0 / (double) intervalCount, dDeltaLat);

        // interpolate between the interpolated left elevation and interpolated right elevation
        double dElevation = dLonHeightLeft - calculateElevation(dLonHeightLeft - dLonHeightRight, 1.0 / (double) intervalCount, dDeltaLon);
        // round the interpolated elevation
        return dElevation + 0.5;
    }

    public static void main(String[]args) throws Exception {
        File file = new File(
                "/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/elevation_stats/farfa2_PTP_2013_ele.tcx");
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        System.out.println("Parsing "+file.getName());

        TrainingCenterDatabaseDocument doc =
                TrainingCenterDatabaseDocument.Factory.parse(
                        new File("/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/farfa2013/farfa2_PTP_2013.tcx"));
        ActivityListT actList = doc.getTrainingCenterDatabase().getActivities();
        ActivityT[] actArray = actList.getActivityArray();
        for(ActivityT act:actArray) {
            ActivityLapT[] lapArray = act.getLapArray();
            for(ActivityLapT lap:lapArray) {
                TrackT[] trackArray = lap.getTrackArray();
                for(TrackT tract:trackArray) {
                    TrackpointT[] trackPointArray = tract.getTrackpointArray();
                    for(TrackpointT trackPoint:trackPointArray) {
                        RandomAccessFile raf = null;
                        if(trackPoint.getPosition()!=null && trackPoint.getPosition().getLatitudeDegrees()!=0
                                && trackPoint.getPosition().getLongitudeDegrees()!=0) {
                            raf = new RandomAccessFile("/home/hifly/projects/geomapviewer/src/main/resources/hgt/N42E012.hgt", "rw");
                            ElevationTile2 tile = new ElevationTile2(raf);
                            String startLat = String.valueOf(trackPoint.getPosition().getLatitudeDegrees());
                            String startLon = String.valueOf(trackPoint.getPosition().getLongitudeDegrees());
                            if(startLat.startsWith("42") && startLon.startsWith("12")) {
                                double elevation = tile.getElevationFor(
                                        trackPoint.getPosition().getLongitudeDegrees(),
                                        trackPoint.getPosition().getLatitudeDegrees());
                                if(elevation==0) {
                                    System.out.println(
                                            trackPoint.getPosition().getLatitudeDegrees()+"-"
                                            +trackPoint.getPosition().getLongitudeDegrees()+"-"+elevation);
                                }
                                output.write(
                                        trackPoint.getPosition().getLatitudeDegrees()+"-"+
                                                trackPoint.getPosition().getLongitudeDegrees()+"-"+elevation+"\n");
                            }
                            else {
                                System.out.println("Point out of file:"+
                                        trackPoint.getPosition().getLatitudeDegrees()+"-"+trackPoint.getPosition().getLongitudeDegrees());
                            }
                        }
                        raf.close();
                    }
                }
            }
        }


        System.out.println("End Parsing "+file.getName());

        output.close();

    }
}
