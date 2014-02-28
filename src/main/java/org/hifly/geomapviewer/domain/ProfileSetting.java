package org.hifly.geomapviewer.domain;

/**
 * @author
 * @date 27/02/14
 */
public class ProfileSetting {
    private double weight = 72;
    private double height = 180;
    private String unitSystem;
    //TODO bike info must be a list, a profile can have more than 1 bike
    private String bikeBrand;
    private String bikeModel;
    private String bikeType;
    private double bikeWeight = 10;
    private double tires;

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

    public double getTires() {
        return tires;
    }

    public void setTires(double tires) {
        this.tires = tires;
    }

    public double getBikeWeight() {
        return bikeWeight;
    }

    public void setBikeWeight(double bikeWeight) {
        this.bikeWeight = bikeWeight;
    }

    public String getUnitSystem() {
        return unitSystem;
    }

    public void setUnitSystem(String unitSystem) {
        this.unitSystem = unitSystem;
    }
}
