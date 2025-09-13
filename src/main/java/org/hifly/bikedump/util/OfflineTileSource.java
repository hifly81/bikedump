package org.hifly.bikedump.util;

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTileSource;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Offline tile source implementation for jmapviewer.
 * Reads tiles from local filesystem in standard TMS format.
 */
public class OfflineTileSource extends AbstractTileSource {

    private final String tileDir;
    private final String name;

    /**
     * Create an offline tile source
     * @param tileDir Directory containing tiles in format {z}/{x}/{y}.png
     * @param name Display name for the tile source
     */
    public OfflineTileSource(String tileDir, String name) {
        super();
        this.tileDir = tileDir;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public Map<String, String> getMetadata(Map<String, List<String>> headers) {
        // Return empty metadata map for offline tiles
        return null;
    }

    @Override
    public int getTileYMin(int zoom) {
        return 0;
    }

    @Override
    public int getTileYMax(int zoom) {
        return (int) Math.pow(2, zoom) - 1;
    }

    @Override
    public int getTileXMin(int zoom) {
        return 0;
    }

    @Override
    public int getTileXMax(int zoom) {
        return (int) Math.pow(2, zoom) - 1;
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate tileXYToLatLon(int xIndex, int yIndex, int zoom) {
        // Standard Web Mercator projection conversion
        double n = Math.pow(2, zoom);
        double lon_deg = xIndex / n * 360.0 - 180.0;
        double lat_rad = Math.atan(asinh(Math.PI * (1 - 2 * yIndex / n)));
        double lat_deg = Math.toDegrees(lat_rad);
        return new org.openstreetmap.gui.jmapviewer.Coordinate(lat_deg, lon_deg);
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate tileXYToLatLon(org.openstreetmap.gui.jmapviewer.Tile tile) {
        return tileXYToLatLon(tile.getXtile(), tile.getYtile(), tile.getZoom());
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate tileXYToLatLon(org.openstreetmap.gui.jmapviewer.TileXY tileXY, int zoom) {
        return tileXYToLatLon(tileXY.getXIndex(), tileXY.getYIndex(), zoom);
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.TileXY latLonToTileXY(org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate coord, int zoom) {
        return latLonToTileXY(coord.getLat(), coord.getLon(), zoom);
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.TileXY latLonToTileXY(double lat, double lon, int zoom) {
        // Convert lat/lon to tile coordinates
        double n = Math.pow(2, zoom);
        int x = (int) Math.floor((lon + 180.0) / 360.0 * n);
        double lat_rad = Math.toRadians(lat);
        // Use log instead of asinh for Java compatibility
        int y = (int) Math.floor((1.0 - Math.log(Math.tan(lat_rad) + 1.0 / Math.cos(lat_rad)) / Math.PI) / 2.0 * n);
        
        return new org.openstreetmap.gui.jmapviewer.TileXY(x, y);
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate xyToLatLon(int x, int y, int zoom) {
        // Standard Web Mercator projection conversion
        double n = Math.pow(2, zoom);
        double lon_deg = x / n * 360.0 - 180.0;
        double lat_rad = Math.atan(asinh(Math.PI * (1 - 2 * y / n)));
        double lat_deg = Math.toDegrees(lat_rad);
        return new org.openstreetmap.gui.jmapviewer.Coordinate(lat_deg, lon_deg);
    }

    @Override
    public org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate xyToLatLon(java.awt.Point point, int zoom) {
        return xyToLatLon(point.x, point.y, zoom);
    }

    @Override
    public java.awt.Point latLonToXY(org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate coord, int zoom) {
        return latLonToXY(coord.getLat(), coord.getLon(), zoom);
    }

    @Override
    public java.awt.Point latLonToXY(double lat, double lon, int zoom) {
        // Convert lat/lon to pixel coordinates
        double n = Math.pow(2, zoom);
        int x = (int) Math.floor((lon + 180.0) / 360.0 * n * 256);
        double lat_rad = Math.toRadians(lat);
        int y = (int) Math.floor((1.0 - Math.log(Math.tan(lat_rad) + 1.0 / Math.cos(lat_rad)) / Math.PI) / 2.0 * n * 256);
        return new java.awt.Point(x, y);
    }

    @Override
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for distance calculation
        double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @Override
    public int getDefaultTileSize() {
        return 256; // Standard tile size
    }

    @Override
    public int getTileSize() {
        return 256; // Standard tile size
    }

    @Override
    public String getTileId(int zoom, int tilex, int tiley) {
        return zoom + "/" + tilex + "/" + tiley;
    }

    @Override
    public String getId() {
        return "offline-tiles";
    }

    // Helper method for asinh since it's not available in older Java versions
    private static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    /**
     * Check if the tile directory exists and contains tiles
     */
    public boolean isAvailable() {
        File dir = new File(tileDir);
        return dir.exists() && dir.isDirectory();
    }
}