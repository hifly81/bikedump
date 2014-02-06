package org.hifly.geomapviewer.domain.gps;

import java.util.Date;

/**
 * @author Giovanni Marigi <g.marigi at beeweeb.com>
 * @date 05/02/14
 */
public class WaypointKm {
    private int km;
    private long timeSpent;
    private double avgSpeed;
    private double eleGained;

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
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

    public String toString() {
        return "[WaypointKm]\n"+km+" km.,"+timeSpent+" millis,"+avgSpeed+" km/h,"+eleGained+" m.\n";
    }
}
