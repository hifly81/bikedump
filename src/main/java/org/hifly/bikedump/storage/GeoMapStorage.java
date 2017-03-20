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
            streamIn = new FileInputStream(System.getProperty("user.home")+"/.geomapviewer/coordinates/storage_coordinates_kyro.db");
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
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                input.close();
                streamIn.close();
            }
            catch (IOException e) {
            }
        }

    }

    public static void save() {
        if(GeoMapStorage.tracksLibrary != null) {
            PrefStorage.savePref(GeoMapStorage.tracksLibrary, "tracks");
        }
        if(GeoMapStorage.profileSetting != null) {
            PrefStorage.savePref(GeoMapStorage.profileSetting, "profile");
        }
        if(GeoMapStorage.librarySetting != null) {
            PrefStorage.savePref(GeoMapStorage.librarySetting, "library");
        }
        if(GeoMapStorage.stravaSetting != null) {
            StravaStorage.saveActivities(GeoMapStorage.stravaSetting, "strava");
        }
        if(GeoMapStorage.savedClimbsList != null) {
            for(SlopeSegment slope: GeoMapStorage.savedClimbsList) {
                ClimbStorage.saveClimb(slope, slope.getName());
            }
        }
    }
}
