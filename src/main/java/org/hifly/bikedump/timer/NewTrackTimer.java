package org.hifly.bikedump.timer;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.BikeDump;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.utility.TimeUtility;

import java.text.SimpleDateFormat;
import java.util.*;


public class NewTrackTimer {

    private BikeDump bk;

    class FindNewTrack extends TimerTask {

        public void run() {
            List<List<Coordinate>> coordinates = new ArrayList();
            List<Map<String, WaypointSegment>> waypoint = new ArrayList();
            List<Track> tracks = new ArrayList();
            StringBuffer sb = new StringBuffer();
            bk.checkNewTrack(coordinates, waypoint, tracks, sb);

            if(!tracks.isEmpty()) {
                Vector<Object> rowData;
                SimpleDateFormat dt1 = new SimpleDateFormat(TimeUtility.ITA_DATE_FORMAT);
                for(Track track:tracks) {
                    rowData = new Vector(6);
                    String startDate;
                    if (track.getStartDate() == null)
                        startDate = "";
                    else
                        startDate = dt1.format(track.getStartDate());
                    rowData.add(startDate);
                    rowData.add(track.getName());
                    rowData.add(String.format("%.2f", Double.isNaN(track.getTotalDistance()) ? 0 : track.getTotalDistance()));
                    rowData.add(TimeUtility.toStringFromTimeDiff(track.getRealTime()));
                    rowData.add(String.format("%.2f", Double.isNaN(track.getCalculatedAvgSpeed()) ? 0 : track.getCalculatedAvgSpeed()));
                    rowData.add(String.format("%.2f", Double.isNaN(track.getRealElevation()) ? 0 : track.getRealElevation()));
                    //update table model
                    //((DefaultTableModel)bk.trackTable.getModel()).addRow(rowData);
                }

                //add founded tracks
                DataHolder.tracksLoaded.addAll(tracks);
            }

        }
    }

    public NewTrackTimer(BikeDump bk) {
        this.bk = bk;
        Timer timer = new Timer();
        //TODO schedule must be in settings
        timer.schedule(new FindNewTrack(), 0, 10000);
    }
}
