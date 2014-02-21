package org.hifly.geomapviewer.gui.frame;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.hifly.geomapviewer.graph.*;
import org.hifly.geomapviewer.utility.GUIUtility;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 20/02/14
 */
public class GraphViewer extends JFrame {

    private Map.Entry<Integer, Integer> dimension;

    public GraphViewer(final JFrame rootFrame, List<List<WaypointKm>> waypoints) {
        super();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                rootFrame.setEnabled(true);
            }
        });

        rootFrame.setEnabled(false);

        //frame dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey()-100, dimension.getValue()-100);
        setAlwaysOnTop(true);
        setResizable(false);
        setLocationRelativeTo(getRootPane());
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JSplitPane panel = createGraphViewer(waypoints);

        add(panel);

    }

    private JSplitPane createGraphViewer(List<List<WaypointKm>> waypoints) {
        JScrollPane scrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel = new JPanel();
        WaypointElevationGraph waypointElevationGraph = new WaypointElevationGraph(waypoints);
        JFreeChart wpChart = waypointElevationGraph.createGraph();
        ChartPanel wpGraph = new ChartPanel(wpChart);
        wpGraph.setMouseWheelEnabled(true);
        panel.add(wpGraph);
        panel.validate();
        scrollPanel.getViewport().add(panel);

        JScrollPane scrollPanel2 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel2 = new JPanel();
        WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(waypoints);
        JFreeChart wp2Chart = waypointAvgSpeedGraph.createGraph();
        ChartPanel wp2Graph = new ChartPanel(wp2Chart);
        wp2Graph.setMouseWheelEnabled(true);
        panel2.add(wp2Graph);
        panel2.validate();
        scrollPanel2.getViewport().add(panel2);

        JScrollPane scrollPanel3 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel3 = new JPanel();
        WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(waypoints);
        JFreeChart wp3Chart = waypointTimeGraph.createGraph();
        ChartPanel wp3Graph = new ChartPanel(wp3Chart);
        wp3Graph.setMouseWheelEnabled(true);
        panel3.add(wp3Graph);
        panel3.validate();
        scrollPanel3.getViewport().add(panel3);

        JScrollPane scrollPanel4 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel panel4 = new JPanel();
        WaypointElevationGainedGraph waypointElevationGainedGraph = new WaypointElevationGainedGraph(waypoints);
        JFreeChart wp4Chart = waypointElevationGainedGraph.createGraph();
        ChartPanel wp4Graph = new ChartPanel(wp4Chart);
        wp4Graph.setMouseWheelEnabled(true);
        panel4.add(wp4Graph);
        panel4.validate();
        scrollPanel4.getViewport().add(panel4);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scrollPanel,scrollPanel2);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scrollPanel3,scrollPanel4);
        JSplitPane splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,splitPane,splitPane2);

        splitPane.setDividerLocation(300);
        splitPane2.setDividerLocation(300);
        splitPaneMain.setDividerLocation(500);

        splitPane.setOneTouchExpandable(true);
        splitPane2.setOneTouchExpandable(true);
        splitPaneMain.setOneTouchExpandable(true);


        return splitPaneMain;
    }
}
