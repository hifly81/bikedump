package org.hifly.geomapviewer.storage;

import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.LibrarySetting;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 02/02/14
 */
public class GeoMapStorage {

    public static Map<String, Double> gpsElevationMap;
    public static List<SlopeSegment> savedClimbsList;
    public static Map<String,String> tracksLibrary;
    public static List<Bike> savedBikesList;
    public static LibrarySetting librarySetting;

    static {
        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(System.getProperty("user.home")+"/.geomapviewer/storage_coordinates.db");
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            gpsElevationMap = (Map<String, Double>) objectinputstream.readObject();

            //load saved climbs
            savedClimbsList = ClimbStorage.readSavedClimbs();
            //load opened tracks
            tracksLibrary = PrefStorage.readOpenedTracks();
            //load saved bikes
            savedBikesList = PrefStorage.readSavedBikes();
            //load saved library
            librarySetting = PrefStorage.readLibrarySetting();
        }
        catch (Exception e) {}
        finally {
            try {
                streamIn.close();
            }
            catch (IOException e) {
            }
        }

    }
}
