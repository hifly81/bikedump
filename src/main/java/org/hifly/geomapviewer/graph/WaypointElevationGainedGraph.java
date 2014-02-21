package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointElevationGainedGraph extends WaypointGraph {

    public WaypointElevationGainedGraph(List<List<WaypointKm>> waypoints) {
        super(waypoints);
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


        return chart;
    }

    @Override
    public IntervalXYDataset createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointKm> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointKm km:waypoint) {
                series1.add(km.getKm(),km.getEleGained());
                series.add(series1);
            }
            index++;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries serie:series) {
            dataset.addSeries(serie);
        }
        return dataset;
    }
}
