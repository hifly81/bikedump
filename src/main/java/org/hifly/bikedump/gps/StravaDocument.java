package org.hifly.bikedump.gps;


import org.apache.commons.lang.StringUtils;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class StravaDocument extends GPSDocument {

    public StravaDocument(ProfileSetting profileSetting) {
        super(profileSetting);
    }

    @Override
    public List<Track> extractTrack(String gpsFile) throws Exception {
        Properties properties = null;
        FileInputStream in = null;
        try {
            File file = new File(gpsFile+".prop");
            in = new FileInputStream(file);
            properties = new Properties();
            properties.load(in);
            in.close();
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String distance = properties.getProperty("distance");
        if(StringUtils.isNotEmpty(distance))  {
            totalDistance = Double.valueOf(distance);
        }

        String heart = properties.getProperty("maxHeart");
        if(StringUtils.isNotEmpty(heart))  {
            maxHeart = Double.valueOf(heart);
        }

        try(BufferedReader br = new BufferedReader(new FileReader(gpsFile))) {
            int i = 0;
            for(String line; (line = br.readLine()) != null; i++) {
                String[] lineElements = line.split(";");
                double currentLat = Double.valueOf(lineElements[0]);
                double currentLon = Double.valueOf(lineElements[1]);
                //add coordinate element
                addCoordinateElement(currentLat, currentLon);

            }
            // line is not visible here.
        }


        return null;
    }
}
