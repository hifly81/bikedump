package org.hifly.bikedump.domain.strava;


import java.io.Serializable;

public class StravaActivity implements Serializable {

    private String id;
    private String name;
    private String date;
    private Integer movingTimeSec;
    private Integer elapsedTimeSec;
    private Float elevation;
    private Float avgSpeed;
    private Float maxSpeed;
    private Float calories;
    private Float avgHeart;
    private Float maxHeart;
    private Float distance;
    private Integer avgTemp;
    private boolean selected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getMovingTimeSec() {
        return movingTimeSec;
    }

    public void setMovingTimeSec(Integer movingTimeSec) {
        this.movingTimeSec = movingTimeSec;
    }

    public Integer getElapsedTimeSec() {
        return elapsedTimeSec;
    }

    public void setElapsedTimeSec(Integer elapsedTimeSec) {
        this.elapsedTimeSec = elapsedTimeSec;
    }

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public Float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(Float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public Float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public Float getAvgHeart() {
        return avgHeart;
    }

    public void setAvgHeart(Float avgHeart) {
        this.avgHeart = avgHeart;
    }

    public Float getMaxHeart() {
        return maxHeart;
    }

    public void setMaxHeart(Float maxHeart) {
        this.maxHeart = maxHeart;
    }

    public Integer getAvgTemp() {
        return avgTemp;
    }

    public void setAvgTemp(Integer avgTemp) {
        this.avgTemp = avgTemp;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }
}
