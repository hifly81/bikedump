package org.hifly.bikedump.domain.gps;

import org.hifly.bikedump.utility.TimeUtility;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class SlopeSegment implements Serializable {

    private static final long serialVersionUID = 2L;

    protected double startLatitude;
    protected double endLatitude;
    protected double startLongitude;
    protected double endLongitude;
    protected double distance;
    protected double elevation;
    protected double gradient;
    protected double startElevation;
    protected double endElevation;
    protected double startDistance;
    protected double endDistance;
    protected double avgSpeed;
    protected double maximumGradient;
    //watt
    protected double power;
    //m/h
    protected double vam;
    protected Date startDate;
    protected Date endDate;
    protected String name;
    protected String description;

    protected List<Waypoint> waypoints;

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getGradient() {
        return gradient;
    }

    public void setGradient(double gradient) {
        this.gradient = gradient;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getStartElevation() {
        return startElevation;
    }

    public void setStartElevation(double startElevation) {
        this.startElevation = startElevation;
    }

    public double getEndElevation() {
        return endElevation;
    }

    public void setEndElevation(double endElevation) {
        this.endElevation = endElevation;
    }

    public double getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(double startDistance) {
        this.startDistance = startDistance;
    }

    public double getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(double endDistance) {
        this.endDistance = endDistance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getMaximumGradient() {
        return maximumGradient;
    }

    public void setMaximumGradient(double maximumGradient) {
        this.maximumGradient = maximumGradient;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getVam() {
        return vam;
    }

    public void setVam(double vam) {
        this.vam = vam;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "[SlopeSegment]<br>"+distance+" km,"+startDistance+" km - "+endDistance+" km,<br>"
                +elevation+" m,"+startElevation+" m - "+endElevation+" m,<br>"
                +gradient+" %,<br>"
                +avgSpeed+" km/h,<br>"
                + TimeUtility.toStringFromTimeDiff(endDate.getTime()-startDate.getTime());
    }
}
