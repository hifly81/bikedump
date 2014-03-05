package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author
 * @date 12/02/14
 */
public class GeoFileChooser extends JFileChooser {

    public GeoFileChooser() {
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
