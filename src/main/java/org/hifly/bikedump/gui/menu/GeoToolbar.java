package org.hifly.bikedump.gui.menu;

import org.hifly.bikedump.graph.WaypointAvgSpeedGraph;
import org.hifly.bikedump.graph.WaypointElevationGainedGraph;
import org.hifly.bikedump.graph.WaypointElevationGraph;
import org.hifly.bikedump.graph.WaypointTimeGraph;
import org.hifly.bikedump.gui.BikeDump;
import org.hifly.bikedump.gui.dialog.GraphViewer;
import org.hifly.bikedump.gui.panel.WorkoutCalendar;
import org.hifly.bikedump.report.PdfReport;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class GeoToolbar extends JToolBar {

    private static final long serialVersionUID = 20L;

    private BikeDump currentFrame;

    public GeoToolbar(BikeDump currentFrame) {
        super();
        this.currentFrame = currentFrame;
        addButtons();
    }

    private void addButtons() {
        URL backImageUrl = getClass().getResource("/img/back.png");
        ImageIcon backImageIcon = new ImageIcon(backImageUrl);
        Image img = backImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        backImageIcon = new ImageIcon(img);
        JButton backButton = makeNavigationButton("Back","Back","Back",backImageIcon);
        URL graphImageUrl = getClass().getResource("/img/bar-chart-icon.png");
        ImageIcon graphImageIcon = new ImageIcon(graphImageUrl);
        img = graphImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        graphImageIcon = new ImageIcon(img);
        JButton graphButton = makeNavigationButton("Graph","Graph","Graph",graphImageIcon);
        URL reportImageUrl = getClass().getResource("/img/report.png");
        ImageIcon reportImageIcon = new ImageIcon(reportImageUrl);
        img = reportImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        reportImageIcon = new ImageIcon(img);
        JButton reportButton = makeNavigationButton("Report","Report","Report",reportImageIcon);
        URL calendarImageUrl = getClass().getResource("/img/calendar.png");
        ImageIcon calendarImageIcon = new ImageIcon(calendarImageUrl);
        img = calendarImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        calendarImageIcon = new ImageIcon(img);
        JButton calendarButton = makeNavigationButton("Calendar","Calendar","Calendar",calendarImageIcon);
        URL webImageUrl = getClass().getResource("/img/web.png");
        ImageIcon webImageIcon = new ImageIcon(webImageUrl);
        img = webImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);


        add(backButton);
        backButton.addActionListener(event -> {
            if (currentFrame.getFolderDetailViewer() != null && currentFrame.getFolderMapScrollViewer() != null && currentFrame.getFolderTableViewer() != null)
                currentFrame.repaintPanels(currentFrame.getFolderTableViewer(), currentFrame.getFolderMapScrollViewer(), currentFrame.getFolderDetailViewer());
        });

        add(graphButton);
        graphButton.addActionListener(event -> {
            if(DataHolder.listsWaypointSegment == null) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Graphs not available",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                WaypointElevationGraph waypointElevationGraph = new WaypointElevationGraph(DataHolder.listsWaypointSegment);
                WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(DataHolder.listsWaypointSegment);
                WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(DataHolder.listsWaypointSegment);
                WaypointElevationGainedGraph waypointElevationGainedGraph = new WaypointElevationGainedGraph(DataHolder.listsWaypointSegment);
                new GraphViewer(currentFrame,
                        Arrays.asList(waypointElevationGraph, waypointAvgSpeedGraph, waypointTimeGraph, waypointElevationGainedGraph));
            }
        });

        add(reportButton);
        reportButton.addActionListener(event -> {
            if (currentFrame.getTextForReport() != null && !currentFrame.getTextForReport().isEmpty()) {
                try {
                    PdfReport report = new PdfReport(currentFrame.getTextForReport());
                    String fileName = System.getProperty("user.home") + "/.geomapviewer/report" + new Date().getTime() + ".pdf";
                    File tmpPdf = new File(fileName);
                    report.saveReport(fileName);
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().open(tmpPdf);
                    //TODO define a delete method on exit

                } catch (Exception ex) {
                    //TODO define exception
                }
            }
        });

        add(calendarButton);
        calendarButton.addActionListener(event -> new WorkoutCalendar(currentFrame));

    }

    private JButton makeNavigationButton(String actionCommand, String toolTipText, String altText, ImageIcon icon) {
        JButton button;
        if(icon != null)
            button = new JButton(icon);
        else {
            button = new JButton();
            button.setText(altText);
        }
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);

        return button;

    }

}
