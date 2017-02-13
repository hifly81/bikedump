package org.hifly.bikedump.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.hifly.bikedump.domain.*;
import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.strava.StravaSetting;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //FIXME put .geomapviewer dir
            streamIn = new FileInputStream(System.getProperty("user.home")+"/Dropbox/CYCLING_MTB/ROUTE/elevation_stats/storage_coordinates_kyro.db");
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
            stravaSetting = StravaStorage.readStravaSetting();
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
