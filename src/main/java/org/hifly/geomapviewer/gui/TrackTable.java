package org.hifly.geomapviewer.gui;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.utility.TimeUtility;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author
 * @date 25/02/14
 */
public class TrackTable extends JTable {

    private List<Track> tracks;

    protected String[] columnToolTips = {
            "Date of the track",
            "Name of the track",
            "Distance",
            "Duration",
            "Average Speed",
            "Elevation",
            "Descent"};

    public TrackTable(List<Track> tracks) {
        super();
        this.tracks = tracks;
        setModel(new TrackTableModel());
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };
    }

    class TrackTableModel extends AbstractTableModel {

        private String[] columnNames = {"Date","Name","Distance","Duration","Speed","Elevation","Descent"};


        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return tracks.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
            Track track = tracks.get(row);
            if(col==0) {
                if(track.getStartDate()== null) {
                    return "";
                }
                else {
                    return dt1.format(track.getStartDate());
                }
            }
            else if(col==1) {
                return track.getName();
            }
            else if(col==2) {
                return String.format("%.2f", track.getTotalDistance());
            }
            else if(col==3) {
                return TimeUtility.toStringFromTimeDiff(track.getRealTime());
            }
            else if(col==4) {
                return String.format("%.2f", track.getCalculatedAvgSpeed());
            }
            else if(col==5) {
                return String.format("%.2f", track.getRealElevation());
            }
            else if(col==6) {
                return String.format("%.2f", track.getRealDescent());
            }

            return null;
        }
    }


}
