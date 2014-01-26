package org.hifly.geomapviewer.domain;

import java.util.List;

/**
 * @author
 * @date 26/01/14
 */
public abstract class GPSDocument {

    public abstract List<Track> extractTrack (String gpsFile);
}
