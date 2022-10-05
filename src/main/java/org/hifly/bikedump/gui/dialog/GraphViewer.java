package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.graph.*;
import org.hifly.bikedump.utility.GUIUtility;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphViewer extends JDialog {

    private static final long serialVersionUID = 12L;

    private Map.Entry<Integer, Integer> dimension;

    public GraphViewer(final JFrame rootFrame, List<WaypointGraph> graphs) {
        super();

        initGUI(rootFrame,graphs);
    }

    private void initGUI(final JFrame rootFrame,  List<WaypointGraph> graphs) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                rootFrame.setEnabled(true);
            }
        });

        rootFrame.setEnabled(false);

        //panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey()-100, dimension.getValue()-100);
        setAlwaysOnTop(true);
        setResizable(false);
        setLocationRelativeTo(getRootPane());
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JSplitPane panel = createGraphViewer(graphs);

        add(panel);
    }

    private JSplitPane createGraphViewer(List<WaypointGraph> graphs) {
        return arrangePanel(graphs);
    }

    private JSplitPane arrangePanel(List<WaypointGraph> graphs) {
        JSplitPane splitPaneMain = null;
        List<JScrollPane> listTemp = new ArrayList<>(graphs.size());
        for(WaypointGraph graph:graphs) {
            JScrollPane scrollPanel =
                    new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            JFreeChart wpChart = graph.createGraph();
            JPanel panel = new JPanel();
            ChartPanel wpGraph = new ChartPanel(wpChart);
            wpGraph.setMouseWheelEnabled(true);
            panel.add(wpGraph);
            panel.validate();
            scrollPanel.getViewport().add(panel);
            listTemp.add(scrollPanel);
        }

        //number of container panel
        int numberOfSplitPanel = listTemp.size()/2 + listTemp.size()%2;
        List<JSplitPane> listTempSplit = new ArrayList<>(numberOfSplitPanel);
        for(int i=0;i<graphs.size();i+=2) {
            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,listTemp.get(i),
                    i+1 >=graphs.size() ? null:listTemp.get(i+1));
            splitPane.setOneTouchExpandable(true);
            listTempSplit.add(splitPane);
        }

        //TODO 1 -2 - 4 -6 -8
        if(graphs.size() <4) {
            splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,listTempSplit.get(0),null);
            splitPaneMain.setOneTouchExpandable(true);
            //TODO divider dimension
        }
        else if(graphs.size() ==4) {
            splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    listTempSplit.get(0),listTempSplit.get(1));
            listTempSplit.get(0).setDividerLocation((dimension.getKey()-100)/2);
            listTempSplit.get(1).setDividerLocation((dimension.getKey()-100)/2);
            splitPaneMain.setOneTouchExpandable(true);
            splitPaneMain.setDividerLocation(360);

        }

        return splitPaneMain;

    }
}
