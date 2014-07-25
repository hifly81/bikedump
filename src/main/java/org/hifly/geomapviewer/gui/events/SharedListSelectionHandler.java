package org.hifly.geomapviewer.gui.events;

import org.hifly.geomapviewer.gui.panel.TrackTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashSet;

public class SharedListSelectionHandler implements ListSelectionListener {

    private TrackTable table;
    private HashSet<String> selectedTrackNames;

    public SharedListSelectionHandler(TrackTable table, HashSet<String> selectedTrackNames) {
          this.table = table;
          this.selectedTrackNames = selectedTrackNames;
    }

    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

        if (!lsm.isSelectionEmpty()) {
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    Object fileKey = table.getValueAt(i, 1);
                    if (fileKey != null) {
                        selectedTrackNames.add(fileKey.toString());
                    }
                }
            }
        }

    }

    public HashSet<String> getSelectedTrackNames() {
        return selectedTrackNames;
    }

    public void setSelectedTrackNames(HashSet<String> selectedTrackNames) {
        this.selectedTrackNames = selectedTrackNames;
    }
}