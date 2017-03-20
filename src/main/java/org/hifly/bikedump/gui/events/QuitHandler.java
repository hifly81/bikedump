package org.hifly.bikedump.gui.events;

import org.hifly.bikedump.storage.GeoMapStorage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class QuitHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        GeoMapStorage.save();
        System.exit(0);
    }
}
