package org.hifly.geomapviewer.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.hifly.geomapviewer.domain.*;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.strava.StravaSetting;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 02/02/14
 */
public class GeoMapStorage {

    public static Map<String, Double> gpsElevationMap;
    public static List<SlopeSegment> savedClimbsList;
    public static Map<String,TrackPref> tracksLibrary;
    public static ProfileSetting profileSetting;
    public static LibrarySetting librarySetting;
    public static StravaSetting stravaSetting;

    static {
        FileInputStream streamIn = null;
        Input input = null;
        try {
            streamIn = new FileInputStream(System.getProperty("user.home")+"/.geomapviewer/storage_coordinates_kyro.db");
            input = new Input(streamIn);
            Kryo kryo = new Kryo();
            gpsElevationMap = (Map<String, Double>)kryo.readObject(input, HashMap.class);

            //load saved climbs
            savedClimbsList = ClimbStorage.readSavedClimbs();
            //load opened tracks
            tracksLibrary = PrefStorage.readOpenedTracks();
            //load saved profile
            profileSetting = PrefStorage.readSavedProfileSetting();
            //load saved library
            librarySetting = PrefStorage.readLibrarySetting();
            //load strava setting
            stravaSetting = PrefStorage.readStravaSetting();
        }
        catch (Exception e) {}
        finally {
            try {
                input.close();
                streamIn.close();
            }
            catch (IOException e) {
            }
        }

    }
}
