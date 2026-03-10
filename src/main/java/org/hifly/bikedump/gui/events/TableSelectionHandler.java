package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.gui.panel.TrackTable;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashSet;

public class TableSelectionHandler implements ListSelectionListener {

    private final TrackTable table;
    private final Bikedump bikeDump;

    public TableSelectionHandler(Bikedump bikeDump, TrackTable table, HashSet<TrackSelected> selectedTrackNames) {
        this.table = table;
        this.bikeDump = bikeDump;
        DataHolder.tracksSelected = selectedTrackNames;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
            DataHolder.tracksSelected.clear();
            return;
        }

        // Rebuild selection buffer from the current JTable selection
        DataHolder.tracksSelected.clear();

        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Track track = ((TrackTable.TrackTableModel) table.getModel()).getTrackAt(modelRow);
            if (track != null) {
                DataHolder.tracksSelected.add(new TrackSelected(track.getFileName()));
            }
        }

        // If single selection -> load immediately (current behavior)
        // If multi selection -> DO NOT auto-load; user can press Enter to load aggregate
        if (selectedRows.length == 1) {
            bikeDump.loadSelectedTracks(table);
        }
    }
}