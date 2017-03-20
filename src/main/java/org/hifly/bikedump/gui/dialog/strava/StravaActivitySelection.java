package org.hifly.bikedump.gui.dialog.strava;

import com.google.gson.internal.LinkedTreeMap;
import org.hifly.bikedump.controller.StravaController;
import org.hifly.bikedump.domain.strava.StravaActivity;
import org.hifly.bikedump.domain.strava.StravaAthlete;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StravaActivitySelection extends JDialog {

    private StravaAthlete stravaAthlete;
    private JPanel activityPanel;
    private StravaActivitySelection currentFrame = this;

    public StravaActivitySelection(Frame frame, final StravaAthlete stravaAthlete) {
        super(frame, true);

        this.stravaAthlete = stravaAthlete;

        setTitle("Strava Activity Selection");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Strava Activity Selection", null, createPanel(),"Strava Activity Selection");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JScrollPane createPanel() {

        activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setSize(new Dimension(500, 500));
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Strava Activity list");
        activityPanel.setBorder(titleBorder);

        JLabel sync = new JLabel();
        sync.setText("<html><a href=\"\">sync</a></html>");
        sync.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sync.addMouseListener(new SyncListener());

        activityPanel.add(sync);

        JScrollPane panelScroll = new JScrollPane(activityPanel);
        panelScroll.setPreferredSize(new Dimension(500, 500));
        panelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        if(stravaAthlete.getActivities() == null || stravaAthlete.getActivities().isEmpty()) {
            //load from strava
            List<StravaActivity> activities = StravaController.getInstance(stravaAthlete.getAccessToken()).getAllActivities();
            if(activities!=null && !activities.isEmpty()) {
                //This map must preserve insertion order
                Map<String,StravaActivity> mapActivities = new LinkedTreeMap();
                Map<String,String> mapActivitiesByName = new HashMap(activities.size());

                for(StravaActivity activity:activities) {
                    JCheckBox temp = new JCheckBox(activity.getName() + "-" + activity.getDate());
                    temp.setSelected(activity.isSelected());
                    temp.addItemListener(new StravaActivityCheckListener(stravaAthlete));
                    activityPanel.add(temp);
                    //athlete --> activities
                    mapActivities.put(activity.getId(), activity);
                    //TODO is need????
                    mapActivitiesByName.put(activity.getName() + "-" + activity.getDate(), activity.getId());

                }
                stravaAthlete.setActivities(mapActivities);
                stravaAthlete.setActivitiesByName(mapActivitiesByName);
            }
        }
        else {
            for (Map.Entry<String, StravaActivity> entry : stravaAthlete.getActivities().entrySet()) {
                JCheckBox temp = new JCheckBox(entry.getValue().getName() + "-" + entry.getValue().getDate());
                temp.setSelected(entry.getValue().isSelected());
                temp.addItemListener(new StravaActivityCheckListener(stravaAthlete));
                activityPanel.add(temp);
            }
        }


        return panelScroll;
    }

    class SyncListener extends MouseAdapter {

        public SyncListener() {
            super();
        }

        public void mouseClicked(MouseEvent e) {
            //find activities selected
            final StravaAthlete athlete = stravaAthlete;

            new SwingWorker<Void, String>() {
                final JLabel label = new JLabel("Loading... ", JLabel.CENTER);
                List<StravaActivity> activities;

                @Override
                protected Void doInBackground() throws Exception {
                    activityPanel.add(label, BorderLayout.CENTER);
                    label.setVisible(true);

                    activities = StravaController.getInstance(athlete.getAccessToken()).getAllActivities();
                    if (activities != null && !activities.isEmpty()) {

                        Component[] components = activityPanel.getComponents();
                        if (components != null) {
                            for (Component component : components)
                                activityPanel.remove(component);
                        }

                        //This map must preserve insertion order
                        Map<String, StravaActivity> mapActivities = new LinkedTreeMap();
                        Map<String, String> mapActivitiesByName = new HashMap(activities.size());
                        for (StravaActivity activity : activities) {
                            JCheckBox temp = new JCheckBox(activity.getName() + "-" + activity.getDate());
                            temp.setSelected(activity.isSelected());
                            temp.addItemListener(new StravaActivityCheckListener(athlete));
                            activityPanel.add(temp);

                            //athlete --> activities
                            mapActivities.put(activity.getId(), activity);
                            //TODO is need????
                            mapActivitiesByName.put(activity.getName() + "-" + activity.getDate(), activity.getId());

                        }
                        athlete.setActivities(mapActivities);
                        athlete.setActivitiesByName(mapActivitiesByName);

                        activityPanel.validate();
                        activityPanel.repaint();
                    }

                    return null;
                }

                @Override
                protected void done() {

                    label.setVisible(false);
                    if (activities == null || activities.isEmpty())
                        JOptionPane.showMessageDialog(currentFrame,
                                "No activities selected",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
            }.execute();

        }
    }


}
