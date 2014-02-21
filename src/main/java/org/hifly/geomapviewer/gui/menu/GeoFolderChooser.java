package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author
 * @date 12/02/14
 */
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
