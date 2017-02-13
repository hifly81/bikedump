package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.gui.panel.TrackTable;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashSet;

public class TableSelectionHandler implements ListSelectionListener {

    private TrackTable table;

    public TableSelectionHandler(TrackTable table, HashSet<String> selectedTrackNames) {
          this.table = table;
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
                    Object fileKey = table.getValueAt(i, 1);
                    if (fileKey != null)
                        DataHolder.tracksSelected.add(fileKey.toString());
                }
            }
        }

    }

}