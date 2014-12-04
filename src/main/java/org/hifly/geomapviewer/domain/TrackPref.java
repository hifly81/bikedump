package org.hifly.geomapviewer.domain;


import java.io.Serializable;

public class TrackPref implements Serializable {

    private Profile profile;
    private Bike bike;

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
