package org.hifly.bikedump.gui.menu;

import org.hifly.bikedump.graph.WaypointAvgSpeedGraph;
import org.hifly.bikedump.graph.WaypointElevationGainedGraph;
import org.hifly.bikedump.graph.WaypointElevationGraph;
import org.hifly.bikedump.graph.WaypointTimeGraph;
import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.gui.dialog.GraphViewer;
import org.hifly.bikedump.gui.panel.WorkoutCalendar;
import org.hifly.bikedump.report.PdfReport;
import org.hifly.bikedump.storage.DataHolder;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.panel.TrackTable;
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
        URL backImageUrl = getClass().getResource("/img/home.png");
        ImageIcon backImageIcon = new ImageIcon(backImageUrl);
        Image img = backImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        backImageIcon = new ImageIcon(img);
        JButton backButton = makeNavigationButton("Home","Home","Home",backImageIcon);
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
            if (currentFrame.getFolderMapScrollViewer() != null
                    && currentFrame.getFolderTableViewer() != null
                    && currentFrame.getHomeAggregateDetailViewer() != null) {

                if (currentFrame.trackTable != null) {
                    currentFrame.trackTable.clearSelection();
                }
                DataHolder.tracksSelected.clear();

                currentFrame.repaintPanels(
                        currentFrame.getFolderTableViewer(),
                        currentFrame.getFolderMapScrollViewer(),
                        currentFrame.getHomeAggregateDetailViewer()
                );
            }
        });

        add(graphButton);
        graphButton.addActionListener(event -> {
            if (currentFrame.trackTable == null) {
                JOptionPane.showMessageDialog(currentFrame,
                        "No track table available",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int[] selectedRows = currentFrame.trackTable.getSelectedRows();

            if (selectedRows == null || selectedRows.length == 0) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Select one track to show graphs.",
                        "Graphs",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (selectedRows.length > 1) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Select only one track to show graphs.",
                        "Graphs",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int viewRow = selectedRows[0];
            int modelRow = currentFrame.trackTable.convertRowIndexToModel(viewRow);

            Track selectedTrack = ((TrackTable.TrackTableModel) currentFrame.trackTable.getModel()).getTrackAt(modelRow);

            if (selectedTrack == null) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Can't determine selected track.",
                        "Graphs",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (DataHolder.tracksLoaded == null || DataHolder.tracksLoaded.isEmpty()) {
                JOptionPane.showMessageDialog(currentFrame,
                        "No tracks loaded",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Build waypoint segments ONLY for selected tracks
            List<List<WaypointSegment>> selectedLists = new ArrayList<>();

            for (int viewRow2: selectedRows) {
                int modelRow2 = currentFrame.trackTable.convertRowIndexToModel(viewRow2);
                Track track = ((TrackTable.TrackTableModel) currentFrame.trackTable.getModel()).getTrackAt(modelRow2);
                if (track != null && track.getCoordinatesNewKm() != null && !track.getCoordinatesNewKm().isEmpty()) {
                    selectedLists.add(new ArrayList<>(track.getCoordinatesNewKm().values()));
                }
            }

            if (selectedLists.isEmpty()) {
                JOptionPane.showMessageDialog(currentFrame,
                        "Graphs not available for selected track(s)",
                        "Graphs not available",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Use the same graph logic, but based on selection
            WaypointElevationGraph waypointElevationGraph = new WaypointElevationGraph(selectedLists);
            WaypointAvgSpeedGraph waypointAvgSpeedGraph = new WaypointAvgSpeedGraph(selectedLists);
            WaypointTimeGraph waypointTimeGraph = new WaypointTimeGraph(selectedLists);
            WaypointElevationGainedGraph waypointElevationGainedGraph = new WaypointElevationGainedGraph(selectedLists);

            new GraphViewer(currentFrame,
                    Arrays.asList(waypointElevationGraph, waypointAvgSpeedGraph, waypointTimeGraph, waypointElevationGainedGraph));
        });

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
