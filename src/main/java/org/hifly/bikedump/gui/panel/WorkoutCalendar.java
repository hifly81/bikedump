package org.hifly.bikedump.gui.panel;

import org.apache.commons.collections.CollectionUtils;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.TrackSelected;
import org.hifly.bikedump.gui.Bikedump;
import org.hifly.bikedump.storage.DataHolder;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

public class WorkoutCalendar extends JFrame {

    private static final long serialVersionUID = 27L;

    private Bikedump mapViewer;
    private WorkoutCalendar currentFrame;

    static JLabel lblMonth;
    static JButton btnPrev, btnNext;
    static JTable tableCalendar;
    static JComboBox cmbYear;
    static DefaultTableModel tableModelCalendar;
    static int realYear, realMonth, realDay, currentYear, currentMonth;
    static Map<Integer, List<Track>> mapTrackByDay = new HashMap<>();
    static HashMap<String, List<Track>> tracksByMonth = DataHolder.getTracksByMonth();

    private JPanel pnlCalendar;
    private JLabel lblYear;
    private JScrollPane stblCalendar;

    public WorkoutCalendar(Bikedump mapViewer) {
        this.mapViewer = mapViewer;
        this.currentFrame = this;

        setTitle("Workout Calendar");

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.max(330, (int) (screen.width * 0.25));
        int h = Math.max(375, (int) (screen.height * 0.25));
        setSize(w, h);

        getContentPane().setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        lblMonth = new JLabel("January");
        lblYear = new JLabel("Change year:");
        cmbYear = new JComboBox();
        btnPrev = new JButton("<<");
        btnNext = new JButton(">>");
        tableModelCalendar = new DefaultTableModel() {
            private static final long serialVersionUID = 28L;
            public boolean isCellEditable(int rowIndex, int mColIndex) { return false; }
        };
        tableCalendar = new JTable(tableModelCalendar);
        stblCalendar = new JScrollPane(tableCalendar);
        pnlCalendar = new JPanel(null);

        pnlCalendar.setBorder(BorderFactory.createTitledBorder("Calendar"));

        btnPrev.addActionListener(new btnPrev_Action());
        btnNext.addActionListener(new btnNext_Action());
        cmbYear.addActionListener(new cmbYear_Action());

        getContentPane().add(pnlCalendar);
        pnlCalendar.add(lblMonth);
        pnlCalendar.add(lblYear);
        pnlCalendar.add(cmbYear);
        pnlCalendar.add(btnPrev);
        pnlCalendar.add(btnNext);
        pnlCalendar.add(stblCalendar);

        doLayoutBounds();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                doLayoutBounds();
            }
        });

        setResizable(true);
        setLocationRelativeTo(mapViewer);
        setVisible(true);

        GregorianCalendar cal = new GregorianCalendar();
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH);
        realMonth = cal.get(GregorianCalendar.MONTH);
        realYear = cal.get(GregorianCalendar.YEAR);
        currentMonth = realMonth;
        currentYear = realYear;

        String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++)
            tableModelCalendar.addColumn(headers[i]);

        tableCalendar.getParent().setBackground(tableCalendar.getBackground());

        tableCalendar.getTableHeader().setResizingAllowed(false);
        tableCalendar.getTableHeader().setReorderingAllowed(false);

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

                    DataHolder.tracksSelected.clear();
                    for (Track track : monthlyTracks) {
                        startDate = track.getStartDate();
                        if (startDate != null) {
                            cal.setTime(startDate);
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            Object cell = tableCalendar.getValueAt(row, col);
                            if (cell instanceof Integer && day == (Integer) cell) {
                                DataHolder.tracksSelected.add(new TrackSelected(track.getFileName(), track.getName()));
                            }
                        }
                    }

                    if (!CollectionUtils.isEmpty(DataHolder.tracksSelected))
                        currentFrame.mapViewer.loadSelectedTracks(currentFrame.mapViewer.trackTable);
                }
            }
        });

        tableCalendar.setRowHeight(Math.max(38, (getHeight() - 120) / 6));
        tableModelCalendar.setColumnCount(7);
        tableModelCalendar.setRowCount(6);

        for (int i = realYear - 100; i <= realYear + 100; i++)
            cmbYear.addItem(String.valueOf(i));

        refreshCalendar(realMonth, realYear);
    }

    private void doLayoutBounds() {
        int frameW = getContentPane().getWidth();
        int frameH = getContentPane().getHeight();

        int pad = 10;

        // panel inside frame
        pnlCalendar.setBounds(0, 0, frameW, frameH);

        // top controls area
        int topY = 25;
        int btnW = 50;
        int btnH = 25;

        btnPrev.setBounds(pad, topY, btnW, btnH);
        btnNext.setBounds(frameW - pad - btnW, topY, btnW, btnH);

        lblMonth.setBounds(frameW / 2 - 90, topY, 180, 25);

        // calendar table area
        int tableTop = 50;
        int bottomAreaH = 45;
        int tableW = frameW - (pad * 2);
        int tableH = frameH - tableTop - bottomAreaH - pad;

        stblCalendar.setBounds(pad, tableTop, tableW, Math.max(150, tableH));

        // bottom controls
        int bottomY = frameH - bottomAreaH;
        lblYear.setBounds(pad, bottomY + 10, 90, 20);
        cmbYear.setBounds(frameW - pad - 80, bottomY + 10, 80, 20);

        // adjust row height when resizing
        if (tableCalendar != null) {
            tableCalendar.setRowHeight(Math.max(38, stblCalendar.getHeight() / 6));
        }

        pnlCalendar.revalidate();
        pnlCalendar.repaint();
    }

    public static void refreshCalendar(int month, int year) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        int nod, som;

        btnPrev.setEnabled(true);
        btnNext.setEnabled(true);
        if (month == 0 && year <= realYear - 10) {
            btnPrev.setEnabled(false);
        }
        if (month == 11 && year >= realYear + 100) {
            btnNext.setEnabled(false);
        }
        lblMonth.setText(months[month]);
        lblMonth.setBounds(160 - lblMonth.getPreferredSize().width / 2, 25, 180, 25);
        cmbYear.setSelectedItem(String.valueOf(year));

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                tableModelCalendar.setValueAt(null, i, j);
            }
        }

        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        som = cal.get(GregorianCalendar.DAY_OF_WEEK);

        for (int i = 1; i <= nod; i++) {
            int row = (i + som - 2) / 7;
            int column = (i + som - 2) % 7;
            tableModelCalendar.setValueAt(i, row, column);
        }

        tableCalendar.setDefaultRenderer(tableCalendar.getColumnClass(0), new CellCalendarRenderer());
    }

    private static void getMonthlyTracks() {
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
            if (column == 0 || column == 6) {
                setBackground(new Color(255, 220, 220));
            } else {
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
                if (intValue == realDay && currentMonth == realMonth && currentYear == realYear) {
                    setBackground(new Color(220, 220, 255));
                }
            }
            setBorder(null);
            setForeground(Color.black);

            return this;
        }
    }

    static class btnPrev_Action implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (currentMonth == 0) {
                currentMonth = 11;
                currentYear -= 1;
            } else {
                currentMonth -= 1;
            }
            getMonthlyTracks();
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class btnNext_Action implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (currentMonth == 11) {
                currentMonth = 0;
                currentYear += 1;
            } else {
                currentMonth += 1;
            }
            getMonthlyTracks();
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class cmbYear_Action implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (cmbYear.getSelectedItem() != null) {
                String b = cmbYear.getSelectedItem().toString();
                currentYear = Integer.parseInt(b);
                getMonthlyTracks();
                refreshCalendar(currentMonth, currentYear);
            }
        }
    }
}