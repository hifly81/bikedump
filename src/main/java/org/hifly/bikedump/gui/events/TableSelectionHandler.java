package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.gui.table.TrackTable;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashSet;

public class TableSelectionHandler implements ListSelectionListener {

    private final TrackTable table;
    private final Bikedump bikeDump;

    private volatile boolean shiftDown = false;

    public TableSelectionHandler(Bikedump bikeDump, TrackTable table, HashSet<TrackSelected> selectedTrackNames) {
        this.table = table;
        this.bikeDump = bikeDump;
        DataHolder.tracksSelected = selectedTrackNames;
    }

    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    protected boolean isShiftDown() {
        return shiftDown;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        if (table.getSelectionModel().isSelectionEmpty()) {
            DataHolder.tracksSelected.clear();
            return;
        }

        // While SHIFT is down, do not auto-load (we load on SHIFT release)
        if (isShiftDown()) {
            return;
        }

        rebuildSelectedTracksFromTable();

        if (!DataHolder.tracksSelected.isEmpty()) {
            bikeDump.loadSelectedTracks(table);
        }
    }

    public void rebuildSelectedTracksFromTable() {
        DataHolder.tracksSelected.clear();

        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Track track = ((TrackTable.TrackTableModel) table.getModel()).getTrackAt(modelRow);
            if (track != null) {
                DataHolder.tracksSelected.add(new TrackSelected(track.getFileName(), track.getName()));
            }
        }
    }
}