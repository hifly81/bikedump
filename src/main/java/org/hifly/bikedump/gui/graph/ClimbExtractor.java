package org.hifly.bikedump.gui.graph;

import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.gps.WaypointSegment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClimbExtractor {

    private ClimbExtractor() {}

    public static List<WaypointSegment> extractBestClimb(List<WaypointSegment> in) {
        if (in == null || in.size() < 2) return in;

        List<WaypointSegment> segs = new ArrayList<>(in);
        segs.sort(Comparator.comparingDouble(WaypointSegment::getUnit));

        final double MAX_CONSEC_DOWN_METERS = 300.0;
        final double MAX_CONSEC_DOWN_DROP_M = 25.0;

        List<List<WaypointSegment>> candidates = new ArrayList<>();
        int start = 0;
        double consecDownMeters = 0.0;
        double consecDownDrop = 0.0;

        for (int i = 1; i < segs.size(); i++) {
            WaypointSegment prev = segs.get(i - 1);
            WaypointSegment cur = segs.get(i);

            double dxMeters = (cur.getUnit() - prev.getUnit()) * 1000.0;
            double dh = cur.getEle() - prev.getEle();

            if (dh < 0) {
                consecDownMeters += Math.max(0.0, dxMeters);
                consecDownDrop += -dh;
            } else {
                consecDownMeters = 0.0;
                consecDownDrop = 0.0;
            }

            boolean split = consecDownMeters >= MAX_CONSEC_DOWN_METERS || consecDownDrop >= MAX_CONSEC_DOWN_DROP_M;
            if (split) {
                if (i - start >= 2) candidates.add(segs.subList(start, i));
                start = i;
                consecDownMeters = 0.0;
                consecDownDrop = 0.0;
            }
        }

        if (segs.size() - start >= 2) candidates.add(segs.subList(start, segs.size()));
        if (candidates.isEmpty()) return segs;

        List<WaypointSegment> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (List<WaypointSegment> c : candidates) {
            Score s = scoreClimbSegments(c);
            if (s.score > bestScore) {
                bestScore = s.score;
                best = c;
            }
        }
        return best != null ? new ArrayList<>(best) : segs;
    }

    public static List<Waypoint> extractBestClimbWaypoints(List<Waypoint> in) {
        if (in == null || in.size() < 2) return in;

        List<Waypoint> pts = new ArrayList<>(in);
        pts.sort(Comparator.comparingDouble(Waypoint::getDistanceFromStartingPoint));

        final double MAX_CONSEC_DOWN_METERS = 300.0;
        final double MAX_CONSEC_DOWN_DROP_M = 25.0;

        List<List<Waypoint>> candidates = new ArrayList<>();
        int start = 0;
        double consecDownMeters = 0.0;
        double consecDownDrop = 0.0;

        for (int i = 1; i < pts.size(); i++) {
            Waypoint prev = pts.get(i - 1);
            Waypoint cur = pts.get(i);

            double dxMeters = (cur.getDistanceFromStartingPoint() - prev.getDistanceFromStartingPoint()) * 1000.0;
            double dh = cur.getEle() - prev.getEle();

            if (dh < 0) {
                consecDownMeters += Math.max(0.0, dxMeters);
                consecDownDrop += -dh;
            } else {
                consecDownMeters = 0.0;
                consecDownDrop = 0.0;
            }

            boolean split = consecDownMeters >= MAX_CONSEC_DOWN_METERS || consecDownDrop >= MAX_CONSEC_DOWN_DROP_M;
            if (split) {
                if (i - start >= 2) candidates.add(pts.subList(start, i));
                start = i;
                consecDownMeters = 0.0;
                consecDownDrop = 0.0;
            }
        }

        if (pts.size() - start >= 2) candidates.add(pts.subList(start, pts.size()));
        if (candidates.isEmpty()) return pts;

        List<Waypoint> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (List<Waypoint> c : candidates) {
            Score s = scoreClimbWaypoints(c);
            if (s.score > bestScore) {
                bestScore = s.score;
                best = c;
            }
        }
        List<Waypoint> out = (best != null ? new ArrayList<>(best) : pts);
        return trimNonClimbEdgesWaypoints(out);
    }

    private static Score scoreClimbSegments(List<WaypointSegment> segs) {
        if (segs.size() < 2) return new Score(-1e9);

        double up = 0.0, down = 0.0;
        for (int i = 1; i < segs.size(); i++) {
            double dh = segs.get(i).getEle() - segs.get(i - 1).getEle();
            if (dh > 0) up += dh; else down += -dh;
        }

        double netGain = segs.get(segs.size() - 1).getEle() - segs.get(0).getEle();
        double downRatio = (up + down) > 0 ? down / (up + down) : 1.0;

        boolean isClimb = netGain >= 80.0 && downRatio <= 0.20;
        double score = (isClimb ? 1_000_000.0 : 0.0) + netGain * 1000.0 - downRatio * 10_000.0;

        return new Score(score);
    }

    private static Score scoreClimbWaypoints(List<Waypoint> pts) {
        if (pts.size() < 2) return new Score(-1e9);

        double up = 0.0, down = 0.0;
        for (int i = 1; i < pts.size(); i++) {
            double dh = pts.get(i).getEle() - pts.get(i - 1).getEle();
            if (dh > 0) up += dh; else down += -dh;
        }

        double netGain = pts.get(pts.size() - 1).getEle() - pts.get(0).getEle();
        double downRatio = (up + down) > 0 ? down / (up + down) : 1.0;

        boolean isClimb = netGain >= 80.0 && downRatio <= 0.20;
        double score = (isClimb ? 1_000_000.0 : 0.0) + netGain * 1000.0 - downRatio * 10_000.0;

        return new Score(score);
    }

    public static ClimbRange extractBestClimbRangeFromSegments(List<WaypointSegment> in) {
        List<WaypointSegment> best = extractBestClimb(in);
        if (best == null || best.size() < 2) return null;

        // Raw dataset bounds (full list bounds are better for clamping)
        double dataMinKm = Double.POSITIVE_INFINITY;
        double dataMaxKm = Double.NEGATIVE_INFINITY;
        for (WaypointSegment s : in) {
            dataMinKm = Math.min(dataMinKm, s.getUnit());
            dataMaxKm = Math.max(dataMaxKm, s.getUnit());
        }

        double startKm = trimLeadingNonClimbStartKm(best);
        double endKm = trimTrailingNonClimbEndKm(best);

        // Buffer to avoid outside-range grey tails due to smoothing/noise
        final double BUFFER_KM = 0.05; // 50m
        startKm -= BUFFER_KM;
        endKm += BUFFER_KM;

        // Clamp to dataset bounds
        startKm = Math.max(dataMinKm, startKm);
        endKm = Math.min(dataMaxKm, endKm);

        if (endKm < startKm) return new ClimbRange(dataMinKm, dataMaxKm);
        return new ClimbRange(startKm, endKm);
    }

    private static double trimLeadingNonClimbStartKm(List<WaypointSegment> segs) {
        final double HALF_WINDOW_METERS = 100.0;
        final double EPS_SLOPE_PERCENT = 0.5;

        int n = segs.size();
        if (n < 2) return segs.get(0).getUnit();

        // Find first index k such that local slope is climb-ish (> EPS)
        for (int k = 0; k < n; k++) {
            Double slope = localSlopePercent(segs, k, HALF_WINDOW_METERS);
            if (slope != null && slope > EPS_SLOPE_PERCENT) {
                return segs.get(k).getUnit();
            }
        }

        // Nothing qualifies: not really a climb
        return segs.get(0).getUnit();
    }

    private static double trimTrailingNonClimbEndKm(List<WaypointSegment> segs) {
        final double HALF_WINDOW_METERS = 100.0;
        final double EPS_SLOPE_PERCENT = 0.5;

        int n = segs.size();
        if (n < 2) return segs.get(n - 1).getUnit();

        // Find last index k such that local slope is climb-ish (> EPS)
        for (int k = n - 1; k >= 0; k--) {
            Double slope = localSlopePercent(segs, k, HALF_WINDOW_METERS);
            if (slope != null && slope > EPS_SLOPE_PERCENT) {
                return segs.get(k).getUnit();
            }
        }

        return segs.get(n - 1).getUnit();
    }

    private static Double localSlopePercent(List<WaypointSegment> segs, int idx, double halfWindowMeters) {
        int n = segs.size();
        if (n < 2) return null;

        double x0 = segs.get(idx).getUnit(); // km

        // left index
        int iL = idx;
        while (iL > 0) {
            double xPrev = segs.get(iL - 1).getUnit();
            if ((x0 - xPrev) * 1000.0 >= halfWindowMeters) break;
            iL--;
        }

        // right index
        int iR = idx;
        while (iR < n - 1) {
            double xNext = segs.get(iR + 1).getUnit();
            if ((xNext - x0) * 1000.0 >= halfWindowMeters) break;
            iR++;
        }

        if (iL == idx && iR == idx) return null;

        WaypointSegment p1 = segs.get(iL);
        WaypointSegment p2 = segs.get(iR);

        double dxMeters = (p2.getUnit() - p1.getUnit()) * 1000.0;
        if (dxMeters <= 0) return null;

        double dh = p2.getEle() - p1.getEle();
        return (dh / dxMeters) * 100.0;
    }

    private static final class Score {
        final double score;
        Score(double score) { this.score = score; }
    }

    private static List<Waypoint> trimNonClimbEdgesWaypoints(List<Waypoint> pts) {
        if (pts == null || pts.size() < 2) return pts;

        // Keep consistent with SlopeRenderer smoothing
        final double HALF_WINDOW_METERS = 100.0;
        final double EPS_SLOPE_PERCENT = 0.5;

        // Find first index that is "climb-ish"
        int first = 0;
        while (first < pts.size() - 1) {
            Double slope = localSlopePercentWaypoints(pts, first, HALF_WINDOW_METERS);
            if (slope != null && slope > EPS_SLOPE_PERCENT) break;
            first++;
        }

        // Find last index that is "climb-ish"
        int last = pts.size() - 1;
        while (last > 0) {
            Double slope = localSlopePercentWaypoints(pts, last, HALF_WINDOW_METERS);
            if (slope != null && slope > EPS_SLOPE_PERCENT) break;
            last--;
        }

        if (last <= first) {
            // Nothing solid found; return original (or return minimal slice).
            return pts;
        }

        return new ArrayList<>(pts.subList(first, last + 1));
    }

    private static Double localSlopePercentWaypoints(List<Waypoint> pts, int idx, double halfWindowMeters) {
        int n = pts.size();
        if (n < 2) return null;

        double x0 = pts.get(idx).getDistanceFromStartingPoint(); // km

        // left index
        int iL = idx;
        while (iL > 0) {
            double xPrev = pts.get(iL - 1).getDistanceFromStartingPoint();
            if ((x0 - xPrev) * 1000.0 >= halfWindowMeters) break;
            iL--;
        }

        // right index
        int iR = idx;
        while (iR < n - 1) {
            double xNext = pts.get(iR + 1).getDistanceFromStartingPoint();
            if ((xNext - x0) * 1000.0 >= halfWindowMeters) break;
            iR++;
        }

        if (iL == idx && iR == idx) return null;

        Waypoint p1 = pts.get(iL);
        Waypoint p2 = pts.get(iR);

        double dxMeters = (p2.getDistanceFromStartingPoint() - p1.getDistanceFromStartingPoint()) * 1000.0;
        if (dxMeters <= 0) return null;

        double dh = p2.getEle() - p1.getEle();
        return (dh / dxMeters) * 100.0;
    }
}