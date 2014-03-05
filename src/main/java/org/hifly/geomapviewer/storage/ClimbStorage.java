package org.hifly.geomapviewer.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;

import java.io.*;
import java.util.*;

/**
 * @author
 * @date 04/03/14
 */
public class ClimbStorage {

    public static void saveClimb(SlopeSegment slope, String slopeName) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos =
                    new FileOutputStream(
                            System.getProperty("user.home") + "/.geomapviewer/climb/" + slopeName + ".db");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(slope);
            oos.close();
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

    public static List<SlopeSegment> readSavedClimbs() {
        File root = new File(System.getProperty("user.home") + "/.geomapviewer/climb/");
        Collection files = FileUtils.listFiles(root, null, true);
        List<SlopeSegment> slopes = new ArrayList();
        FileInputStream streamIn = null;
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            if (ext.equalsIgnoreCase("db")) {
                try {
                    streamIn = new FileInputStream(file);
                    ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
                    SlopeSegment slope = (SlopeSegment) objectinputstream.readObject();
                    slopes.add(slope);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        if (streamIn != null) {
            try {
                streamIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return slopes;
    }

}
