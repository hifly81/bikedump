package org.hifly.geomapviewer.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 27/02/14
 */
//TODO multiple profile settings --> similar
public class ProfileSetting implements Serializable {
    private String profileName;
    private Double weight = 72.0;
    private Double height = 180.0;
    private Double lhtr = 100.0;
    private String unitSystem;
    private List<Bike> bikes;

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
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

    public Double getLhtr() {
        return lhtr;
    }

    public void setLhtr(Double lhtr) {
        this.lhtr = lhtr;
    }
}
