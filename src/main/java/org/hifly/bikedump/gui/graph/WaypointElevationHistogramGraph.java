package org.hifly.bikedump.gui.graph;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.utility.GPSUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//TODO it only highlights the last climb
public class WaypointElevationHistogramGraph extends WaypointGraph {

    private final ClimbRange climbRange;

    public WaypointElevationHistogramGraph(List<List<WaypointSegment>> waypoints, ClimbRange climbRange) {
        super(waypoints);
        this.climbRange = climbRange;
    }

    @Override
    public JFreeChart createGraph() {

        IntervalXYDataset dataset = createDataset();

        final JFreeChart chart = ChartFactory.createXYBarChart(
                "elevation gained/distance",
                "distance (Km.)",
                false,
                "elevation gained (m.)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        TextTitle subtitle1 = new TextTitle("This plot shows the elevation gained (+ or -) for every lap");
        chart.addSubtitle(subtitle1);

        chart.removeLegend();

        XYPlot plot = (XYPlot) chart.getPlot();

        ValueAxis xAxis = plot.getDomainAxis();
        XYDataset xData = dataset instanceof XYBarDataset
                ? ((XYBarDataset) dataset).getUnderlyingDataset()
                : dataset;

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;

        for (int s = 0; s < xData.getSeriesCount(); s++) {
            int items = xData.getItemCount(s);
            for (int i = 0; i < items; i++) {
                double x = xData.getXValue(s, i);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
        }

        double totalKm = maxX - minX;
        if (Double.isFinite(totalKm) && totalKm > 0.0) {
            double padKm = Math.max(0.2, Math.min(1.0, totalKm * 0.02)); // 200m..1km
            double margin = padKm / totalKm;
            xAxis.setLowerMargin(margin);
            xAxis.setUpperMargin(margin);
        }

        plot.getRangeAxis().setUpperMargin(0.10);

        if (climbRange != null) {
            org.jfree.chart.plot.IntervalMarker m = new org.jfree.chart.plot.IntervalMarker(climbRange.startKm, climbRange.endKm);
            m.setPaint(new Color(255, 252, 0, 120));
            m.setOutlinePaint(Color.YELLOW);
            m.setOutlineStroke(new java.awt.BasicStroke(2.0f));
            plot.addDomainMarker(m, org.jfree.ui.Layer.FOREGROUND);
        }

        return chart;
    }

    @Override
    public IntervalXYDataset createDataset() {
        List<XYSeries> series = new ArrayList<>(waypoints.size());
        int index = 0;

        for (List<WaypointSegment> waypoint : waypoints) {
            XYSeries s = new XYSeries(index);
            for (WaypointSegment km : waypoint) {
                s.add(km.getUnit(), GPSUtility.roundDoubleStat(km.getEleGained()));
            }
            series.add(s);
            index++;
        }

        XYSeriesCollection base = new XYSeriesCollection();
        series.forEach(base::addSeries);

        // width in km (your laps are 1km)
        return new XYBarDataset(base, 1.0);
    }

}
