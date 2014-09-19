package org.hifly.geomapviewer.gui.menu;

import org.hifly.geomapviewer.graph.WaypointAvgSpeedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGainedGraph;
import org.hifly.geomapviewer.graph.WaypointElevationGraph;
import org.hifly.geomapviewer.graph.WaypointTimeGraph;
import org.hifly.geomapviewer.gui.BikeDump;
import org.hifly.geomapviewer.gui.dialog.GraphViewer;
import org.hifly.geomapviewer.gui.panel.WorkoutCalendar;
import org.hifly.geomapviewer.report.PdfReport;
import org.hifly.geomapviewer.storage.DataHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

/**
 * @author
 * @date 20/02/14
 */
public class GeoToolbar extends JToolBar {

    private BikeDump currentFrame;
    private JButton backButton;
    private JButton graphButton;
    private JButton reportButton;
    private JButton calendarButton;
    private JButton webButton;
    private JButton trophyButton;
    private JButton targetButton;

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
        backButton = makeNavigationButton("Back","Back","Back",backImageIcon);
        URL graphImageUrl = getClass().getResource("/img/bar-chart-icon.png");
        ImageIcon graphImageIcon = new ImageIcon(graphImageUrl);
        img = graphImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        graphImageIcon = new ImageIcon(img);
        graphButton = makeNavigationButton("Graph","Graph","Graph",graphImageIcon);
        URL reportImageUrl = getClass().getResource("/img/report.png");
        ImageIcon reportImageIcon = new ImageIcon(reportImageUrl);
        img = reportImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        reportImageIcon = new ImageIcon(img);
        reportButton = makeNavigationButton("Report","Report","Report",reportImageIcon);
        URL calendarImageUrl = getClass().getResource("/img/calendar.png");
        ImageIcon calendarImageIcon = new ImageIcon(calendarImageUrl);
        img = calendarImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        calendarImageIcon = new ImageIcon(img);
        calendarButton = makeNavigationButton("Calendar","Calendar","Calendar",calendarImageIcon);
        URL webImageUrl = getClass().getResource("/img/web.png");
        ImageIcon webImageIcon = new ImageIcon(webImageUrl);
        img = webImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        webImageIcon = new ImageIcon(img);
        webButton = makeNavigationButton("Internet","Internet","Internet",webImageIcon);
        URL recordImageUrl = getClass().getResource("/img/trophy.png");
        ImageIcon recordImageIcon = new ImageIcon(recordImageUrl);
        img = recordImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        recordImageIcon = new ImageIcon(img);
        trophyButton = makeNavigationButton("Record","Record","Record",recordImageIcon);
        URL targetImageUrl = getClass().getResource("/img/target.png");
        ImageIcon targetImageIcon = new ImageIcon(targetImageUrl);
        img = targetImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        targetImageIcon = new ImageIcon(img);
        targetButton = makeNavigationButton("Target","Target","Target",targetImageIcon);

        add(backButton);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (currentFrame.getFolderDetailViewer() != null && currentFrame.getFolderMapScrollViewer() != null && currentFrame.getFolderTableViewer() != null) {
                    currentFrame.repaintPanels(currentFrame.getFolderTableViewer(), currentFrame.getFolderMapScrollViewer(), currentFrame.getFolderDetailViewer());
                }
            }
        });
        add(graphButton);
        graphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                WaypointElevationGraph waypointElevationGraph = new WaypointElevationGraph(DataHolder.listsWaypointSegment);
                WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(DataHolder.listsWaypointSegment);
                WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(DataHolder.listsWaypointSegment);
                WaypointElevationGainedGraph waypointElevationGainedGraph = new WaypointElevationGainedGraph(DataHolder.listsWaypointSegment);
                new GraphViewer(currentFrame,
                                Arrays.asList(waypointElevationGraph, waypointAvgSpeedGraph, waypointTimeGraph, waypointElevationGainedGraph));
            }
        });
        add(reportButton);
        reportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (currentFrame.getTextForReport() != null && !currentFrame.getTextForReport().isEmpty()) {
                    try {
                        PdfReport report = new PdfReport(currentFrame.getTextForReport());
                        String fileName = System.getProperty("user.home") + "/.geomapviewer/report" + new Date().getTime() + ".pdf";
                        File tmpPdf = new File(fileName);
                        report.saveReport(fileName);
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(tmpPdf);
                        }
                        //TODO define a delete method on exit

                    } catch (Exception ex) {
                        //TODO define exception
                    }
                }
            }
        });
        add(calendarButton);
        calendarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new WorkoutCalendar(currentFrame);
            }
        });
        //TODO internet connection
        add(webButton);
        //TODO record section
        add(trophyButton);
        //TODO target section
        add(targetButton);

    }

    private JButton makeNavigationButton(String actionCommand, String toolTipText, String altText, ImageIcon icon) {
        JButton button;
        if(icon!=null) {
            button = new JButton(icon);
        }
        else {
            button = new JButton();
            button.setText(altText);
        }
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);

        return button;

    }

}
