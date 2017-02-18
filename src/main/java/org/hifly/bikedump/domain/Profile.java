package org.hifly.bikedump.domain;


import java.io.Serializable;

public class Profile implements Serializable {

    private String name;
    private Double weight = 72.0;
    private Double height = 180.0;
    private Double lhtr = 100.0;
    private boolean selected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public Double getLhtr() {
        return lhtr;
    }

    public void setLhtr(Double lhtr) {
        this.lhtr = lhtr;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


}
