package org.hifly.geomapviewer.controller;

import org.apache.xmlbeans.XmlException;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPSDocument;
import org.hifly.geomapviewer.gps.GPX10Document;
import org.hifly.geomapviewer.gps.GPXDocument;
import org.hifly.geomapviewer.gps.TCX2Document;

import java.util.List;

/**
 * @author
 * @date 12/02/14
 */
public class GPSController {


    public static Track extractTrackFromGpx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        List<Track> tracks = null;
        GPSDocument doc = null;

        //TODO check version of gpx file
        try {
            doc = new GPXDocument(profileSetting);
            tracks = doc.extractTrack(filename);
        } catch (Exception ex) {
            //TODO log exception message
            ex.printStackTrace();
            doc = new GPX10Document(profileSetting);
            try {
                tracks = doc.extractTrack(filename);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }


        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }
        return track;
    }

    public static Track extractTrackFromTcx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        TCX2Document doc = new TCX2Document(profileSetting);
        List<Track> tracks = null;
        try {
            tracks = doc.extractTrack(filename);
        } catch (Exception e) {
            //TODO exception --> must be raised till GUI --> dialog popup
            e.printStackTrace();
        }
        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }
        return track;
    }
}
