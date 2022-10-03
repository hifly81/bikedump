package org.hifly.bikedump.storage;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.TrackPref;

import java.io.*;
import java.util.Map;

import static org.hifly.bikedump.utility.Constants.HOME_FOLDER_NAME;

//TODO refactor read methods
public class PrefStorage {

    private static final String PREF_DIR = System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "preferences" + File.separator;

    public static void savePref(Object toSave, String filename) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(PREF_DIR + filename + ".pref");
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
        File file = new File(PREF_DIR + "tracks.pref");
        FileInputStream streamIn = null;
        Map<String, TrackPref> map = null;
        if (file != null && file.exists()) {
            ObjectInputStream objectinputstream = null;
            try {
                streamIn = new FileInputStream(file);
                objectinputstream = new ObjectInputStream(streamIn);
                map = (Map<String, TrackPref>) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(objectinputstream != null)
                        objectinputstream.close();
                } catch(Exception ex2) {
                    ex2.printStackTrace();
                }
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

    public static LibrarySetting readLibrarySetting() {
        File file = new File(PREF_DIR + "library.pref");
        FileInputStream streamIn = null;
        ObjectInputStream objectinputstream = null;
        LibrarySetting library = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                objectinputstream = new ObjectInputStream(streamIn);
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

            if (objectinputstream != null) {
                try {
                    objectinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return library;
    }

}
