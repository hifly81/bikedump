package org.hifly.bikedump.task;


import org.apache.commons.io.FilenameUtils;
import org.hifly.bikedump.controller.GPSController;
import org.hifly.bikedump.domain.Profile;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackPref;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.storage.GeoMapStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadTrack implements Runnable {

    private File file;
    private ProfileSetting profileSetting;
    private StringBuffer sb;
    private List<List<Coordinate>> coordinates;
    private List<Map<String, WaypointSegment>> waypoint;
    private List<Track> tracks;

    public LoadTrack(File file, ProfileSetting profileSetting, StringBuffer sb, List<List<Coordinate>> coordinates, List<Map<String, WaypointSegment>> waypoint, List<Track> tracks) {
        this.file = file;
        this.profileSetting = profileSetting;
        this.sb = sb;
        this.coordinates = coordinates;
        this.waypoint = waypoint;
        this.tracks = tracks;
    }


    @Override
    public void run() {

        //TODO check if file already exists in Cache

        if (file.exists()) {

            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            Track track;
            Map.Entry<Track, StringBuffer> resultTrack;
            //TODO check not based on extension
            if (ext.equalsIgnoreCase("gpx"))
                resultTrack = GPSController.extractTrackFromGpx(file.getAbsolutePath(), profileSetting);
            else if (ext.equalsIgnoreCase("tcx"))
                resultTrack = GPSController.extractTrackFromTcx(file.getAbsolutePath(), profileSetting);
            else
                return;

            if (resultTrack != null && resultTrack.getValue().toString().equals("")) {
                track = resultTrack.getKey();
                if (track != null) {
                    //add to map
                    DataHolder.mapFilePathTrack.put(track.getName(), track.getFileName());
                    //add to opened files map
                    if (GeoMapStorage.tracksLibrary == null)
                        GeoMapStorage.tracksLibrary = new HashMap<>();
                    TrackPref trackPref = new TrackPref();
                    Profile profile = profileSetting.getSelectedProfile();
                    trackPref.setProfile(profile);

                    GeoMapStorage.tracksLibrary.put(track.getFileName(), trackPref);
                    coordinates.add(track.getCoordinates());
                    waypoint.add(track.getCoordinatesNewKm());
                    tracks.add(track);
                    if(track.getCoordinatesNewKm() !=null && !track.getCoordinatesNewKm().isEmpty()) {
                        List<WaypointSegment> listWaypoints =
                                new ArrayList<>(track.getCoordinatesNewKm().values());
                        DataHolder.listsWaypointSegment.add(listWaypoints);
                    }
                }
            } else
                sb.append(resultTrack.getValue().toString()).append("\n");
        }

    }
}
