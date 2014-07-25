package org.hifly.geomapviewer.domain;

import com.j256.ormlite.field.DatabaseField;
import org.hifly.geomapviewer.domain.gps.Coordinate;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.utility.TimeUtility;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 26/01/14
 */
public class Track {
    @DatabaseField(id = true)
    protected int id;
    @DatabaseField(canBeNull = false)
    protected String name;
    @DatabaseField(canBeNull = false)
    protected String fileName;
    @DatabaseField(canBeNull = false)
    protected Date startDate;
    @DatabaseField(canBeNull = false)
    protected Date endDate;
    @DatabaseField(canBeNull = false)
    protected double totalDistance;
    @DatabaseField(canBeNull = false)
    protected double calculatedAvgSpeed;
    @DatabaseField(canBeNull = false)
    protected double effectiveAvgSpeed;
    @DatabaseField(canBeNull = false)
    protected double maxSpeed;
    @DatabaseField(canBeNull = false)
    protected double calculatedElevation;
    @DatabaseField(canBeNull = false)
    protected double realElevation;
    @DatabaseField(canBeNull = false)
    protected double calculatedDescent;
    @DatabaseField(canBeNull = false)
    protected double realDescent;
    @DatabaseField(canBeNull = false)
    protected long realTime;
    @DatabaseField(canBeNull = false)
    protected long effectiveTime;
    @DatabaseField(canBeNull = true)
    protected long calories;
    @DatabaseField(canBeNull = true)
    protected double heartFrequency;
    @DatabaseField(canBeNull = true)
    protected double heartMax;
    @DatabaseField(canBeNull = false, foreign = true)
    protected Author author;
    //TODO evaluate if this can be an object
    protected String sportType;

    //calculated elements from a gps document
    protected List<SlopeSegment> slopes;
    protected List<Coordinate> coordinates;
    protected Map<String,WaypointSegment> coordinatesNewKm;
    protected Map<String,WaypointSegment> statsNewKm;
    protected double maxAltitude;
    protected double minAltitude;
    protected long climbingTimeMillis;
    protected double climbingSpeed;
    private double climbingDistance;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getEffectiveAvgSpeed() {
        return effectiveAvgSpeed;
    }

    public void setEffectiveAvgSpeed(double effectiveAvgSpeed) {
        this.effectiveAvgSpeed = effectiveAvgSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }


    public long getRealTime() {
        return realTime;
    }

    public void setRealTime(long realTime) {
        this.realTime = realTime;
    }

    public long getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public double getCalculatedAvgSpeed() {
        return calculatedAvgSpeed;
    }

    public void setCalculatedAvgSpeed(double calculatedAvgSpeed) {
        this.calculatedAvgSpeed = calculatedAvgSpeed;
    }

    public double getRealElevation() {
        return realElevation;
    }

    public void setRealElevation(double realElevation) {
        this.realElevation = realElevation;
    }

    public double getCalculatedElevation() {
        return calculatedElevation;
    }

    public void setCalculatedElevation(double calculatedElevation) {
        this.calculatedElevation = calculatedElevation;
    }

    public long getCalories() {
        return calories;
    }

    public void setCalories(long calories) {
        this.calories = calories;
    }

    public double getCalculatedDescent() {
        return calculatedDescent;
    }

    public void setCalculatedDescent(double calculatedDescent) {
        this.calculatedDescent = calculatedDescent;
    }

    public double getRealDescent() {
        return realDescent;
    }

    public void setRealDescent(double realDescent) {
        this.realDescent = realDescent;
    }

    public List<SlopeSegment> getSlopes() {
        return slopes;
    }

    public void setSlopes(List<SlopeSegment> slopes) {
        this.slopes = slopes;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public Map<String, WaypointSegment> getCoordinatesNewKm() {
        return coordinatesNewKm;
    }

    public void setCoordinatesNewKm(Map<String, WaypointSegment> coordinatesNewKm) {
        this.coordinatesNewKm = coordinatesNewKm;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, WaypointSegment> getStatsNewKm() {
        return statsNewKm;
    }

    public void setStatsNewKm(Map<String, WaypointSegment> statsNewKm) {
        this.statsNewKm = statsNewKm;
    }


    public String toString() {
        return "[Track]<br>"+id+","+name+","+fileName+
                "<br>Date:"+startDate+"<br>"+endDate+"<br>"+ TimeUtility.toStringFromTimeDiff(realTime)+
                "<br>"+TimeUtility.toStringFromTimeDiff(effectiveTime)+"<br>"+
                "Distance:"+totalDistance+
                "<br>Calculated Speed:"+calculatedAvgSpeed+"<br>Effective Speed:"+effectiveAvgSpeed+
                "<br>Max Speed:"+maxSpeed+
                "<br>Calculated Elevation:"+calculatedElevation+"<br>Real Elevation:"+realElevation+
                "<br>Calculated Descent:"+calculatedDescent+"<br>Real Descent:"+realDescent
                +"<br>Calories:"+calories+"<br>"+
                "<br>Heart avg frequency:"+heartFrequency+"<br>Heart max frequency:"+heartMax +
                "Slopes:"+slopes+"<br>"+
                "Waypoint KM:"+coordinatesNewKm+"<br>"+
                author;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public double getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(double minAltitude) {
        this.minAltitude = minAltitude;
    }

    public long getClimbingTimeMillis() {
        return climbingTimeMillis;
    }

    public void setClimbingTimeMillis(long climbingTimeMillis) {
        this.climbingTimeMillis = climbingTimeMillis;
    }

    public double getClimbingSpeed() {
        return climbingSpeed;
    }

    public void setClimbingSpeed(double climbingSpeed) {
        this.climbingSpeed = climbingSpeed;
    }

    public double getClimbingDistance() {
        return climbingDistance;
    }

    public void setClimbingDistance(double climbingDistance) {
        this.climbingDistance = climbingDistance;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public double getHeartFrequency() {
        return heartFrequency;
    }

    public void setHeartFrequency(double heartFrequency) {
        this.heartFrequency = heartFrequency;
    }

    public double getHeartMax() {
        return heartMax;
    }

    public void setHeartMax(double heartMax) {
        this.heartMax = heartMax;
    }
}
