package org.hifly.bikedump.gui.menu;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooser extends JFileChooser {

    private static final long serialVersionUID = 17L;

    public FileChooser() {
        super();
        init();
    }

    private void init() {
        this.setAcceptAllFileFilterUsed(true);
        this.setMultiSelectionEnabled(false);
        FileFilter gpxType = new FileNameExtensionFilter("Gpx files (.gpx)", "gpx");
        FileFilter tcxType = new FileNameExtensionFilter("Tcx files (.tcx)", "tcx");
        this.addChoosableFileFilter(gpxType);
        this.addChoosableFileFilter(tcxType);
        //initial filter
        this.setFileFilter(gpxType);
    }
}
