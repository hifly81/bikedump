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
    public static LibrarySetting librarySetting;

    public static StravaPref stravaPref;

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

            // NEW: strava export dir
            Path pathE = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "strava" + File.separator + "exports");
            if(Files.notExists(pathE))
                new File(pathE.toString()).mkdirs();

            Path path = Paths.get(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates/storage_coordinates_kyro.db");
            if (Files.exists(path)) {
                streamIn = new FileInputStream(System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "coordinates/storage_coordinates_kyro.db");
                input = new Input(streamIn);
                Kryo kryo = new Kryo();
                gpsElevationMap = (Map<String, Double>)kryo.readObject(input, HashMap.class);
            }

            savedClimbsList = ClimbStorage.readSavedClimbs();
            tracksLibrary = PrefStorage.readOpenedTracks();
            librarySetting = PrefStorage.readLibrarySetting();

            stravaPref = PrefStorage.readStravaPref();
            if (stravaPref == null) stravaPref = new StravaPref();

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
        if(GeoMapStorage.librarySetting != null) {
            PrefStorage.savePref(GeoMapStorage.librarySetting, "library");
        }
        if (GeoMapStorage.stravaPref != null) {
            PrefStorage.savePref(GeoMapStorage.stravaPref, "strava");
        }
    }

    public static String getStravaExportsDir() {
        return System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "strava" + File.separator + "exports" + File.separator;
    }
}