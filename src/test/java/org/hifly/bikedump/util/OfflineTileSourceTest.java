package org.hifly.bikedump.util;

import java.io.File;

/**
 * Simple test for OfflineTileSource functionality
 */
public class OfflineTileSourceTest {
    
    public static void main(String[] args) {
        System.out.println("Testing OfflineTileSource...");
        
        // Test with existing test directory
        String testTilesDir = "/tmp/test_tiles";
        OfflineTileSource offlineSource = new OfflineTileSource(testTilesDir, "Test Offline Tiles");
        
        System.out.println("Tile source name: " + offlineSource.getName());
        System.out.println("Is available: " + offlineSource.isAvailable());
        System.out.println("Min zoom: " + offlineSource.getMinZoom());
        System.out.println("Max zoom: " + offlineSource.getMaxZoom());
        
        // Test tile URL generation
        String tileUrl = offlineSource.getTileUrl(10, 500, 300);
        System.out.println("Generated tile URL: " + tileUrl);
        
        if (tileUrl != null) {
            File tileFile = new File(tileUrl.replace("file:", ""));
            System.out.println("Tile file exists: " + tileFile.exists());
        }
        
        // Test with non-existent tile
        String nonExistentTileUrl = offlineSource.getTileUrl(10, 999, 999);
        System.out.println("Non-existent tile URL: " + nonExistentTileUrl);
        
        System.out.println("Test completed.");
    }
}