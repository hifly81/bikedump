package org.hifly.geomapviewer.domain.gps;

import java.util.Date;

/**
 * @author
 * @date 05/02/14
 */
//TODO consider even miles
public class WaypointKm {
    private int km;
    private Date timeSpent;
    private long timeIncrement;
    private double avgSpeed;
    private double eleGained;
    private double ele;

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
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

    public String toString() {
        return "[WaypointKm]<br>"+km+" km.))) Time Spent:"+timeSpent+" - Time increment:"+timeIncrement+"<br> Avg speed:"+avgSpeed+" km/h -<br> Ele gained:"+eleGained+" m.";
    }
}
