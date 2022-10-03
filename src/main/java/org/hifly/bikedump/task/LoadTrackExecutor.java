package org.hifly.bikedump.task;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackPref;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.storage.GeoMapStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadTrackExecutor {

    private ExecutorService es;
    private List<Callable<Object>> todo;

    public LoadTrackExecutor(
            boolean isNewTrackToLoad,
            Iterator iterator,
            StringBuffer sb, List<List<Coordinate>> coordinates,
            List<Map<String, WaypointSegment>> waypoint,
            List<Track> tracks) {
        es = Executors.newFixedThreadPool(5);
        todo = new ArrayList<>();

        for (; iterator.hasNext(); ) {
            Object obj = iterator.next();
            File file = null;
            if (obj instanceof File) {
                file = (File) obj;
            } else if (obj instanceof Map.Entry) {
                Map.Entry<String, TrackPref> entry = (Map.Entry<String, TrackPref>) obj;
                file = new File(entry.getKey());
            }

            if(isNewTrackToLoad && (GeoMapStorage.tracksLibrary.get(file.getAbsolutePath()) == null))
                todo.add(Executors.callable(new LoadTrack(file, sb, coordinates,waypoint, tracks)));
            else
                todo.add(Executors.callable(new LoadTrack(file, sb, coordinates,waypoint, tracks)));
        }

    }

    public List<Future<Object>> execute() throws Exception {
        return es.invokeAll(todo);
    }
}
