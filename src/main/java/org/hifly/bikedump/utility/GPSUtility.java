package org.hifly.bikedump.utility;

import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.gps.WaypointSegment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GPSUtility {

    public static class GpsStats {
        private Map<String, WaypointSegment> waypointsKm;
        private double maxAltitude;
        private double minAltitude;
        private long climbingTime;
        private double climbingSpeed;
        private double climbingDistance;


        public Map<String, WaypointSegment> getWaypointsKm() {
            return waypointsKm;
        }

        public void setWaypointsKm(Map<String, WaypointSegment> waypointsKm) {
            this.waypointsKm = waypointsKm;
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

        public long getClimbingTime() {
            return climbingTime;
        }

        public void setClimbingTime(long climbingTime) {
            this.climbingTime = climbingTime;
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
    }

    public static double roundDoubleStat(double value) {
        BigDecimal bd;
        try {
            bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN);
        } catch (NumberFormatException nef) {
            bd = new BigDecimal(0);
        }
        value = bd.doubleValue();
        return value;

    }

    public static String getKeyForCoordinatesMap(String key) {
        String keyString = key;
        String[] splitter = keyString.split("-");
        String s1 = splitter[0];
        String s2 = splitter[1];
        int lenghtDec = s1.substring(s1.lastIndexOf('.')).length();
        int lenghtDec2 = s2.substring(s2.lastIndexOf('.')).length();
        if (lenghtDec < 7) {
            for (int i = lenghtDec; i < 7; i++)
                s1 = s1.concat("0");
        }
        if (lenghtDec2 < 7) {
            for (int i = lenghtDec2; i < 7; i++)
                s2 = s2.concat("0");
        }
        return s1 + "-" + s2;
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
            double totalDistance,
            double heart,
            Date time) {
        //create a gradient wrapper
        Waypoint wrapper = new Waypoint();
        wrapper.setDistance(distance);
        wrapper.setGradient(GPSUtility.gradientPercentage(gainInElevation, distance));
        wrapper.setDateRelevation(time);
        wrapper.setEle(elevation);
        wrapper.setLat(latitude);
        wrapper.setLon(longitude);
        wrapper.setDistanceFromStartingPoint(totalDistance);
        wrapper.setHeart(heart);
        return wrapper;
    }

    public static GPSUtility.GpsStats extractInfoFromWaypoints(List<Waypoint> waypoints, double totalDistance) {
        GPSUtility.GpsStats stats = new GpsStats();
        if (waypoints == null || waypoints.isEmpty())
            return stats;

        double limitHighestPoint = 0;
        double limitLowestPoint = 1000000;
        long climbingTime = 0;
        double climbingDistance = 0;
        double limitMinHeart = 10000000;
        double limitMaxHeart = 0;

        Map<String, WaypointSegment> mapKm = new LinkedHashMap();

        int kmTotalDistance = (int) totalDistance;
        int[] elements = new int[kmTotalDistance];
        for (int i = 0; i < kmTotalDistance; i++)
            elements[i] = i;
        int index = 1;
        int indexArray = 0;
        WaypointSegment waypointSegmentLast = null;
        Waypoint waypointFirst = waypoints.get(0);
        WaypointSegment waypointSegmentFirst = new WaypointSegment();
        waypointSegmentFirst.setEleGained(waypointFirst.getEle());
        waypointSegmentFirst.setEle(waypointFirst.getEle());
        waypointSegmentFirst.setTimeSpent(waypointFirst.getDateRelevation());
        if (waypointFirst.getDateRelevation() != null)
            waypointSegmentFirst.setTimeIncrement(waypointFirst.getDateRelevation().getTime());
        Waypoint first = null;
        for (Waypoint waypoint : waypoints) {
            double elevation = waypoint.getEle();
            if (first != null) {
                double eleGained = waypoint.getEle() - first.getEle();
                if (eleGained > 0) {
                    if (waypoint.getDateRelevation() != null)
                        climbingTime += (waypoint.getDateRelevation().getTime() - first.getDateRelevation().getTime());
                    climbingDistance +=
                            (waypoint.getDistanceFromStartingPoint() - first.getDistanceFromStartingPoint());
                }
            }
            if (elevation > limitHighestPoint)
                limitHighestPoint = elevation;
            if (elevation < limitLowestPoint)
                limitLowestPoint = elevation;
            if (waypoint.getHeart() < limitMinHeart)
                limitMinHeart = waypoint.getHeart();
            if (waypoint.getHeart() > limitMaxHeart)
                limitMaxHeart = waypoint.getHeart();
            String coordinate = waypoint.getLat() + "-" + waypoint.getLon();
            double newKm = waypoint.getDistanceFromStartingPoint();
            if (newKm > index) {
                WaypointSegment waypointSegment = new WaypointSegment();
                waypointSegment.setUnit(index);
                if (waypointSegmentLast == null) {
                    waypointSegment.setEleGained(waypoint.getEle() - waypointSegmentFirst.getEle());
                    waypointSegment.setEle(waypoint.getEle());
                    waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                    waypointSegment.setMinHeart(limitMinHeart);
                    waypointSegment.setMaxHeart(limitMaxHeart);
                    if (waypoint.getDateRelevation() != null) {
                        waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime() - waypointSegmentFirst.getTimeSpent().getTime());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(waypointSegmentFirst.getTimeSpent());
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(waypoint.getDateRelevation());
                        waypointSegment.setAvgSpeed(waypoint.getDistanceFromStartingPoint() / TimeUtility.getTimeDiffHour(cal, cal2));
                    }
                } else {
                    waypointSegment.setEleGained(waypoint.getEle() - waypointSegmentLast.getEle());
                    waypointSegment.setEle(waypoint.getEle());
                    waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                    waypointSegment.setMinHeart(limitMinHeart);
                    waypointSegment.setMaxHeart(limitMaxHeart);
                    if (waypoint.getDateRelevation() != null) {
                        waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime() - waypointSegmentLast.getTimeSpent().getTime());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(waypointSegmentLast.getTimeSpent());
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(waypoint.getDateRelevation());
                        waypointSegment.setAvgSpeed((waypoint.getDistanceFromStartingPoint() - waypointSegmentLast.getUnit()) / TimeUtility.getTimeDiffHour(cal, cal2));
                    }
                }
                limitMaxHeart = 0;
                limitMinHeart = 10000000;
                mapKm.put(coordinate, waypointSegment);
                waypointSegmentLast = waypointSegment;
                index++;
            }
            //last element
            else if (index > totalDistance && indexArray == waypoints.size() - 1) {
                WaypointSegment waypointSegment = new WaypointSegment();
                waypointSegment.setUnit(index);
                waypointSegment.setEleGained(waypoint.getEle() - waypointSegmentLast.getEle());
                waypointSegment.setEle(waypoint.getEle());
                waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                waypointSegment.setMinHeart(limitMinHeart);
                waypointSegment.setMaxHeart(limitMaxHeart);
                if (waypoint.getDateRelevation() != null) {
                    waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime() - waypointSegmentLast.getTimeSpent().getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(waypointSegmentLast.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointSegment.setAvgSpeed((waypoint.getDistanceFromStartingPoint() - waypointSegmentLast.getUnit()) / TimeUtility.getTimeDiffHour(cal, cal2));
                }
                mapKm.put(coordinate, waypointSegment);
            }
            indexArray++;

            first = waypoint;
        }

        stats.setWaypointsKm(mapKm);
        stats.setMaxAltitude(limitHighestPoint);
        stats.setMinAltitude(limitLowestPoint);
        stats.setClimbingTime(climbingTime);
        stats.setClimbingSpeed(climbingDistance / (climbingTime / (60 * 60 * 1000)));
        stats.setClimbingDistance(climbingDistance);

        return stats;
    }

    public static HashMap<String, WaypointSegment> calculateStatsInUnit(Map<String, WaypointSegment> waypoints) {
        HashMap<String, WaypointSegment> mapResult = new HashMap(6);

        double limitFastestKm = 0;
        double limitSlowestKm = 1000;
        double limitLongestKm = 0;
        double limitShortestKm = 1000000000;
        double limitMostElevated = 0;
        double limitLessElevated = 1000000;


        for (Map.Entry<String, WaypointSegment> entry : waypoints.entrySet()) {
            WaypointSegment waypoint = entry.getValue();

            if (waypoint.getAvgSpeed() > limitFastestKm) {
                mapResult.put("Fastest", waypoint);
                limitFastestKm = waypoint.getAvgSpeed();
            }

            if (waypoint.getAvgSpeed() < limitSlowestKm) {
                mapResult.put("Slowest", waypoint);
                limitSlowestKm = waypoint.getAvgSpeed();
            }

            if (waypoint.getTimeIncrement() < limitShortestKm) {
                mapResult.put("Shortest", waypoint);
                limitShortestKm = waypoint.getTimeIncrement();
            }

            if (waypoint.getTimeIncrement() > limitLongestKm) {
                mapResult.put("Longest", waypoint);
                limitLongestKm = waypoint.getTimeIncrement();
            }

            if (waypoint.getEleGained() < limitLessElevated) {
                mapResult.put("Less Elevated", waypoint);
                limitLessElevated = waypoint.getEleGained();
            }

            if (waypoint.getEleGained() > limitMostElevated) {
                mapResult.put("Most Elevated", waypoint);
                limitMostElevated = waypoint.getEleGained();
            }

        }
        return mapResult;
    }

}
