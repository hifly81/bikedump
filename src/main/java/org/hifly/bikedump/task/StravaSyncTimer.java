package org.hifly.bikedump.task;

import org.hifly.bikedump.domain.StravaPref;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.integration.strava.StravaImporter;
import org.hifly.bikedump.storage.GeoMapStorage;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StravaSyncTimer {

    private final StravaImporter importer;
    private Timer timer;

    public StravaSyncTimer(StravaImporter importer) {
        this.importer = importer;
    }

    public void startIfEnabled() {
        StravaPref pref = GeoMapStorage.stravaPref;
        if (pref == null || !pref.isAutoSyncEnabled() || !pref.isConnected()) return;

        stop();

        timer = new Timer(true);
        long interval = Math.max(5 * 60 * 1000L, pref.getAutoSyncIntervalMillis()); // min 5 min
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<Track> imported = importer.importNewRides(pref, 10);
                    if (!imported.isEmpty()) {
                        pref.setLastSuccessfulSyncAt(new Date());
                        GeoMapStorage.save();
                        // TODO: hook into UI to show "imported N rides"
                    }
                } catch (Exception e) {
                    // TODO: log
                    e.printStackTrace();
                }
            }
        }, 0, interval);
    }

    public void stop() {
        if (timer != null) {
            try { timer.cancel(); } catch (Exception ignored) {}
            timer = null;
        }
    }
}