package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.utility.GPSUtility;
import org.jfree.chart.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public class WaypointElevationGraph extends WaypointGraph {

    private boolean singleColor = true;

    public WaypointElevationGraph(List<List<WaypointSegment>> waypoints) {
        super(waypoints);
    }

    public WaypointElevationGraph(List<Waypoint> waypointDetails, boolean detailGraph) {
          super(waypointDetails,detailGraph);
    }

    public WaypointElevationGraph(List<Waypoint> waypointDetails, boolean detailGraph, boolean singleColor) {
        super(waypointDetails,detailGraph);
        this.singleColor = singleColor;
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

        XYPlot plot = (XYPlot) chart.getPlot();

        //remove old legends
        chart.removeLegend();

        if(detailGraph) {
            //add ew legends
            LegendItemSource lis = getLegendSource();
            LegendTitle legendTitle = new LegendTitle(lis);
            legendTitle.setPosition(RectangleEdge.BOTTOM);
            legendTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            legendTitle.setBorder(new BlockBorder());

            chart.addLegend(legendTitle);

        }


        plot.setRenderer(new SlopeRenderer(dataset,singleColor));

        return chart;
    }

    private LegendItemSource getLegendSource() {
        return new LegendItemSource() {
            public LegendItemCollection getLegendItems() {
                //customize label for elevation
                LegendItemCollection lic = new LegendItemCollection();
                LegendItem item1 = new LegendItem(
                        "0% - 4%", null, null, null,
                        new Rectangle(25, 25),
                        new Color(152, 230, 0));
                LegendItem item2 = new LegendItem(
                        "4% - 8%", null, null, null,
                        new Rectangle(25, 25),
                        new Color(0, 77, 233));
                LegendItem item3 = new LegendItem(
                        "8% - 10%", null, null, null,
                        new Rectangle(25, 25),
                        new Color(254, 252, 0));
                LegendItem item4 = new LegendItem(
                        "10% - 15%", null, null, null,
                        new Rectangle(25, 25),
                        new Color(254, 0, 0));
                LegendItem item5 = new LegendItem(
                        ">15%", null, null, null,
                        new Rectangle(25, 25),
                        new Color(77, 0, 0));

                lic.add(item1);
                lic.add(item2);
                lic.add(item3);
                lic.add(item4);
                lic.add(item5);
                return lic;
            }
        };
}


    @Override
    public XYSeriesCollection createDataset() {
        List<XYSeries> series = new ArrayList(waypoints.size());
        //TODO real name
        int index = 0;
        for(List<WaypointSegment> waypoint:waypoints) {
            XYSeries series1 = new XYSeries(index);
            for(WaypointSegment km:waypoint) {
                series1.add(km.getUnit(), GPSUtility.roundDoubleStat(km.getEle()));
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
            series1.add(waypoint.getDistanceFromStartingPoint(), GPSUtility.roundDoubleStat(waypoint.getEle()));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);

        return dataset;
    }
}
