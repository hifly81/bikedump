package org.hifly.geomapviewer.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointAggregateGraph {

    private List<WaypointGraph> graphs;

    public WaypointAggregateGraph(List<WaypointGraph> graphs) {
        this.graphs = graphs;
    }

    public  JFreeChart createGraph() {
        XYSeriesCollection dataset = (XYSeriesCollection)graphs.get(0).createDataset();


        for(int i=1; i<graphs.size();i++) {
            WaypointGraph graph = graphs.get(i);
            XYSeriesCollection temp = (XYSeriesCollection)graph.createDataset();
            List<XYSeries> series = temp.getSeries();
            for(XYSeries serie:series) {
                dataset.addSeries(serie);
            }
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "tt",
                "distance",
                "tt",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return chart;

    }
}
