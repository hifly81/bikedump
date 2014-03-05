package org.hifly.geomapviewer.controller;

import org.apache.xmlbeans.XmlException;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPSDocument;
import org.hifly.geomapviewer.gps.GPX10Document;
import org.hifly.geomapviewer.gps.GPXDocument;
import org.hifly.geomapviewer.gps.TCX2Document;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 12/02/14
 */
public class GPSController {


    public static Map.Entry<Track,StringBuffer> extractTrackFromGpx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        List<Track> tracks = null;
        GPSDocument doc = null;

        //TODO check version of gpx file
        try {
            doc = new GPXDocument(profileSetting);
            tracks = doc.extractTrack(filename);

        }
        catch(XmlException xe) {
            //TODO log exception message
            doc = new GPX10Document(profileSetting);
            try {
                tracks = doc.extractTrack(filename);
            }
            catch(XmlException xe2) {
                //TODO log exception message
                sb.append("["+filename+"] is not a gpx 1.0 or gpx 1.1 file");
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
                //TODO log exception message
                sb.append("Can't load ["+filename+"]"+ex2.getMessage());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            //TODO log exception message
            sb.append("Can't load ["+filename+"]"+ex.getMessage());
        }


        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }

        return new AbstractMap.SimpleImmutableEntry<Track, StringBuffer>(track, sb);
    }

    public static Map.Entry<Track,StringBuffer> extractTrackFromTcx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        TCX2Document doc = new TCX2Document(profileSetting);
        List<Track> tracks = null;
        try {
            tracks = doc.extractTrack(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO exception --> must be raised till GUI --> dialog popup
            sb.append("Can't load ["+filename+"]"+ex.getMessage());
        }
        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }
        return new AbstractMap.SimpleImmutableEntry<Track, StringBuffer>(track, sb);
    }
}
