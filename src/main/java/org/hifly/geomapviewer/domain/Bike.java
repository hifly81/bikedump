package org.hifly.geomapviewer.domain;

import java.io.Serializable;

/**
 * @author
 * @date 06/03/14
 */
public class Bike implements Serializable {

    private String bikeName;
    private String bikeBrand;
    private String bikeModel;
    private String bikeType;
    private boolean selected;
    private double bikeWeight = 10;
    private double tires;

    public String getBikeBrand() {
        return bikeBrand;
    }

    public void setBikeBrand(String bikeBrand) {
        this.bikeBrand = bikeBrand;
    }

    public String getBikeModel() {
        return bikeModel;
    }

    public void setBikeModel(String bikeModel) {
        this.bikeModel = bikeModel;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public double getBikeWeight() {
        return bikeWeight;
    }

    public void setBikeWeight(double bikeWeight) {
        this.bikeWeight = bikeWeight;
    }

    public double getTires() {
        return tires;
    }

    public void setTires(double tires) {
        this.tires = tires;
    }

    public String getBikeName() {
        return bikeName;
    }

    public void setBikeName(String bikeName) {
        this.bikeName = bikeName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
