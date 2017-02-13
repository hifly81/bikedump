package org.hifly.bikedump.domain;

import java.io.Serializable;
import java.util.List;

public class ProfileSetting implements Serializable {
    private String unitSystem;
    private List<Profile> profiles;
    private List<Bike> bikes;

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public String getUnitSystem() {
        return unitSystem;
    }

    public void setUnitSystem(String unitSystem) {
        this.unitSystem = unitSystem;
    }

    public List<Bike> getBikes() {
        return bikes;
    }

    public void setBikes(List<Bike> bikes) {
        this.bikes = bikes;
    }

    public Profile getSelectedProfile() {
        Profile profile = null;
        if(profiles !=null && !profiles.isEmpty()) {
            for(Profile temp:profiles) {
                if(temp.isSelected()) {
                    profile = temp;
                    break;
                }
            }
        }
        return profile;
    }


}
