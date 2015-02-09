package org.hifly.geomapviewer.domain.strava;


import java.util.List;

public class StravaAthlete {

    private String accessToken;
    private boolean selected;
    private List<StravaActivity> activities;

    public List<StravaActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<StravaActivity> activities) {
        this.activities = activities;
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
}
