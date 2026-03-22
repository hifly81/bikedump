package org.hifly.bikedump.gui.menu;

import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.gui.dialog.GraphViewer;
import org.hifly.bikedump.gui.graph.*;
import org.hifly.bikedump.gui.panel.WorkoutCalendar;
import org.hifly.bikedump.report.PdfReport;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.WaypointSegment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import static org.hifly.bikedump.utility.Constants.HOME_FOLDER_NAME;

public class Toolbar extends JToolBar {

    private static final long serialVersionUID = 20L;

    private Bikedump currentFrame;

    public Toolbar(Bikedump currentFrame) {
        super();
        this.currentFrame = currentFrame;
        addButtons();
    }

    private void addButtons() {
        URL graphImageUrl = getClass().getResource("/img/bar-chart-icon.png");
        ImageIcon graphImageIcon = new ImageIcon(graphImageUrl);
        Image img = graphImageIcon.getImage();
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

        add(graphButton);
        graphButton.addActionListener(event -> SwingUtilities.invokeLater(() -> {
            if (currentFrame.trackTable == null) {
                JOptionPane.showMessageDialog(currentFrame,
                        "No track table available",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Ensure selection is committed/focus is correct
            currentFrame.trackTable.requestFocusInWindow();

            Track selectedTrack = currentFrame.getLastSelectedTrackForGraphs();
            if (selectedTrack == null) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Select exactly one track to show graphs.",
                        "Graphs",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (DataHolder.tracksLoaded == null || DataHolder.tracksLoaded.isEmpty()) {
                JOptionPane.showMessageDialog(currentFrame,
                        "No tracks loaded",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Build waypoint segments for the selected track (keep FULL track for correct X axis)
            List<List<WaypointSegment>> selectedLists = new ArrayList<>();
            ClimbRange climbRange = null;

            if (selectedTrack.getCoordinatesNewKm() != null && !selectedTrack.getCoordinatesNewKm().isEmpty()) {
                List<WaypointSegment> segsAll = new ArrayList<>(selectedTrack.getCoordinatesNewKm().values());
                segsAll.sort(java.util.Comparator.comparingDouble(WaypointSegment::getUnit));
                selectedLists.add(segsAll);
                climbRange = ClimbExtractor.extractBestClimbRangeFromSegments(segsAll);
            }

            if (selectedLists.isEmpty()) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Graphs not available for selected track(s)",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            WaypointGraph waypointElevationGraph = new WaypointElevationGraph(
                    selectedLists,
                    true,
                    climbRange != null ? climbRange.startKm : null,
                    climbRange != null ? climbRange.endKm : null
            );


            WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(selectedLists);
            WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(selectedLists);
            WaypointElevationHistogramGraph waypointElevationDistanceGraph = new WaypointElevationHistogramGraph(selectedLists, climbRange);

            new GraphViewer(currentFrame,
                    Arrays.asList(waypointElevationGraph, waypointAvgSpeedGraph, waypointTimeGraph, waypointElevationDistanceGraph));
        }));

        add(reportButton);
        reportButton.addActionListener(event -> {
            if (currentFrame.getTextForReport() != null && !currentFrame.getTextForReport().isEmpty()) {
                try {
                    String fileName = System.getProperty("user.home") + File.separator + HOME_FOLDER_NAME + File.separator + "report" + new Date().getTime() + ".pdf";
                    JOptionPane jop = new JOptionPane();
                    jop.setMessageType(JOptionPane.PLAIN_MESSAGE);
                    String reportName = currentFrame.getTextForReport().split("\\$\\$\\$")[0];
                    jop.setMessage(reportName + "\n\nReport will be stored in:\n" + fileName);
                    JDialog dialog = jop.createDialog(null, "Message");
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                        }
                        dialog.dispose();
                        dialog.setVisible(false);
                    }).start();

                    dialog.setVisible(true);
                    PdfReport report = new PdfReport(currentFrame.getTextForReport());
                    report.saveReport(fileName);


                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "Report not available",
                            "Report not available",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(currentFrame,
                        "No Track selected - Report not available",
                        "No Track selected - Report not available",
                        JOptionPane.ERROR_MESSAGE);
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
