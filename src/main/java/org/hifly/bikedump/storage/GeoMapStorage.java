package org.hifly.bikedump.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.hifly.bikedump.domain.*;
import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.strava.StravaSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoMapStorage {

    public static Map<String, Double> gpsElevationMap;
    public static Map<String, Double> gpsElevationMapFallback = new HashMap<>();
    public static List<SlopeSegment> savedClimbsList;
    public static Map<String,TrackPref> tracksLibrary;
    public static ProfileSetting profileSetting;
    public static LibrarySetting librarySetting;
    public static StravaSetting stravaSetting;

    static {
        FileInputStream streamIn = null;
        Input input = null;
        try {
            Path pathA = Paths.get(System.getProperty("user.home")+"/.geomapviewer");
            if(Files.notExists(pathA))
                new File(System.getProperty("user.home")+"/.geomapviewer").mkdirs();

            Path pathB = Paths.get(System.getProperty("user.home")+"/.geomapviewer/coordinates");
            if(Files.notExists(pathB))
                new File(System.getProperty("user.home")+"/.geomapviewer/coordinates").mkdirs();

            Path pathC = Paths.get(System.getProperty("user.home")+"/.geomapviewer/climb");
            if(Files.notExists(pathC))
                new File(System.getProperty("user.home")+"/.geomapviewer/climb").mkdirs();

            Path pathD = Paths.get(System.getProperty("user.home")+"/.geomapviewer/preferences");
            if(Files.notExists(pathD))
                new File(System.getProperty("user.home")+"/.geomapviewer/preferences").mkdirs();

            Path pathE = Paths.get(System.getProperty("user.home")+"/.geomapviewer/strava");
            if(Files.notExists(pathE))
                new File(System.getProperty("user.home")+"/.geomapviewer/strava").mkdirs();


            Path path = Paths.get(System.getProperty("user.home")+"/.geomapviewer/coordinates/storage_coordinates_kyro.db");
            if (Files.exists(path)) {
                // file exist
                streamIn = new FileInputStream(System.getProperty("user.home")+"/.geomapviewer/coordinates/storage_coordinates_kyro.db");
                input = new Input(streamIn);
                Kryo kryo = new Kryo();
                gpsElevationMap = (Map<String, Double>)kryo.readObject(input, HashMap.class);
            }


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
                if(input != null)
                    input.close();
                if(streamIn != null)
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
