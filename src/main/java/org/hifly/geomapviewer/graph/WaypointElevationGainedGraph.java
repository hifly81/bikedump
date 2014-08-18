package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointElevationGainedGraph extends WaypointGraph {

    public WaypointElevationGainedGraph(List<List<WaypointSegment>> waypoints) {
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

        TextTitle subtitle1 = new TextTitle("This plot shows the elevation gained (+ or -) for every lap");
        chart.addSubtitle(subtitle1);

        //remove old legends
        chart.removeLegend();


        return chart;
    }

    @Override
    public IntervalXYDataset createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointSegment> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointSegment km:waypoint) {
                series1.add(km.getUnit(), GpsUtility.roundDoubleStat(km.getEleGained()));
            }
            series.add(series1);
            index++;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for(XYSeries serie:series) {
            dataset.addSeries(serie);
        }
        return dataset;
    }

}
