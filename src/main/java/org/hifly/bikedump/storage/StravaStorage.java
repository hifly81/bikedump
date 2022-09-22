package org.hifly.bikedump.storage;

import org.hifly.bikedump.domain.strava.StravaSetting;

import java.io.*;

import static org.hifly.bikedump.utility.Constants.HOME_FOLDER_NAME;

public class StravaStorage {

    private static final String STRAVA_DIR = System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "strava" + File.separator;

    public static void saveActivities(Object toSave, String filename) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos =
                    new FileOutputStream(
                            STRAVA_DIR + filename + ".pref");
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
        File file = new File(STRAVA_DIR + "strava.pref");
        FileInputStream streamIn = null;
        ObjectInputStream objectinputstream = null;
        StravaSetting strava = null;
        if (file != null && file.exists()) {
            try {
                streamIn = new FileInputStream(file);
                objectinputstream = new ObjectInputStream(streamIn);
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

            if (objectinputstream != null) {
                try {
                    objectinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return strava;
    }
}
