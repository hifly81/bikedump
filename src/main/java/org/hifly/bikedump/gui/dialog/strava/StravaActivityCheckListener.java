package org.hifly.bikedump.gui.dialog.strava;

import org.hifly.bikedump.controller.StravaController;
import org.hifly.bikedump.domain.strava.StravaActivity;
import org.hifly.bikedump.domain.strava.StravaAthlete;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class StravaActivityCheckListener implements ItemListener {

    private StravaAthlete athlete;

    public StravaActivityCheckListener(StravaAthlete athlete) {
        this.athlete = athlete;
    }

    public void itemStateChanged(ItemEvent e) {
        ItemSelectable is = e.getItemSelectable();
        Object selected[] = is.getSelectedObjects();
        String name = (String) selected[0];
        String id = athlete.getActivitiesByName().get(name);
        StravaActivity activity = athlete.getActivities().get(id);
        if (e.getStateChange() == ItemEvent.SELECTED) {
            activity.setSelected(true);
            athlete.getActivitiesSelected().put(id, id);
            StravaController.getInstance(athlete.getAccessToken()).getActivityData(activity);
        } else {
            activity.setSelected(false);
            athlete.getActivitiesSelected().remove(id);
        }
    }
}