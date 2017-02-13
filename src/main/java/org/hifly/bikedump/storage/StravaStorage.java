package org.hifly.bikedump.storage;

import org.hifly.bikedump.domain.strava.StravaSetting;

import java.io.*;

public class StravaStorage {

    public static void saveActivities(Object toSave, String filename) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos =
                    new FileOutputStream(
                            System.getProperty("user.home") + "/.geomapviewer/strava/" + filename + ".pref");
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

    public static StravaSetting readStravaSetting() {
        File file = new File(System.getProperty("user.home") + "/.geomapviewer/strava/strava.pref");
        FileInputStream streamIn = null;
        StravaSetting strava = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                strava = (StravaSetting) objectinputstream.readObject();
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
        return strava;
    }
}
