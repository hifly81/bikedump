package org.hifly.geomapviewer.domain;


import java.io.Serializable;

public class TrackPref implements Serializable {

    private double weight = 72;
    private double height = 180;
    private double lhtr = 100;
    private Bike bike;

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

    public double getLhtr() {
        return lhtr;
    }

    public void setLhtr(double lhtr) {
        this.lhtr = lhtr;
    }

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }
}
