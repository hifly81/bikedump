package org.hifly.bikedump.gui.menu;

import javax.swing.*;

public class GeoFolderChooser extends JFileChooser {

    public GeoFolderChooser() {
        super();
        init();
    }

    private void init() {
        this.setMultiSelectionEnabled(false);
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
}
