package org.hifly.geomapviewer.domain.strava;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StravaAthlete implements Serializable {

    private String accessToken;
    private boolean selected;
    private Map<String, StravaActivity> activities;
    private Map<String, String> activitiesByName;
    private Map<String, String> activitiesSelected = new HashMap<>();

    public Map<String, StravaActivity> getActivities() {
        return activities;
    }

    public void setActivities(Map<String, StravaActivity> activities) {
        this.activities = activities;
    }

    public Map<String, String> getActivitiesByName() {
        return activitiesByName;
    }

    public void setActivitiesByName(Map<String, String> activitiesByName) {
        this.activitiesByName = activitiesByName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Map<String, String> getActivitiesSelected() {
        return activitiesSelected;
    }

    public void setActivitiesSelected(Map<String, String> activitiesSelected) {
        this.activitiesSelected = activitiesSelected;
    }

}
