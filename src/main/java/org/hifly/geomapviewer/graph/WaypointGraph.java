package org.hifly.geomapviewer.graph;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public abstract class WaypointGraph {

    protected List<List<WaypointKm>> waypoints;

    public WaypointGraph(List<List<WaypointKm>> waypoints) {
        this.waypoints = waypoints;
    }

    public abstract JFreeChart createGraph();

    public abstract XYDataset createDataset();
}
