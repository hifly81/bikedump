package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointElevationGraph extends WaypointGraph {

    public WaypointElevationGraph(List<List<WaypointSegment>> waypoints) {
        super(waypoints);
    }

    public WaypointElevationGraph(List<Waypoint> waypointDetails, boolean detailGraph) {
          super(waypointDetails,detailGraph);
    }

    @Override
    public JFreeChart createGraph() {

        XYSeriesCollection dataset = null;
        if(detailGraph) {
            dataset = createSingleDataset();
        }
        else {
            dataset = createDataset();
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "elevation/distance",
                "distance (Km.)",
                "elevation (m.)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        TextTitle subtitle1 = new TextTitle("This plot shows the elevation in relation with the distance");
        chart.addSubtitle(subtitle1);

        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();
        /*XYLineAndShapeRenderer renderer
                = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true)*/;

        plot.setRenderer(new SlopeRenderer(dataset));

        return chart;
    }

    @Override
    public XYSeriesCollection createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointSegment> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointSegment km:waypoint) {
                series1.add(km.getKm(), GpsUtility.roundDoubleStat(km.getEle()));
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

    public XYSeriesCollection createSingleDataset() {
        List<XYSeries> series = new ArrayList(waypointDetails.size());
        //TODO real name
        int index = 0;
        XYSeries series1 = new XYSeries(index);
        for(Waypoint waypoint:waypointDetails) {
            series1.add(waypoint.getDistanceFromStartingPoint(),GpsUtility.roundDoubleStat(waypoint.getEle()));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);

        return dataset;
    }
}
