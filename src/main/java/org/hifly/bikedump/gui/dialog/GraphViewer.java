package org.hifly.bikedump.gui.dialog;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hifly.bikedump.gui.graph.WaypointGraph;
import org.hifly.bikedump.utility.GUIUtility;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphViewer extends JDialog {

    @Serial
    private static final long serialVersionUID = 12L;

    private Map.Entry<Integer, Integer> dimension;

    public GraphViewer(final JFrame rootFrame, List<WaypointGraph> graphs) {
        super();
        initGUI(rootFrame, graphs);
    }

    private void initGUI(final JFrame rootFrame, List<WaypointGraph> graphs) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                rootFrame.setEnabled(true);
            }
        });

        rootFrame.setEnabled(false);

        // panel dimension
        dimension = GUIUtility.getScreenDimension();
        setSize(dimension.getKey() - 100, dimension.getValue() - 100);
        setAlwaysOnTop(true);
        setResizable(false);
        setLocationRelativeTo(getRootPane());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JSplitPane panel = createGraphViewer(graphs);

        // IMPORTANT: let the split pane fill the dialog
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JSplitPane createGraphViewer(List<WaypointGraph> graphs) {
        return arrangePanel(graphs);
    }

    private JSplitPane arrangePanel(List<WaypointGraph> graphs) {
        JSplitPane splitPaneMain = null;

        List<JScrollPane> listTemp = new ArrayList<>(graphs.size());
        for (WaypointGraph graph : graphs) {
            JFreeChart wpChart = graph.createGraph();

            ChartPanel wpGraph = new ChartPanel(wpChart, true);
            wpGraph.setMouseWheelEnabled(true);

            // Allow the chart to scale with large containers (prevents "unused space")
            wpGraph.setMaximumDrawWidth(Integer.MAX_VALUE);
            wpGraph.setMaximumDrawHeight(Integer.MAX_VALUE);
            wpGraph.setMinimumDrawWidth(0);
            wpGraph.setMinimumDrawHeight(0);
            wpGraph.setBorder(null);

            // Put ChartPanel directly into the scrollpane viewport (no FlowLayout wrapper)
            JScrollPane scrollPanel = new JScrollPane(wpGraph,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

            // Optional: remove border/padding that looks like empty space
            scrollPanel.setBorder(null);

            listTemp.add(scrollPanel);
        }

        // number of container panel
        List<JSplitPane> listTempSplit = getJSplitPanes(graphs, listTemp);

        if (graphs.size() < 4) {
            splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listTempSplit.get(0), null);
            splitPaneMain.setOneTouchExpandable(true);
            splitPaneMain.setResizeWeight(1.0); // all space to top when bottom is null
        } else if (graphs.size() == 4) {
            splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listTempSplit.get(0), listTempSplit.get(1));
            listTempSplit.get(0).setDividerLocation((dimension.getKey() - 100) / 2);
            listTempSplit.get(1).setDividerLocation((dimension.getKey() - 100) / 2);
            splitPaneMain.setOneTouchExpandable(true);
            splitPaneMain.setResizeWeight(0.5);

            // Better than a hardcoded 360: split in half of available height
            splitPaneMain.setDividerLocation((dimension.getValue() - 100) / 2);
        }

        return splitPaneMain;
    }

    private static @NonNull List<JSplitPane> getJSplitPanes(List<WaypointGraph> graphs, List<JScrollPane> listTemp) {
        int numberOfSplitPanel = listTemp.size() / 2 + listTemp.size() % 2;
        List<JSplitPane> listTempSplit = new ArrayList<>(numberOfSplitPanel);
        for (int i = 0; i < graphs.size(); i += 2) {
            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    listTemp.get(i),
                    i + 1 >= graphs.size() ? null : listTemp.get(i + 1)
            );
            splitPane.setOneTouchExpandable(true);
            splitPane.setResizeWeight(0.5); // distribute space evenly
            listTempSplit.add(splitPane);
        }
        return listTempSplit;
    }
}