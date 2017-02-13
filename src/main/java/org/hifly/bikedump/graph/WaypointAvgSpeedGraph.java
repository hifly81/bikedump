package org.hifly.bikedump.graph;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.utility.GPSUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

public class WaypointAvgSpeedGraph extends WaypointGraph {

    public WaypointAvgSpeedGraph(List<List<WaypointSegment>> waypoints) {
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

        TextTitle subtitle1 = new TextTitle("This plot shows the average speed in relation with the distance");
        chart.addSubtitle(subtitle1);

        //remove old legends
        chart.removeLegend();

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer
                = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true);

        return chart;
    }

    @Override
    public XYSeriesCollection createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointSegment> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointSegment km:waypoint)
                series1.add(km.getUnit(), GPSUtility.roundDoubleStat(km.getAvgSpeed()));
            series.add(series1);
            index++;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries serie:series)
            dataset.addSeries(serie);
        return dataset;
    }
}
