# Offline Map Tiles Support

Bike Dump now supports offline map tiles to avoid rate limiting and 403 errors from online OpenStreetMap servers.

## How to Use Offline Tiles

1. **Get Map Tiles**: Download map tiles in TMS format from sources like:
   - [OpenMapTiles](https://openmaptiles.com/downloads/dataset/osm/)
   - Extract tiles from MBTiles format using tools like `mb-util`
   - Generate tiles using tools like TileStache or TileServer

2. **Directory Structure**: Organize tiles in the standard format:
   ```
   tiles/
   ├── 0/
   │   └── 0/
   │       └── 0.png
   ├── 1/
   │   ├── 0/
   │   │   ├── 0.png
   │   │   └── 1.png
   │   └── 1/
   │       ├── 0.png
   │       └── 1.png
   └── 2/
       └── ...
   ```
   Where the format is: `{zoom_level}/{x_tile}/{y_tile}.png`

3. **Configure in Bike Dump**:
   - Launch Bike Dump
   - Go to **Options** menu → **Library** tab
   - Check "Use offline map tiles"
   - Click "Browse..." and select your tiles directory
   - Click OK to save settings

4. **View Maps**: Open or reload track files to see maps using your offline tiles

## Benefits

- **No Rate Limiting**: Avoid 403 errors from overusing online tile servers
- **Faster Loading**: Local tiles load faster than downloading from internet
- **Offline Usage**: View maps without internet connection
- **Custom Maps**: Use specialized map styles or custom tile sets

## Fallback Behavior

- If offline tiles are enabled but directory is not found, falls back to online tiles
- If specific tile files are missing, those areas will show blank (no automatic online fallback for individual tiles)
- Online tiles remain as default when offline tiles are disabled

## Troubleshooting

- **Maps not showing**: Verify tile directory path and structure
- **Partial coverage**: Ensure tiles exist for the geographic area and zoom levels you need
- **Performance**: Very large tile directories may slow down startup

## Technical Details

- Supports zoom levels 0-18
- Standard 256x256 pixel PNG tiles
- Web Mercator projection (EPSG:3857)
- Compatible with jmapviewer TileSource interface