package org.hifly.bikedump.gui.panel.tiles;

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractOsmTileSource;

public class HumanitarianTileSource extends AbstractOsmTileSource {
    private static final String[] SERVER = new String[]{"a", "b", "c"};
    private int serverNum;

    public HumanitarianTileSource() {
        super("Humanitarian", "https://%s.tile.openstreetmap.fr/hot", "OSM_HOT");
    }

    @Override
    public String getBaseUrl() {
        String url = String.format(this.baseUrl, SERVER[this.serverNum]);
        this.serverNum = (this.serverNum + 1) % SERVER.length;
        return url;
    }

    @Override
    public int getMaxZoom() {
        return 19;
    }
}