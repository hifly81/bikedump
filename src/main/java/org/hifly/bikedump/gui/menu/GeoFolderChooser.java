package org.hifly.bikedump.gui.menu;

import javax.swing.*;

public class GeoFolderChooser extends JFileChooser {

    private static final long serialVersionUID = 18L;

    public GeoFolderChooser() {
        super();
        init();
    }

    private void init() {
        this.setMultiSelectionEnabled(false);
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
}
