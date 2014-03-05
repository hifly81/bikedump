package org.hifly.geomapviewer.storage;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 05/03/14
 */
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

    public static Map<String, String> readOpenedTracks() {
        File file = new File(System.getProperty("user.home") + "/.geomapviewer/preferences/tracks.pref");
        FileInputStream streamIn = null;
        Map<String, String> map = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                map = (Map<String, String>) objectinputstream.readObject();
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
}
