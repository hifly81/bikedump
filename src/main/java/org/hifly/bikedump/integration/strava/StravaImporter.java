package org.hifly.bikedump.integration.strava;

import org.hifly.bikedump.controller.GPSController;
import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.storage.GeoMapStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Strava importer that generates GPX files from Streams (latlng/altitude/time).
 */
public class StravaImporter {

    private final StravaClient client;

    public StravaImporter(StravaClient client) {
        this.client = client;
    }

    public static class ImportResult {
        public final List<Track> tracks = new ArrayList<>();
        public final StringBuffer log = new StringBuffer();
    }

    /**
     * Compatibility wrapper used by timers/old code.
     * Imports "latest" rides (no date bounds) and returns only the tracks list.
     *
     * NOTE: This method does NOT apply the "after" marker logic anymore by itself.
     * If you want incremental sync, pass an "after" filter using importRidesBetweenWithLog(...)
     * or implement marker logic in the caller.
     */
    public List<Track> importNewRides(StravaPref pref, int maxToImport) throws Exception {
        ImportResult r = importRidesBetweenWithLog(pref, maxToImport, null, null);
        return r.tracks;
    }

    /**
     * Import rides in a date range [after,before] (inclusive), limited to maxToImport.
     * after/before are epoch seconds (UTC).
     * Pass nulls to mean "no bound".
     */
    public ImportResult importRidesBetweenWithLog(StravaPref pref, int maxToImport, Long afterEpochSeconds, Long beforeEpochSeconds) throws Exception {
        ImportResult result = new ImportResult();
        result.log.append("Strava import started (GPX via Streams)\n");
        result.log.append("maxToImport=").append(maxToImport)
                .append(" after=").append(afterEpochSeconds)
                .append(" before=").append(beforeEpochSeconds).append("\n");

        if (pref == null) {
            result.log.append("ERROR: StravaPref is null\n");
            return result;
        }
        if (!pref.isConnected()) {
            result.log.append("Not connected (missing access token)\n");
            return result;
        }

        File exportDir = new File(GeoMapStorage.getStravaExportsDir());
        if (!exportDir.exists()) exportDir.mkdirs();
        result.log.append("Export dir: ").append(exportDir.getAbsolutePath()).append("\n");

        client.refreshIfNeeded(pref);

        int perPage = 50;
        int page = 1;

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));

        while (result.tracks.size() < maxToImport) {
            result.log.append("Listing activities page=").append(page).append("\n");
            List<StravaClient.StravaActivity> acts = client.listActivities(pref, perPage, page, afterEpochSeconds, beforeEpochSeconds);
            result.log.append("Fetched activities count=").append(acts.size()).append("\n");
            if (acts.isEmpty()) break;

            for (StravaClient.StravaActivity a : acts) {
                if (a == null) continue;
                if (a.type == null || !"Ride".equalsIgnoreCase(a.type)) continue;

                result.log.append("Ride activityId=").append(a.id)
                        .append(" name=").append(a.name == null ? "" : a.name)
                        .append(" start_date=").append(a.startDate == null ? "" : a.startDate)
                        .append("\n");

                // If already exported, skip (prevents duplicates + speeds up)
                File gpx = new File(exportDir, "strava_" + a.id + ".gpx");
                if (gpx.exists() && gpx.length() > 0) {
                    result.log.append("GPX already exists, skipping download: ").append(gpx.getName()).append("\n");
                } else {
                    StravaClient.Streams streams;
                    try {
                        streams = client.getActivityStreams(pref, a.id);
                    } catch (Exception ex) {
                        result.log.append("Streams failed activityId=").append(a.id)
                                .append(" err=").append(ex.getMessage()).append("\n");
                        continue;
                    }

                    if (streams.latlng == null || streams.latlng.isEmpty()) {
                        result.log.append("No latlng stream for activityId=").append(a.id).append(" (skipped)\n");
                        continue;
                    }

                    Date startTimeUtc = null;
                    try {
                        if (a.startDate != null && !a.startDate.isBlank()) {
                            startTimeUtc = iso.parse(a.startDate.trim());
                        }
                    } catch (Exception ignored) { }

                    try {
                        GpxWriter.writeGpxFromStreams(gpx, streams.latlng, streams.altitude, streams.time, startTimeUtc);
                        result.log.append("Wrote GPX ").append(gpx.getName()).append(" size=").append(gpx.length()).append("\n");
                    } catch (Exception ex) {
                        result.log.append("GPX write failed activityId=").append(a.id)
                                .append(" err=").append(ex.getMessage()).append("\n");
                        continue;
                    }
                }

                // Parse GPX into Track
                try {
                    Track t = GPSController.extractTrackFromGpx(gpx.getAbsolutePath()).getKey();
                    if (t != null) {
                        result.tracks.add(t);
                        result.log.append("Imported OK (total=").append(result.tracks.size()).append(")\n");
                    } else {
                        result.log.append("Parsed Track is null (skipped)\n");
                    }
                } catch (Exception ex) {
                    result.log.append("GPX parse failed activityId=").append(a.id)
                            .append(" err=").append(ex.getMessage()).append("\n");
                }

                if (result.tracks.size() >= maxToImport) break;
            }

            page++;
        }

        result.log.append("Strava import finished\n");
        return result;
    }
}