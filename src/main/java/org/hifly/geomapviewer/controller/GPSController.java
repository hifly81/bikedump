package org.hifly.geomapviewer.controller;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPXDocument;

import java.util.List;

/**
 * @author
 * @date 12/02/14
 */
public class GPSController {

    public static Track extractTrackFromGpx(String filename) {
        Track track = null;
        GPXDocument doc = new GPXDocument();
        List<Track> tracks = null;
        try {
            tracks = doc.extractTrack(filename);
        }
        catch (Exception e) {
            //TODO exception
            e.printStackTrace();
        }
        //TODO manage a list of tracks
        if(tracks!=null && !tracks.isEmpty()) {
             track = tracks.get(0);
        }
        return track;
    }
}
