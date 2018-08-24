package org.hifly.bikedump.gui.panel;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.utility.TimeUtility;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

public class TrackTable extends JTable {

    private List<Track> tracks;


    protected String[] columnToolTips = {
            "Date of the track",
            "Name of the track",
            "Distance",
            "Duration",
            "Average Speed",
            "Elevation"};

    public TrackTable(List<Track> tracks) {
        super();
        this.tracks = tracks;
        setModel(new TrackTableModel());
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(getModel());
        setFillsViewportHeight(true);
        setRowSorter(sorter);

        final Comparator<Object> ascendingColumn0 = (o1, o2) -> {

            String s1 = (String) o1;
            String s2 = (String) o2;


            if (s1.equals("") && !s2.equals(""))
                return 1;
            else if (!s1.equals("") && s2.equals(""))
                return -1;
            else if (s1.equals("") && s2.equals(""))
                return 0;
            else {
                if (s1 != null && s2 != null && !s1.equalsIgnoreCase("") && !s2.equalsIgnoreCase("")) {
                    try {
                        return
                                TimeUtility.convertToDate(TimeUtility.ITA_DATE_FORMAT, s1).compareTo(TimeUtility.convertToDate(TimeUtility.ITA_DATE_FORMAT, s2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return -1;
        };

        final Comparator<Object> ascendingColumn2 = (o1, o2) -> {

            String s1 = (String) o1;
            String s2 = (String) o2;


            if (s1.equals("") && !s2.equals(""))
                return 1;
            else if (!s1.equals("") && s2.equals(""))
                return -1;
            else if (s1.equals("") && s2.equals(""))
                return 0;
            else {
                if (s1 != null && s2 != null && !s1.equalsIgnoreCase("") && !s2.equalsIgnoreCase("")) {
                        s1 = s1.replaceAll(",",".");
                        s2 = s2.replaceAll(",",".");
                        return

                                Double.valueOf(s1).compareTo(Double.valueOf(s2));
                }
            }

            return -1;
        };


        sorter.setComparator(0, ascendingColumn0);
        sorter.setComparator(2, ascendingColumn2);
        sorter.setComparator(4, ascendingColumn2);
        sorter.setComparator(5, ascendingColumn2);
        sorter.toggleSortOrder(0);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setFont(new Font("Arial", Font.PLAIN, 10));
        setIntercellSpacing(new Dimension(10,0));
        getColumnModel().setColumnMargin(10);

        Dimension tableSize =  this.getPreferredSize();
        this.getColumnModel().getColumn(0).setPreferredWidth(Math.round(tableSize.width*0.20f));
        this.getColumnModel().getColumn(1).setPreferredWidth(Math.round(tableSize.width*0.35f));
        this.getColumnModel().getColumn(2).setPreferredWidth(Math.round(tableSize.width*0.15f));
        this.getColumnModel().getColumn(3).setPreferredWidth(Math.round(tableSize.width*0.15f));
        this.getColumnModel().getColumn(4).setPreferredWidth(Math.round(tableSize.width*0.15f));

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

    public class TrackTableModel extends AbstractTableModel {

        private String[] columnNames = {"Date", "Name", "Distance", "Duration", "Speed", "Elevation"};

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
            SimpleDateFormat dt1 = new SimpleDateFormat(TimeUtility.ITA_DATE_FORMAT);
            Track track = tracks.get(row);
            if (col == 0) {
                if (track.getStartDate() == null)
                    return "";
                else
                    return dt1.format(track.getStartDate());
            }
            else if (col == 1)
                return track.getName();
            else if (col == 2)
                return String.format("%.2f", Double.isNaN(track.getTotalDistance()) ? 0 : track.getTotalDistance());
            else if (col == 3)
                return TimeUtility.toStringFromTimeDiff(track.getRealTime());
            else if (col == 4)
                return String.format("%.2f", Double.isNaN(track.getCalculatedAvgSpeed()) ? 0 : track.getEffectiveAvgSpeed());
            else if (col == 5)
                return String.format("%.2f", Double.isNaN(track.getRealElevation()) ? 0 : track.getRealElevation());

            return null;
        }

        public Track getTrackAt(int row) {
            return tracks.get(row);
        }
    }


}
