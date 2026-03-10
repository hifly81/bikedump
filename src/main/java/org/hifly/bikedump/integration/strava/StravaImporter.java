package org.hifly.bikedump.integration.strava;

import org.hifly.bikedump.controller.GPSController;
import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.storage.GeoMapStorage;

import java.io.File;
import java.util.*;

public class StravaImporter {

    private final StravaClient client;

    public StravaImporter(StravaClient client) {
        this.client = client;
    }

    public List<Track> importNewRides(StravaPref pref, int maxToImport) throws Exception {
        client.refreshIfNeeded(pref);

        long after = pref.getLastSyncAfterEpochSeconds(); // marker time
        List<Track> imported = new ArrayList<>();

        int page = 1;
        int perPage = 50; // Strava max is typically 200, but 50 is safe

        while (imported.size() < maxToImport) {
            List<StravaClient.StravaActivity> acts = client.listActivities(pref, perPage, page, after);
            if (acts.isEmpty()) break;

            for (StravaClient.StravaActivity a : acts) {
                if (!"Ride".equalsIgnoreCase(a.type)) continue;

                File tcx = new File(GeoMapStorage.getStravaExportsDir() + "strava_" + a.id + ".tcx");
                File gpx = new File(GeoMapStorage.getStravaExportsDir() + "strava_" + a.id + ".gpx");

                Track t = null;
                try {
                    client.downloadActivityExport(pref, a.id, "tcx", tcx);
                    t = GPSController.extractTrackFromTcx(tcx.getAbsolutePath()).getKey();
                } catch (Exception ex) {
                    try {
                        client.downloadActivityExport(pref, a.id, "gpx", gpx);
                        t = GPSController.extractTrackFromGpx(gpx.getAbsolutePath()).getKey();
                    } catch (Exception ex2) {
                        continue;
                    }
                }

                if (t != null) imported.add(t);
                if (imported.size() >= maxToImport) break;
            }

            page++;
        }

        // Update marker: set "after" to now (seconds)
        // This means next sync only looks after this moment.
        if (!imported.isEmpty()) {
            pref.setLastSyncAfterEpochSeconds(System.currentTimeMillis() / 1000L);
            pref.setLastSuccessfulSyncAt(new Date());
        }

        return imported;
    }
}