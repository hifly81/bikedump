package org.hifly.bikedump.gui.graph;

import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.utility.GPSUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//TODO it only highlights the last climb
public class WaypointElevationGraph extends WaypointGraph {

    private boolean singleColor = true;
    private boolean bounds;

    private Double climbStartKm = null;
    private Double climbEndKm = null;

    public WaypointElevationGraph(List<List<WaypointSegment>> waypoints, boolean singleColor, Double climbStartKm, Double climbEndKm) {
        super(waypoints);
        this.singleColor = singleColor;
        this.climbStartKm = climbStartKm;
        this.climbEndKm = climbEndKm;
    }

    public WaypointElevationGraph(List<Waypoint> waypointDetails, boolean detailGraph, boolean singleColor, boolean bounds) {
        super(waypointDetails, detailGraph);
        this.singleColor = singleColor;
        this.bounds = bounds;
    }

    @Override
    public JFreeChart createGraph() {

        XYSeriesCollection dataset;
        if (detailGraph) {
            dataset = createSingleDataset();
        } else {
            dataset = createDataset();
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "elevation/distance",
                "distance (Km.)",
                "elevation (m.)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        TextTitle subtitle1 = new TextTitle("This plot shows the elevation in relation with the distance");
        chart.addSubtitle(subtitle1);

        XYPlot plot = (XYPlot) chart.getPlot();

        // Default: remove unused empty space
        plot.getRangeAxis().setLowerMargin(0.0);
        plot.getRangeAxis().setUpperMargin(0.0);

        if (detailGraph) {
            plot.getDomainAxis().setLowerMargin(0.0);
            plot.getDomainAxis().setUpperMargin(0.0);
        } else {
            plot.getDomainAxis().setLowerMargin(0.02);
            plot.getDomainAxis().setUpperMargin(0.02);
        }

        if (!detailGraph && climbStartKm != null && climbEndKm != null) {
            double a = Math.min(climbStartKm, climbEndKm);
            double b = Math.max(climbStartKm, climbEndKm);

            IntervalMarker m = new IntervalMarker(a, b);
            m.setPaint(new Color(255, 252, 0, 120));
            m.setOutlinePaint(Color.YELLOW);
            m.setOutlineStroke(new BasicStroke(2.0f));
            plot.addDomainMarker(m, Layer.FOREGROUND);
        }

        if (detailGraph) {
            LegendItemSource lis = getLegendSource();
            LegendTitle legendTitle = new LegendTitle(lis);
            legendTitle.setPosition(RectangleEdge.BOTTOM);
            legendTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            legendTitle.setBorder(new BlockBorder());
            chart.addLegend(legendTitle);

            if (bounds) {
                double xMin = waypointDetails.get(0).getDistanceFromStartingPoint();
                double xMax = waypointDetails.get(waypointDetails.size() - 1).getDistanceFromStartingPoint();

                xMin = Math.max(0.0, xMin - 0.5);
                xMax = xMax + 0.5;

                double yMin = Double.POSITIVE_INFINITY;
                double yMax = Double.NEGATIVE_INFINITY;
                for (Waypoint w : waypointDetails) {
                    double ele = GPSUtility.roundDoubleStat(w.getEle());
                    yMin = Math.min(yMin, ele);
                    yMax = Math.max(yMax, ele);
                }

                yMax = yMax + 70.0;

                plot.getDomainAxis().setRange(xMin, xMax);
                plot.getRangeAxis().setRange(yMin, yMax);
            }
        }

        plot.setRenderer(new SlopeRenderer(dataset, singleColor, climbStartKm, climbEndKm));

        return chart;
    }

    private LegendItemSource getLegendSource() {
        return () -> {
            LegendItemCollection lic = new LegendItemCollection();

            LegendItem item1 = new LegendItem("0% - 4%", null, null, null, new Rectangle(25, 25), new Color(152, 230, 0));
            LegendItem item2 = new LegendItem("4% - 8%", null, null, null, new Rectangle(25, 25), new Color(0, 77, 233));
            LegendItem item3 = new LegendItem("8% - 10%", null, null, null, new Rectangle(25, 25), new Color(254, 252, 0));
            LegendItem item4 = new LegendItem("10% - 15%", null, null, null, new Rectangle(25, 25), new Color(254, 0, 0));
            LegendItem item5 = new LegendItem(">15%", null, null, null, new Rectangle(25, 25), new Color(77, 0, 0));

            lic.add(item1);
            lic.add(item2);
            lic.add(item3);
            lic.add(item4);
            lic.add(item5);

            return lic;
        };
    }

    @Override
    public XYSeriesCollection createDataset() {
        List<XYSeries> series = new ArrayList<>(waypoints.size());
        int index = 0;

        for (List<WaypointSegment> waypoint : waypoints) {
            XYSeries series1 = new XYSeries(index);
            for (WaypointSegment km : waypoint) {
                series1.add(km.getUnit(), GPSUtility.roundDoubleStat(km.getEle()));
            }
            series.add(series1);
            index++;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries serie : series) dataset.addSeries(serie);
        return dataset;
    }

    public XYSeriesCollection createSingleDataset() {
        int index = 0;
        XYSeries series1 = new XYSeries(index);

        for (Waypoint waypoint : waypointDetails) {
            series1.add(waypoint.getDistanceFromStartingPoint(), GPSUtility.roundDoubleStat(waypoint.getEle()));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        return dataset;
    }
}