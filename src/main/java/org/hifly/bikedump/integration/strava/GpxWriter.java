package org.hifly.bikedump.integration.strava;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Writes a minimal GPX 1.1 track from Strava streams.
 * We support:
 * - lat/lng (required)
 * - ele (optional)
 * - time (optional, based on startTime + secondsFromStart)
 */
public final class GpxWriter {

    private GpxWriter() {}

    public static void writeGpxFromStreams(
            File out,
            List<double[]> latlng,
            List<Double> altitude,
            List<Integer> secondsFromStart,
            Date startTimeUtc
    ) throws Exception {

        if (latlng == null || latlng.isEmpty()) {
            throw new IllegalArgumentException("No latlng stream data");
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"bikedump\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        sb.append("  <trk>\n");
        sb.append("    <name>Strava import</name>\n");
        sb.append("    <trkseg>\n");

        for (int i = 0; i < latlng.size(); i++) {
            double[] p = latlng.get(i);

            sb.append("      <trkpt lat=\"").append(p[0]).append("\" lon=\"").append(p[1]).append("\">\n");

            if (altitude != null && i < altitude.size() && altitude.get(i) != null) {
                sb.append("        <ele>").append(altitude.get(i)).append("</ele>\n");
            }

            if (startTimeUtc != null && secondsFromStart != null && i < secondsFromStart.size()) {
                long t = startTimeUtc.getTime() + (long) secondsFromStart.get(i) * 1000L;
                sb.append("        <time>").append(df.format(new Date(t))).append("</time>\n");
            }

            sb.append("      </trkpt>\n");
        }

        sb.append("    </trkseg>\n");
        sb.append("  </trk>\n");
        sb.append("</gpx>\n");

        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}