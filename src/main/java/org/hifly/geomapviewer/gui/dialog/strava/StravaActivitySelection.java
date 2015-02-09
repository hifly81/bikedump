package org.hifly.geomapviewer.gui.dialog.strava;

import org.hifly.geomapviewer.domain.strava.StravaActivity;
import org.hifly.geomapviewer.domain.strava.StravaAthlete;
import org.hifly.geomapviewer.domain.strava.StravaSetting;
import org.hifly.geomapviewer.storage.GeoMapStorage;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


public class StravaActivitySelection extends JDialog {

    private StravaAthlete stravaAthlete;
    private JPanel panelRadio = null;

    public StravaActivitySelection(Frame frame, final StravaAthlete stravaAthlete) {
        super(frame, true);

        this.stravaAthlete = stravaAthlete;

        setTitle("Strava Activity Selection");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Strava Activity Selection", null, createPanel(),"Strava Activity Selection");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));

        panelRadio = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Strava Activity list");
        panelRadio.setBorder(titleBorder);

        ButtonGroup group = new ButtonGroup();

        //TODO get strava activities
        List<StravaActivity> activities = null;
        JStravaV3 strava= new JStravaV3(stravaAthlete.getAccessToken());
        List<Activity> s_activities = strava.getCurrentAthleteActivities();
        if(s_activities!=null && !s_activities.isEmpty()) {
            activities = new ArrayList<>(s_activities.size());

            for (Activity s_activity:s_activities) {
                StravaActivity activity = new StravaActivity();
                activity.setId(String.valueOf(s_activity.getId()));
                activities.add(activity);
            }

            stravaAthlete.setActivities(activities);

            for(StravaActivity activity:activities) {
                JCheckBox temp = new JCheckBox(activity.getId());
                temp.setSelected(activity.isSelected());
                temp.addItemListener(new CheckListener());
                group.add(temp);


                panelRadio.add(temp);
            }

        }

        panel.add(panelRadio);

        return panel;
    }

    class CheckListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();


        }
    }

}
