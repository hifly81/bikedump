package org.hifly.bikedump.gps;

import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.utility.GPSUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class GPSDocument {

    protected Logger log = LoggerFactory.getLogger(GPSDocument.class);
    protected List<Track> result = new ArrayList();
    protected List<Coordinate> coordinates = new ArrayList();
    protected List<Waypoint> waypoints = new ArrayList();
    protected Map<String, Double> elevationMap = GeoMapStorage.gpsElevationMap;
    protected Date startTime, endTime = null;
    protected ProfileSetting profileSetting;

    protected double totalDistance,
           totalDistanceCalculated,
           totalSpeed, totalEffectiveSpeed,
           totalElevation, totalCalculatedElevation,
           totalDescent, totalCalculatedDescent,
           maxSpeed,
           maxHeart,
           totalTime,
           totalHeart;
    protected int totalEffectiveSpeedPoints, totalCalculatedSpeedPoints, calories, heart;
    protected long totalTimeDiff = 0;


    public GPSDocument(ProfileSetting profileSetting) {
        totalDistance = totalDistanceCalculated =
        totalSpeed = totalEffectiveSpeed =
        totalElevation = totalCalculatedElevation =
        totalDescent = totalCalculatedDescent =
        maxSpeed =
        maxHeart =
        totalTime =
        totalHeart = 0;
        totalEffectiveSpeedPoints = totalCalculatedSpeedPoints = calories = heart = 0;
        this.profileSetting = profileSetting;
    }

    protected void addSpeedElement(double distance, double timeDiffInHour) {
        if (timeDiffInHour != 0) {
            double speed = distance / timeDiffInHour;
            if (speed < 100) {

                if (speed > maxSpeed) {
                    maxSpeed = speed;
                }
                totalEffectiveSpeed += speed;
                totalEffectiveSpeedPoints++;
            } else {
                //log.warn(gpsFile+": found a spike in speed for coordinate ["+currentLat+","+currentLon+"]:"+speed);

            }
            totalSpeed += speed;
            totalCalculatedSpeedPoints++;
        }
    }

    protected void addHeart(double heartValue) {
        totalHeart+=heartValue;
        heart+=1;
    }

    protected void addCoordinateElement(double currentLat, double currentLon) {
        Coordinate coordinate = new Coordinate(currentLat, currentLon);
        coordinates.add(coordinate);
    }

    protected void createWaypointElement(
            double currentLat,
            double currentLon,
            double lastLat,
            double lastLon,
            double distance,
            BigDecimal currentCalcEle,
            BigDecimal lastCalcEle,
            Date currentTime,
            Date lastTime,
            double heart,
            double totalDistance) {
        long diffMillis = 0;
        if(currentTime!=null && lastTime!=null)
            diffMillis = currentTime.getTime() - lastTime.getTime();
        Double eleCurrent = elevationMap != null? elevationMap.get(GPSUtility.getKeyForCoordinatesMap(currentLat + "-" + currentLon)): currentCalcEle.doubleValue();
        if (currentLat != lastLat && currentLon != lastLon) {
            totalTimeDiff += diffMillis;
            Double eleLast = elevationMap != null? elevationMap.get(GPSUtility.getKeyForCoordinatesMap(lastLat + "-" + lastLon)) : lastCalcEle.doubleValue();
            if (eleCurrent != null && eleLast != null) {
                Double eleGained = eleCurrent - eleLast;
                if (eleGained > 0)
                    totalElevation += eleGained;
                if (eleGained < 0)
                    totalDescent += Math.abs(eleGained);
                addWaypointElement(currentLat, currentLon, distance, eleCurrent, eleGained, currentTime, heart, totalDistance);
            }
            else {
                if(currentCalcEle != null && lastCalcEle != null) {
                    Double eleGained = currentCalcEle.doubleValue() - lastCalcEle.doubleValue();
                    addWaypointElement(currentLat, currentLon, distance, currentCalcEle.doubleValue(), eleGained, currentTime, heart, totalDistance);
                }  else
                    addWaypointElement(currentLat, currentLon, distance, 0.0, 0.0, currentTime, heart, totalDistance);
            }
            addCalculatedElevationElement(currentCalcEle, lastCalcEle);
        }
        else {
            if (eleCurrent != null)
                addWaypointElement(currentLat,currentLon,distance,eleCurrent,0.0,currentTime, heart, totalDistance);
            else {
                if(currentCalcEle != null && lastCalcEle != null) {
                    Double eleGained = currentCalcEle.doubleValue() - lastCalcEle.doubleValue();
                    addWaypointElement(currentLat, currentLon, distance, currentCalcEle.doubleValue(), eleGained, currentTime, heart, totalDistance);
                }  else
                    addWaypointElement(currentLat, currentLon, distance, 0.0, 0.0, currentTime, heart, totalDistance);
            }

        }
    }

    protected void addWaypointElement(
            double currentLat,
            double currentLon,
            double distance,
            Double eleCurrent,
            Double eleGained,
            Date currentTime,
            double heart,
            double totalDistance) {
        Waypoint waypoint =
                GPSUtility.createWaypointWrapper(
                        currentLat,
                        currentLon,
                        distance,
                        eleCurrent,
                        eleGained,
                        totalDistance,
                        heart,
                        currentTime);
        waypoints.add(waypoint);
    }

    protected void addCalculatedElevationElement(BigDecimal eleCalcCurrent, BigDecimal eleCalcLast) {
        if (eleCalcCurrent != null && eleCalcLast != null) {
            Double eleGained = eleCalcCurrent.doubleValue() - eleCalcLast.doubleValue();
            if (eleGained > 0)
                totalCalculatedElevation += eleGained;

            if (eleGained < 0)
                totalCalculatedDescent += Math.abs(eleGained);
        }
    }

    public abstract List<Track> extractTrack(String gpsFile) throws Exception;
}
