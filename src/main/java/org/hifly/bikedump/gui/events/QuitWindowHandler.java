package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.storage.GeoMapStorage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class QuitWindowHandler extends WindowAdapter {

    public QuitWindowHandler() {}

    public void windowClosing(WindowEvent e) {
        GeoMapStorage.save();
        System.exit(0);
    }

}
