package org.hifly.bikedump.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.hifly.bikedump.domain.*;
import org.hifly.bikedump.domain.gps.SlopeSegment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hifly.bikedump.utility.Constants.HOME_FOLDER_NAME;

public class GeoMapStorage {

    public static Map<String, Double> gpsElevationMap;
    public static Map<String, Double> gpsElevationMapFallback = new HashMap<>();
    public static List<SlopeSegment> savedClimbsList;
    public static Map<String,TrackPref> tracksLibrary;
    public static ProfileSetting profileSetting;
    public static LibrarySetting librarySetting;

    static {
        FileInputStream streamIn = null;
        Input input = null;
        try {
            Path pathA = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME);
            if(Files.notExists(pathA))
                new File(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME).mkdirs();

            Path pathB = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates");
            if(Files.notExists(pathB))
                new File(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates").mkdirs();

            Path pathC = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "climb");
            if(Files.notExists(pathC))
                new File(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "climb").mkdirs();

            Path pathD = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "preferences");
            if(Files.notExists(pathD))
                new File(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "preferences").mkdirs();

            Path path = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates/storage_coordinates_kyro.db");
            if (Files.exists(path)) {
                // file exist
                streamIn = new FileInputStream(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates/storage_coordinates_kyro.db");
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
        if(GeoMapStorage.savedClimbsList != null) {
            for(SlopeSegment slope: GeoMapStorage.savedClimbsList) {
                ClimbStorage.saveClimb(slope, slope.getName());
            }
        }
    }
}
