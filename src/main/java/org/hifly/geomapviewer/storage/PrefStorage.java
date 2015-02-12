package org.hifly.geomapviewer.storage;

import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.LibrarySetting;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.TrackPref;
import org.hifly.geomapviewer.domain.strava.StravaSetting;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 05/03/14
 */

//TODO refactor read methods
public class PrefStorage {

    public static void savePref(Object toSave, String filename) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos =
                    new FileOutputStream(
                            System.getProperty("user.home") + "/.geomapviewer/preferences/" + filename + ".pref");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(toSave);
            oos.close();
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

    public static Map<String, TrackPref> readOpenedTracks() {
        File file = new File(System.getProperty("user.home") + "/.geomapviewer/preferences/tracks.pref");
        FileInputStream streamIn = null;
        Map<String, TrackPref> map = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                map = (Map<String, TrackPref>) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (streamIn != null) {
                try {
                    streamIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static ProfileSetting readSavedProfileSetting() {
        File file = new File(System.getProperty("user.home") + "/.geomapviewer/preferences/profile.pref");
        FileInputStream streamIn = null;
        ProfileSetting profile = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                profile = (ProfileSetting) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (streamIn != null) {
                try {
                    streamIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return profile;
    }


    public static LibrarySetting readLibrarySetting() {
        File file = new File(System.getProperty("user.home") + "/.geomapviewer/preferences/library.pref");
        FileInputStream streamIn = null;
        LibrarySetting library = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                library = (LibrarySetting) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (streamIn != null) {
                try {
                    streamIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return library;
    }

}
