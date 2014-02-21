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
        //TODO extarct these values in a useful data structure
        double limitHighestPoint = 0;
        double limitLowestPoint = 1000000;


        Map<String, WaypointKm> mapKm = new LinkedHashMap();

        int kmTotalDistance = (int) totalDistance;
        int[] elements = new int[kmTotalDistance];
        for (int i = 0; i < kmTotalDistance; i++) {
            elements[i] = i;
        }
        int index = 1;
        int indexArray = 0;
        WaypointKm waypointKmLast = null;
        Waypoint waypointFirst = waypoints.get(0);
        WaypointKm waypointKmFirst = new WaypointKm();
        waypointKmFirst.setEleGained(waypointFirst.getEle());
        waypointKmFirst.setEle(waypointFirst.getEle());
        waypointKmFirst.setTimeSpent(waypointFirst.getDateRelevation());
        waypointKmFirst.setTimeIncrement(waypointFirst.getDateRelevation().getTime());
        for (Waypoint waypoint : waypoints) {
            String coordinate = waypoint.getLat() + "-" + waypoint.getLon();
            double newKm = waypoint.getDistanceFromStartingPoint();
            if (newKm > index) {
                WaypointKm waypointKm = new WaypointKm();
                waypointKm.setKm(index);
                if(waypointKmLast==null) {
                    waypointKm.setEleGained(waypoint.getEle()-waypointKmFirst.getEle());
                    waypointKm.setEle(waypoint.getEle());
                    waypointKm.setTimeSpent(waypoint.getDateRelevation());
                    waypointKm.setTimeIncrement(waypoint.getDateRelevation().getTime()-waypointKmFirst.getTimeSpent().getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(waypointKmFirst.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointKm.setAvgSpeed(waypoint.getDistanceFromStartingPoint()/TimeUtility.getTimeDiff(cal,cal2));
                }
                else {
                    waypointKm.setEleGained(waypoint.getEle()-waypointKmLast.getEle());
                    waypointKm.setEle(waypoint.getEle());
                    waypointKm.setTimeSpent(waypoint.getDateRelevation());
                    waypointKm.setTimeIncrement(waypoint.getDateRelevation().getTime()-waypointKmLast.getTimeSpent().getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(waypointKmLast.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointKm.setAvgSpeed((waypoint.getDistanceFromStartingPoint()-waypointKmLast.getKm())/TimeUtility.getTimeDiff(cal,cal2));
                }
                mapKm.put(coordinate, waypointKm);
                waypointKmLast = waypointKm;
                index++;
            }
            //last element
            else if(index>totalDistance && indexArray==waypoints.size()-1) {
                WaypointKm waypointKm = new WaypointKm();
                waypointKm.setKm(index);
                waypointKm.setEleGained(waypoint.getEle()-waypointKmLast.getEle());
                waypointKm.setEle(waypoint.getEle());
                waypointKm.setTimeSpent(waypoint.getDateRelevation());
                waypointKm.setTimeIncrement(waypoint.getDateRelevation().getTime()-waypointKmLast.getTimeSpent().getTime());
                Calendar cal = Calendar.getInstance();
                cal.setTime(waypointKmLast.getTimeSpent());
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(waypoint.getDateRelevation());
                waypointKm.setAvgSpeed((waypoint.getDistanceFromStartingPoint()-waypointKmLast.getKm())/TimeUtility.getTimeDiff(cal,cal2));
                mapKm.put(coordinate, waypointKm);
            }
            indexArray++;
        }
        return mapKm;
    }

    public static HashMap<String,WaypointKm> calculateStatsFromKm(Map<String, WaypointKm> waypoints) {
        HashMap<String,WaypointKm> mapResult = new HashMap(6);

        double limitFastestKm = 0;
        double limitSlowestKm = 1000;
        double limitLongestKm = 0;
        double limitShortestKm = 1000000000;
        double limitMostElevated = 0;
        double limitLessElevated = 1000000;

        for (Map.Entry<String, WaypointKm> entry : waypoints.entrySet()) {
            WaypointKm waypoint = entry.getValue();

            if(waypoint.getAvgSpeed()>limitFastestKm) {
                mapResult.put("Fastest",waypoint);
                limitFastestKm = waypoint.getAvgSpeed();
            }

            if(waypoint.getAvgSpeed()<limitSlowestKm) {
                mapResult.put("Slowest",waypoint);
                limitSlowestKm = waypoint.getAvgSpeed();
            }

            if(waypoint.getTimeIncrement()<limitShortestKm) {
                mapResult.put("Shortest",waypoint);
                limitShortestKm = waypoint.getTimeIncrement();
            }

            if(waypoint.getTimeIncrement()>limitLongestKm) {
                mapResult.put("Longest",waypoint);
                limitLongestKm = waypoint.getTimeIncrement();
            }

            if(waypoint.getEleGained()<limitLessElevated) {
                mapResult.put("Less Elevated",waypoint);
                limitLessElevated = waypoint.getEleGained();
            }

            if(waypoint.getEleGained()>limitMostElevated) {
                mapResult.put("Most Elevated",waypoint);
                limitMostElevated = waypoint.getEleGained();
            }
        }
        return mapResult;
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
                        slope.setDistance(gradientLast.getDistanceFromStartingPoint()-gradientFirst.getDistanceFromStartingPoint());
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
