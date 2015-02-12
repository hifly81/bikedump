package org.hifly.geomapviewer.gui.dialog.strava;

import com.google.gson.internal.LinkedTreeMap;
import org.hifly.geomapviewer.controller.StravaController;
import org.hifly.geomapviewer.domain.strava.StravaActivity;
import org.hifly.geomapviewer.domain.strava.StravaAthlete;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StravaActivitySelection extends JDialog {

    private StravaAthlete stravaAthlete;


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

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.setSize(new Dimension(500, 500));
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Strava Activity list");
        panel2.setBorder(titleBorder);

        JScrollPane panelScroll = new JScrollPane(panel2);
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
                    temp.addItemListener(new CheckListener());
                    panel2.add(temp);
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
                temp.addItemListener(new CheckListener());
                panel2.add(temp);
            }
        }


        return panelScroll;
    }

    class CheckListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            ItemSelectable is = e.getItemSelectable();
            Object selected[] = is.getSelectedObjects();
            String name = (String) selected[0];
            String id = stravaAthlete.getActivitiesByName().get(name);
            StravaActivity activity = stravaAthlete.getActivities().get(id);
            if(e.getStateChange() == ItemEvent.SELECTED) {
                activity.setSelected(true);
                stravaAthlete.getActivitiesSelected().put(id, id);
            }
            else {
                activity.setSelected(false);
                stravaAthlete.getActivitiesSelected().remove(id);
            }
        }
    }

}
