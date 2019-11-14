package org.hifly.bikedump.gui.panel;

import org.apache.commons.collections.CollectionUtils;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.gui.BikeDump;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class WorkoutCalendar extends JFrame {

    private static final long serialVersionUID = 27L;

    private BikeDump mapViewer;
    private WorkoutCalendar currentFrame;

    static JLabel lblMonth;
    static JButton btnPrev, btnNext;
    static JTable tableCalendar;
    static JComboBox cmbYear;
    static DefaultTableModel tableModelCalendar;
    static int realYear, realMonth, realDay, currentYear, currentMonth;
    static Map<Integer, List<Track>> mapTrackByDay = new HashMap<>();
    static HashMap<String, List<Track>> tracksByMonth = DataHolder.getTracksByMonth();

    //TODO derive dimension from parent
    public WorkoutCalendar(BikeDump mapViewer) {
        this.mapViewer = mapViewer;
        this.currentFrame = this;

        setTitle("Workout Calendar");
        setSize(330, 375);
        getContentPane().setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Create controls
        lblMonth = new JLabel("January");
        JLabel lblYear = new JLabel("Change year:");
        cmbYear = new JComboBox();
        btnPrev = new JButton("&lt;&lt;");
        btnNext = new JButton("&gt;&gt;");
        tableModelCalendar = new DefaultTableModel() {
            private static final long serialVersionUID = 28L;

            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        tableCalendar = new JTable(tableModelCalendar);
        JScrollPane stblCalendar = new JScrollPane(tableCalendar);
        JPanel pnlCalendar = new JPanel(null);

        //Set border
        pnlCalendar.setBorder(BorderFactory.createTitledBorder("Calendar"));

        //Register action listeners
        btnPrev.addActionListener(new btnPrev_Action());
        btnNext.addActionListener(new btnNext_Action());
        cmbYear.addActionListener(new cmbYear_Action());

        //Add controls to pane
        getContentPane().add(pnlCalendar);
        pnlCalendar.add(lblMonth);
        pnlCalendar.add(lblYear);
        pnlCalendar.add(cmbYear);
        pnlCalendar.add(btnPrev);
        pnlCalendar.add(btnNext);
        pnlCalendar.add(stblCalendar);

        //Set bounds
        pnlCalendar.setBounds(0, 0, 320, 335);
        lblMonth.setBounds(160 - lblMonth.getPreferredSize().width / 2, 25, 100, 25);
        lblYear.setBounds(10, 305, 80, 20);
        cmbYear.setBounds(230, 305, 80, 20);
        btnPrev.setBounds(10, 25, 50, 25);
        btnNext.setBounds(260, 25, 50, 25);
        stblCalendar.setBounds(10, 50, 300, 250);

        //Make frame visible
        setResizable(false);
        setVisible(true);

        //Get real month/year
        GregorianCalendar cal = new GregorianCalendar(); //Create calendar
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); //Get day
        realMonth = cal.get(GregorianCalendar.MONTH); //Get month
        realYear = cal.get(GregorianCalendar.YEAR); //Get year
        currentMonth = realMonth; //Match month and year
        currentYear = realYear;

        //Add headers
        String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; //All headers
        for (int i = 0; i < 7; i++)
            tableModelCalendar.addColumn(headers[i]);

        tableCalendar.getParent().setBackground(tableCalendar.getBackground()); //Set background

        //No resize/reorder
        tableCalendar.getTableHeader().setResizingAllowed(false);
        tableCalendar.getTableHeader().setReorderingAllowed(false);

        //Single cell selection
        tableCalendar.setColumnSelectionAllowed(true);
        tableCalendar.setRowSelectionAllowed(true);
        tableCalendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableCalendar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tableCalendar.rowAtPoint(e.getPoint());
                int col = tableCalendar.columnAtPoint(e.getPoint());

                List<Track> monthlyTracks = tracksByMonth.get(currentMonth + "-" + currentYear);
                if (!CollectionUtils.isEmpty(monthlyTracks)) {
                    Calendar cal = Calendar.getInstance();
                    Date startDate;
                    //clear data holder
                    DataHolder.tracksSelected.clear();
                    for (Track track : monthlyTracks) {
                        startDate = track.getStartDate();
                        if (startDate != null) {
                            cal.setTime(startDate);
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            if (day == (Integer) tableCalendar.getValueAt(row, col)) {
                                DataHolder.tracksSelected.add(new TrackSelected(track.getFileName(), track.getName()));
                            }
                        }
                    }

                    if (!CollectionUtils.isEmpty(DataHolder.tracksSelected))
                        currentFrame.mapViewer.loadSelectedTracks(currentFrame.mapViewer.trackTable);
                }
            }

        });

        //Set row/column count
        tableCalendar.setRowHeight(38);
        tableModelCalendar.setColumnCount(7);
        tableModelCalendar.setRowCount(6);

        //Populate table
        for (int i = realYear - 100; i <= realYear + 100; i++)
            cmbYear.addItem(String.valueOf(i));

        //Refresh calendar
        refreshCalendar(realMonth, realYear); //Refresh calendar
    }

    public static void refreshCalendar(int month, int year) {
        //Variables
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        int nod, som; //Number Of Days, Start Of Month

        //Allow/disallow buttons
        btnPrev.setEnabled(true);
        btnNext.setEnabled(true);
        if (month == 0 && year <= realYear - 10) {
            btnPrev.setEnabled(false);
        } //Too early
        if (month == 11 && year >= realYear + 100) {
            btnNext.setEnabled(false);
        } //Too late
        lblMonth.setText(months[month]); //Refresh the month label (at the top)
        lblMonth.setBounds(160 - lblMonth.getPreferredSize().width / 2, 25, 180, 25); //Re-align label with calendar
        cmbYear.setSelectedItem(String.valueOf(year)); //Select the correct year in the combo box

        //Clear table
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                tableModelCalendar.setValueAt(null, i, j);
            }
        }

        //Get first day of month and number of days
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        som = cal.get(GregorianCalendar.DAY_OF_WEEK);

        //Draw calendar
        for (int i = 1; i <= nod; i++) {
            int row = (i + som - 2) / 7;
            int column = (i + som - 2) % 7;
            tableModelCalendar.setValueAt(i, row, column);
        }

        //Apply renderers
        tableCalendar.setDefaultRenderer(tableCalendar.getColumnClass(0), new CellCalendarRenderer());
    }

    private static void getMonthlyTracks() {
        //get tracks by month
        if (!mapTrackByDay.isEmpty()) {
            mapTrackByDay.clear();
        }
        if (tracksByMonth != null) {
            List<Track> tracks = tracksByMonth.get(currentMonth + "-" + currentYear);
            if (!CollectionUtils.isEmpty(tracks)) {
                Calendar cal = Calendar.getInstance();
                Date startDate;
                for (Track track : tracks) {
                    startDate = track.getStartDate();
                    cal.setTime(startDate);
                    int key = cal.get(Calendar.DAY_OF_MONTH);
                    List<Track> tracksTemp;
                    if (!mapTrackByDay.containsKey(key))
                        tracksTemp = new ArrayList<>();
                    else
                        tracksTemp = mapTrackByDay.get(key);
                    tracksTemp.add(track);
                    if (!mapTrackByDay.containsKey(key))
                        mapTrackByDay.put(key, tracksTemp);
                }
            }
        }
    }

    static class CellCalendarRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 29L;

        static ImageIcon imgBikeIcon;

        static {
            URL bikeImageUrl = WorkoutCalendar.class.getResource("/img/bike.png");
            imgBikeIcon = new ImageIcon(bikeImageUrl);
            Image imgBike = imgBikeIcon.getImage();
            imgBike = imgBike.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
            imgBikeIcon = new ImageIcon(imgBike);
            getMonthlyTracks();

        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (column == 0 || column == 6) { //Week-end
                setBackground(new Color(255, 220, 220));
            } else { //Week
                setBackground(new Color(255, 255, 255));
            }
            if (value != null) {
                List<Track> tracks = mapTrackByDay.get(Integer.parseInt(value.toString()));
                int intValue = Integer.parseInt(value.toString());
                if (!CollectionUtils.isEmpty(tracks)) {
                    setText("<html><body><b>" + value + "</b></body></html>");
                    setIcon(imgBikeIcon);
                } else {
                    setText(value.toString());
                    setIcon(null);
                }
                if (intValue == realDay && currentMonth == realMonth && currentYear == realYear) { //Today
                    setBackground(new Color(220, 220, 255));
                }

            }
            setBorder(null);
            setForeground(Color.black);

            return this;
        }
    }

    static class btnPrev_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 0) { //Back one year
                currentMonth = 11;
                currentYear -= 1;
            } else { //Back one month
                currentMonth -= 1;
            }
            getMonthlyTracks();
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class btnNext_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 11) { //Foward one year
                currentMonth = 0;
                currentYear += 1;
            } else { //Foward one month
                currentMonth += 1;
            }
            getMonthlyTracks();
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class cmbYear_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (cmbYear.getSelectedItem() != null) {
                String b = cmbYear.getSelectedItem().toString();
                currentYear = Integer.parseInt(b);
                getMonthlyTracks();
                refreshCalendar(currentMonth, currentYear);
            }
        }
    }
}