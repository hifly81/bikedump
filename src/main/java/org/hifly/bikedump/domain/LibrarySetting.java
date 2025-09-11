package org.hifly.bikedump.domain;

import java.io.Serializable;
import java.util.List;

public class LibrarySetting implements Serializable {

    private static final long serialVersionUID = 5L;

    private List<String> scannedDirs;
    private boolean scanFolder;
    private String offlineTilesPath;
    private boolean useOfflineTiles;

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

    public String getOfflineTilesPath() {
        return offlineTilesPath;
    }

    public void setOfflineTilesPath(String offlineTilesPath) {
        this.offlineTilesPath = offlineTilesPath;
    }

    public boolean isUseOfflineTiles() {
        return useOfflineTiles;
    }

    public void setUseOfflineTiles(boolean useOfflineTiles) {
        this.useOfflineTiles = useOfflineTiles;
    }
}
