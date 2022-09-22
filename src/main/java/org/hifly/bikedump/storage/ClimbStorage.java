package org.hifly.bikedump.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hifly.bikedump.domain.gps.SlopeSegment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hifly.bikedump.utility.Constants.HOME_FOLDER_NAME;

public class ClimbStorage {

    private static final String CLIMB_DIR = System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "climb" + File.separator;

    public static void saveClimb(SlopeSegment slope, String slopeName) {
        //TODO md5 name
        FileOutputStream fos = null;
        try {
            fos =
                    new FileOutputStream(
                            CLIMB_DIR + slopeName + ".db");
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

    public static List<SlopeSegment> readSavedClimbs() throws Exception {
        File root = new File(CLIMB_DIR);
        if (!root.exists()) {
            Path path = Paths.get(CLIMB_DIR);
            root = Files.createDirectories(path).toFile();
        }
        Collection files = FileUtils.listFiles(root, null, true);
        List<SlopeSegment> slopes = new ArrayList<>();
        FileInputStream streamIn = null;
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            if (ext.equalsIgnoreCase("db")) {
                ObjectInputStream objectinputstream = null;
                try {
                    streamIn = new FileInputStream(file);
                    objectinputstream = new ObjectInputStream(streamIn);
                    SlopeSegment slope = (SlopeSegment) objectinputstream.readObject();
                    slopes.add(slope);
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
