package org.hifly.geomapviewer.gui.events;

import org.hifly.geomapviewer.storage.GeoMapStorage;
import org.hifly.geomapviewer.storage.PrefStorage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author
 * @date 05/03/14
 */
public class PanelWindowAdapter extends WindowAdapter {

    public PanelWindowAdapter() {}

    public void windowClosing(WindowEvent e) {
        if(GeoMapStorage.tracksLibrary!=null) {
            PrefStorage.savePref(GeoMapStorage.tracksLibrary, "tracks");
        }
        if(GeoMapStorage.savedBikesList!=null) {
            PrefStorage.savePref(GeoMapStorage.savedBikesList, "bikes");
        }
        System.exit(0);
    }

}
