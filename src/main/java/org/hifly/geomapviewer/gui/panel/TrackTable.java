package org.hifly.geomapviewer.gui.panel;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.utility.TimeUtility;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
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
            "Elevation"};

    public TrackTable(List<Track> tracks) {
        super();
        this.tracks = tracks;
        setModel(new TrackTableModel());
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(getModel());
        setFillsViewportHeight(true);
        setRowSorter(sorter);

        //FIXME don't order by month
        final Comparator<Object> ascendingColumn0 = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {

                String s1 = (String) o1;
                String s2 = (String) o2;


                if (s1.equals("") && !s2.equals("")) {
                    return 1;
                } else if (!s1.equals("") && s2.equals("")) {
                    return -1;
                } else if (s1.equals("") && s2.equals("")) {
                    return 0;
                } else {
                    if (s1 != null && s2 != null && !s1.equalsIgnoreCase("") && !s2.equalsIgnoreCase("")) {
                        try {
                            return
                                    TimeUtility.convertToDate("dd-mm-yyyy", s1).compareTo(TimeUtility.convertToDate("dd-mm-yyyy", s2));
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }

                return -1;
            }
        };

        final Comparator<Object> ascendingColumn2 = new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {

                String s1 = (String) o1;
                String s2 = (String) o2;


                if (s1.equals("") && !s2.equals("")) {
                    return 1;
                } else if (!s1.equals("") && s2.equals("")) {
                    return -1;
                } else if (s1.equals("") && s2.equals("")) {
                    return 0;
                } else {
                    if (s1 != null && s2 != null && !s1.equalsIgnoreCase("") && !s2.equalsIgnoreCase("")) {
                            s1 = s1.replaceAll(",",".");
                            s2 = s2.replaceAll(",",".");
                            return

                                    Double.valueOf(s1).compareTo(Double.valueOf(s2));
                    }
                }

                return -1;
            }
        };


        sorter.setComparator(0, ascendingColumn0);
        sorter.setComparator(2, ascendingColumn2);
        sorter.setComparator(4, ascendingColumn2);
        sorter.setComparator(5, ascendingColumn2);
        sorter.toggleSortOrder(0);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setFont(new Font("Arial", Font.PLAIN, 10));
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
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
            Track track = tracks.get(row);
            if (col == 0) {
                if (track.getStartDate() == null) {
                    return "";
                } else {
                    return dt1.format(track.getStartDate());
                }
            } else if (col == 1) {
                return track.getName();
            } else if (col == 2) {
                return String.format("%.2f", Double.isNaN(track.getTotalDistance()) ? 0 : track.getTotalDistance());
            } else if (col == 3) {
                return TimeUtility.toStringFromTimeDiff(track.getRealTime());
            } else if (col == 4) {
                return String.format("%.2f", Double.isNaN(track.getCalculatedAvgSpeed()) ? 0 : track.getCalculatedAvgSpeed());
            } else if (col == 5) {
                return String.format("%.2f", Double.isNaN(track.getRealElevation()) ? 0 : track.getRealElevation());
            }

            return null;
        }
    }


}
