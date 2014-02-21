package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointAvgSpeedGraph extends WaypointGraph {

    public WaypointAvgSpeedGraph(List<List<WaypointKm>> waypoints) {
        super(waypoints);
    }

    @Override
    public JFreeChart createGraph() {

        XYSeriesCollection dataset = createDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "speed/distance",
                "distance (Km)",
                "speed (Km/h)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return chart;
    }

    @Override
    public XYSeriesCollection createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointKm> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointKm km:waypoint) {
                series1.add(km.getKm(),km.getAvgSpeed());
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
