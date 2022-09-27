package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.gui.BikeDump;
import org.hifly.bikedump.gui.panel.TrackTable;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashSet;

public class TableSelectionHandler implements ListSelectionListener {

    private TrackTable table;
    private BikeDump bikeDump;

    public TableSelectionHandler(BikeDump bikeDump, TrackTable table, HashSet<TrackSelected> selectedTrackNames) {
          this.table = table;
          this.bikeDump = bikeDump;
          DataHolder.tracksSelected = selectedTrackNames;
    }

    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

        if (!lsm.isSelectionEmpty()) {
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if(minIndex == maxIndex && !DataHolder.tracksSelected.isEmpty())
                    DataHolder.tracksSelected.clear();
                if (lsm.isSelectedIndex(i)) {
                    //this method finds the index inside the model regardless of sorting
                    Track track = ((TrackTable.TrackTableModel)table.getModel()).getTrackAt(table.convertRowIndexToModel(i));
                    if (track != null) {
                        DataHolder.tracksSelected.add(new TrackSelected(track.getFileName()));
                        bikeDump.loadSelectedTracks(table);
                    }
                }
            }
        }

    }

}