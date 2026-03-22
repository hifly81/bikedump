package org.hifly.bikedump.gui.graph;

import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;

public class SlopeRenderer extends XYAreaRenderer2 {

    private static final long serialVersionUID = 9L;

    // GPS-derived elevation is noisy -> use a larger window (meters) for a more realistic slope.
    // Total window = 2 * HALF_WINDOW_METERS. Suggested: 200m total.
    private static final double HALF_WINDOW_METERS = 100.0;

    // Avoid flickering near flat due to GPS noise
    private static final double FLAT_EPS_PERCENT = 0.5;

    private final boolean singleColor;
    private final XYSeriesCollection dataset;

    // Uphill palette (5 bands)
    private final ArrayList<Color> mapSlopeUpColor = new ArrayList<Color>();
    {
        mapSlopeUpColor.add(new Color(152, 230, 0)); // 0..4%
        mapSlopeUpColor.add(new Color(0, 77, 233));  // 4..8%
        mapSlopeUpColor.add(new Color(254, 252, 0)); // 8..10%
        mapSlopeUpColor.add(new Color(254, 0, 0));   // 10..15%
        mapSlopeUpColor.add(new Color(77, 0, 0));    // >15%
    }

    // Downhill/slope<=0 single color (choose what you prefer)
    private static final Color DOWNHILL_COLOR = new Color(160, 160, 160);

    private final ArrayList<Color> mapSlopeSingleColor = new ArrayList<Color>();
    {
        mapSlopeSingleColor.add(new Color(254, 0, 0));
    }

    private final Double climbStartKm;
    private final Double climbEndKm;
    private static final Color OUTSIDE_CLIMB_COLOR = new Color(210, 210, 210);

    public SlopeRenderer(XYSeriesCollection dataset, boolean singleColor) {
        this(dataset, singleColor, null, null);
    }

    public SlopeRenderer(XYSeriesCollection dataset, boolean singleColor, Double climbStartKm, Double climbEndKm) {
        this.dataset = dataset;
        this.singleColor = singleColor;
        this.climbStartKm = climbStartKm;
        this.climbEndKm = climbEndKm;
    }



    @Override
    public Paint getItemPaint(int series, int item) {
        if (singleColor) return mapSlopeSingleColor.get(0);

        double x0km = dataset.getXValue(series, item);
        if (climbStartKm != null && climbEndKm != null) {
            if (x0km < climbStartKm || x0km > climbEndKm) {
                return OUTSIDE_CLIMB_COLOR;
            }
        }

        int itemCount = dataset.getItemCount(series);
        if (itemCount <= 1) return mapSlopeUpColor.get(0);

        double y0 = dataset.getYValue(series, item);
        if (Double.isNaN(y0)) y0 = 0.0;

        // Find left index iL: at least HALF_WINDOW_METERS behind x0
        int iL = item;
        while (iL > 0) {
            double xPrevKm = dataset.getXValue(series, iL - 1);
            double distMeters = (x0km - xPrevKm) * 1000.0;
            if (distMeters >= HALF_WINDOW_METERS) break;
            iL--;
        }

        // Find right index iR: at least HALF_WINDOW_METERS ahead of x0
        int iR = item;
        while (iR < itemCount - 1) {
            double xNextKm = dataset.getXValue(series, iR + 1);
            double distMeters = (xNextKm - x0km) * 1000.0;
            if (distMeters >= HALF_WINDOW_METERS) break;
            iR++;
        }

        if (iL == item && iR == item) return mapSlopeUpColor.get(0);

        double x1km = dataset.getXValue(series, iL);
        double y1 = dataset.getYValue(series, iL);
        if (Double.isNaN(y1)) y1 = 0.0;

        double x2km = dataset.getXValue(series, iR);
        double y2 = dataset.getYValue(series, iR);
        if (Double.isNaN(y2)) y2 = 0.0;

        double deltaHeight = y2 - y1;
        double deltaDistanceMeters = (x2km - x1km) * 1000.0;
        if (deltaDistanceMeters <= 0) return mapSlopeUpColor.get(0);

        double slopePercent = (deltaHeight / deltaDistanceMeters) * 100.0;

        // treat near-flat as "easy" uphill color (or downhill color if you prefer)
        if (Math.abs(slopePercent) < FLAT_EPS_PERCENT) {
            return mapSlopeUpColor.get(0);
        }

        // Downhill or flat => single color
        if (slopePercent <= 0) {
            return DOWNHILL_COLOR;
        }

        // Uphill => 5 colored bands
        return pickUphillBandColor(slopePercent);
    }

    private Color pickUphillBandColor(double slopePercent) {
        if (slopePercent <= 4.0) return mapSlopeUpColor.get(0);
        if (slopePercent <= 8.0) return mapSlopeUpColor.get(1);
        if (slopePercent <= 10.0) return mapSlopeUpColor.get(2);
        if (slopePercent <= 15.0) return mapSlopeUpColor.get(3);
        return mapSlopeUpColor.get(4);
    }
}