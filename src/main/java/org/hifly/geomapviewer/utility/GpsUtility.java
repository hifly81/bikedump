package org.hifly.geomapviewer.utility;

import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointKm;

import java.util.*;

/**
 * @author
 * @date 02/02/14
 */
public class GpsUtility {

    public static String getKeyForCoordinatesMap(String key) {
        String keyString = key;
        String[] splitter = keyString.split("-");
        String s1 = splitter[0];
        String s2 = splitter[1];
        int lenghtDec = s1.substring(s1.lastIndexOf('.')).length();
        int lenghtDec2 = s2.substring(s2.lastIndexOf('.')).length();
        if(lenghtDec<7) {
            for(int i=lenghtDec;i<7;i++) {
                s1 = s1.concat("0");
            }
        }
        if(lenghtDec2<7) {
            for(int i=lenghtDec2;i<7;i++) {
                s2 = s2.concat("0");
            }
        }
        return s1+"-"+s2;
    }

    public static double haversine(
            double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }

    public static double gradientPercentage(double elevation, double distance) {
        return (elevation / (distance * 1000)) * 100;
    }

    public static Waypoint createWaypointWrapper(
            double latitude,
            double longitude,
            double distance,
            double elevation,
            double gainInElevation,
            double distanceFromStartingPoint,
            Date time) {
        //create a gradient wrapper
        Waypoint wrapper = new Waypoint();
        wrapper.setDistance(distance);
        wrapper.setGradient(GpsUtility.gradientPercentage(gainInElevation, distance));
        wrapper.setDateRelevation(time);
        wrapper.setEle(elevation);
        wrapper.setLat(latitude);
        wrapper.setLon(longitude);
        wrapper.setDistanceFromStartingPoint(distanceFromStartingPoint);
        return wrapper;
    }

    public static Map<String, WaypointKm> extractInfoFromWaypoints(List<Waypoint> waypoints, double totalDistance) {
        Map<String, WaypointKm> mapKm = new HashMap();

        int kmTotalDistance = (int) totalDistance;
        int[] elements = new int[kmTotalDistance];
        for (int i = 0; i < kmTotalDistance; i++) {
            elements[i] = i;
        }
        int index = 0;
        WaypointKm waypointKmLast = null;
        Waypoint waypointFirst = waypoints.get(0);
        WaypointKm waypointKmFirst = new WaypointKm();
        waypointKmFirst.setEleGained(waypointFirst.getEle());
        waypointKmFirst.setTimeSpent(waypointFirst.getDateRelevation().getTime());
        for (Waypoint waypoint : waypoints) {
            String coordinate = waypoint.getLat() + "-" + waypoint.getLon();
            double newKm = waypoint.getDistanceFromStartingPoint();
            if (newKm > index) {
                WaypointKm waypointKm = new WaypointKm();
                waypointKm.setKm(index);
                if(waypointKmLast==null) {
                    waypointKm.setEleGained(waypoint.getEle()-waypointKmFirst.getEleGained());
                    waypointKm.setTimeSpent(waypoint.getDateRelevation().getTime()-waypointKmFirst.getTimeSpent());
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(waypointKmFirst.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointKm.setAvgSpeed(waypoint.getDistanceFromStartingPoint()/TimeUtility.getTimeDiff(cal,cal2));
                }
                else {
                    waypointKm.setEleGained(waypoint.getEle()-waypointKmLast.getEleGained());
                    waypointKm.setTimeSpent(waypoint.getDateRelevation().getTime()-waypointKmLast.getTimeSpent());
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(waypointKmLast.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointKm.setAvgSpeed(waypoint.getDistanceFromStartingPoint()/TimeUtility.getTimeDiff(cal,cal2));
                }
                mapKm.put(coordinate, waypointKm);
                waypointKmLast = waypointKm;
                index++;
            }
        }
        return mapKm;
    }

    public static List<SlopeSegment> extractSlope(List<Waypoint> waypoints) {
        List<SlopeSegment> list = new ArrayList();
        double accDistance = 0;
        double decDistance = 0;
        double limitDec = 2;
        double limitUp = 2;
        double gradientUp = 1.5;
        Waypoint gradientFirst = null;
        Waypoint gradientLast = null;


        for (Waypoint w : waypoints) {

            if (w.getGradient() > 0) {
                if (accDistance == 0) {
                    gradientFirst = w;
                } else {
                    if (decDistance < limitDec) {
                        if (accDistance < decDistance) {
                            gradientFirst = w;
                        } else {
                            gradientLast = w;
                        }
                    } else {
                        if (accDistance < decDistance) {
                            gradientFirst = w;
                        }
                    }
                }
                accDistance += w.getDistance();

            } else {
                decDistance += w.getDistance();
                if (decDistance >= limitDec && accDistance < limitUp) {
                    accDistance = 0;
                    decDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
                if (decDistance >= limitDec && accDistance >= limitUp) {
                    double gradient = ((gradientLast.getEle() - gradientFirst.getEle()) / (accDistance * 1000)) * 100;
                    if (gradient >= gradientUp) {
                        SlopeSegment slope = new SlopeSegment();
                        double elevation = gradientLast.getEle() - gradientFirst.getEle();
                        Calendar calLast = Calendar.getInstance();
                        calLast.setTime(gradientLast.getDateRelevation());
                        Calendar calFirst = Calendar.getInstance();
                        calFirst.setTime(gradientFirst.getDateRelevation());
                        double timeDiffInHour = TimeUtility.getTimeDiff(calLast, calFirst);
                        double speed = Math.abs(accDistance / timeDiffInHour);
                        slope.setElevation(elevation);
                        slope.setDistance(accDistance);
                        slope.setGradient(GpsUtility.gradientPercentage(elevation, accDistance));
                        slope.setEndLatitude(gradientLast.getLat());
                        slope.setEndLongitude(gradientLast.getLon());
                        slope.setStartLatitude(gradientFirst.getLat());
                        slope.setStartLongitude(gradientFirst.getLon());
                        slope.setStartDate(gradientFirst.getDateRelevation());
                        slope.setEndDate(gradientLast.getDateRelevation());
                        slope.setStartElevation(gradientFirst.getEle());
                        slope.setEndElevation(gradientLast.getEle());
                        slope.setStartDistance(gradientFirst.getDistanceFromStartingPoint());
                        slope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                        slope.setAvgSpeed(speed);
                        list.add(slope);
                    }
                    accDistance = 0;
                    decDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
            }
        }
        return list;
    }
}
