package org.hifly.bikedump.graph;

import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;

import java.util.List;

/**
 * @author
 * @date 18/02/14
 */
public abstract class WaypointGraph {

    protected List<List<WaypointSegment>> waypoints;
    protected List<Waypoint> waypointDetails;
    protected boolean detailGraph;
    protected JFreeChart chart;

    public WaypointGraph(List<List<WaypointSegment>> waypoints) {
        this.waypoints = waypoints;
    }

    public WaypointGraph(List<Waypoint> waypointDetails, boolean detailGraph) {
        this.waypointDetails = waypointDetails;
        this.detailGraph = true;
    }

    public abstract JFreeChart createGraph();
    public abstract XYDataset createDataset();


}
