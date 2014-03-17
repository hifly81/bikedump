package org.hifly.geomapviewer.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 17/03/14
 */
public class LibrarySetting implements Serializable {

    private List<String> scannedDirs;
    private boolean scanFolder;

    public boolean isScanFolder() {
        return scanFolder;
    }

    public void setScanFolder(boolean scanFolder) {
        this.scanFolder = scanFolder;
    }

    public List<String> getScannedDirs() {
        return scannedDirs;
    }

    public void setScannedDirs(List<String> scannedDirs) {
        this.scannedDirs = scannedDirs;
    }
}
