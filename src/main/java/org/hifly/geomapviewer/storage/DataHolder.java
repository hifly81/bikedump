package org.hifly.geomapviewer.storage;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;

import java.util.*;

public class DataHolder {

    public static Map<String, String> mapFilePathTrack = new HashMap();
    public static List<Track> tracksLoaded;
    public static List<List<WaypointSegment>> listsWaypointSegment;
    public static HashSet<String> tracksSelected;

    public static HashMap<String, List<Track>> getTracksByMonth() {
        HashMap<String, List<Track>> tracksByMonth = new HashMap();
        if (CollectionUtils.isNotEmpty(tracksLoaded)) {
            Calendar cal = Calendar.getInstance();
            Date startDate;
            for (Track track : tracksLoaded) {
                startDate = track.getStartDate();
                if(startDate!=null) {
                    cal.setTime(startDate);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    String key = month + "-" + year;
                    List<Track> tracks;
                    if (!tracksByMonth.containsKey(key)) {
                        tracks = new ArrayList();
                    } else {
                        tracks = tracksByMonth.get(key);
                    }
                    tracks.add(track);
                    if (!tracksByMonth.containsKey(key)) {
                        tracksByMonth.put(key, tracks);
                    }
                }

            }

        }
        return tracksByMonth;
    }
}
