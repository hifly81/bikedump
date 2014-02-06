package org.hifly.geomapviewer.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * @author
 * @date 02/02/14
 */
public class GPSStorage {

    public static Map<String, Double> gpsElevationMap;

    static {
        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(System.getProperty("user.home")+"/.geomapviewer/storage_coordinates.db");
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            gpsElevationMap = (Map<String, Double>) objectinputstream.readObject();
        }
        catch (Exception e) {}
        finally {
            try {
                streamIn.close();
            }
            catch (IOException e) {
            }
        }

    }
}
