package org.hifly.geomapviewer.domain.gps;

import java.util.Date;

/**
 * @author
 * @date 05/02/14
 */
public class WaypointSegment {
    //TODO consider even miles
    private int unit;
    private Date timeSpent;
    private long timeIncrement;
    private double avgSpeed;
    private double eleGained;
    private double ele;
    private double minHeart;
    private double maxHeart;

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public Date getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Date timeSpent) {
        this.timeSpent = timeSpent;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getEleGained() {
        return eleGained;
    }

    public void setEleGained(double eleGained) {
        this.eleGained = eleGained;
    }

    public double getEle() {
        return ele;
    }

    public void setEle(double ele) {
        this.ele = ele;
    }

    public long getTimeIncrement() {
        return timeIncrement;
    }

    public void setTimeIncrement(long timeIncrement) {
        this.timeIncrement = timeIncrement;
    }

    public double getMinHeart() {
        return minHeart;
    }

    public void setMinHeart(double minHeart) {
        this.minHeart = minHeart;
    }

    public double getMaxHeart() {
        return maxHeart;
    }

    public void setMaxHeart(double maxHeart) {
        this.maxHeart = maxHeart;
    }

    public String toString() {
        return "[WaypointSegment]<br>"+ unit +" unit.))) Time Spent:"+timeSpent+" - Time increment:"+timeIncrement+"<br> Avg speed:"+avgSpeed+" unit/h -<br> Ele gained:"+eleGained+" m.";
    }
}
