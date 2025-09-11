package org.hifly.bikedump.util;

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTileSource;

import java.io.File;

/**
 * Offline tile source implementation for jmapviewer.
 * Reads tiles from local filesystem in standard TMS format.
 */
public class OfflineTileSource extends AbstractTileSource {

    private final String tileDir;

    /**
     * Create an offline tile source
     * @param tileDir Directory containing tiles in format {z}/{x}/{y}.png
     * @param name Display name for the tile source
     */
    public OfflineTileSource(String tileDir, String name) {
        super(name, null);
        this.tileDir = tileDir;
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) {
        // Return file URL for local tiles in TMS format: z/x/y.png
        File tileFile = new File(tileDir, zoom + "/" + tilex + "/" + tiley + ".png");
        if (tileFile.exists()) {
            return tileFile.toURI().toString();
        }
        // If tile doesn't exist, return null to allow fallback
        return null;
    }

    @Override
    public int getMaxZoom() {
        return 18; // Default max zoom for most tile sets
    }

    @Override
    public int getMinZoom() {
        return 0;
    }

    /**
     * Check if the tile directory exists and contains tiles
     */
    public boolean isAvailable() {
        File dir = new File(tileDir);
        return dir.exists() && dir.isDirectory();
    }
}