package org.hifly.geomapviewer.gui.events;

import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.storage.ClimbStorage;
import org.hifly.geomapviewer.storage.GeoMapStorage;
import org.hifly.geomapviewer.storage.PrefStorage;
import org.hifly.geomapviewer.storage.StravaStorage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author
 * @date 05/03/14
 */
public class QuitAppHandler extends WindowAdapter {

    public QuitAppHandler() {}

    public void windowClosing(WindowEvent e) {
        if(GeoMapStorage.tracksLibrary != null) {
            PrefStorage.savePref(GeoMapStorage.tracksLibrary, "tracks");
        }
        if(GeoMapStorage.profileSetting != null) {
            PrefStorage.savePref(GeoMapStorage.profileSetting, "profile");
        }
        if(GeoMapStorage.librarySetting != null) {
            PrefStorage.savePref(GeoMapStorage.librarySetting, "library");
        }
        if(GeoMapStorage.stravaSetting != null) {
            StravaStorage.saveActivities(GeoMapStorage.stravaSetting, "strava");
        }
        if(GeoMapStorage.savedClimbsList != null) {
            for(SlopeSegment slope: GeoMapStorage.savedClimbsList) {
                ClimbStorage.saveClimb(slope, slope.getName());
            }
        }
        System.exit(0);
    }

}
