package org.hifly.bikedump.domain;

import java.io.Serializable;
import java.util.List;

public class LibrarySetting implements Serializable {

    private static final long serialVersionUID = 4L;

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
