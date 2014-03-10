package org.hifly.geomapviewer.domain;

import java.util.List;

/**
 * @author
 * @date 27/02/14
 */
public class ProfileSetting {
    private double weight = 72;
    private double height = 180;
    private String unitSystem;
    private List<Bike> bikes;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
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
}
