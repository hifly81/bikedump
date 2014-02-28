package org.hifly.geomapviewer.utility;

import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.WaypointSegment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author
 * @date 02/02/14
 */
public class GpsUtility {

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
        BigDecimal bd = null;
        try {
            bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN);
        }
        catch(NumberFormatException nef) {
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

    public static GpsUtility.GpsStats extractInfoFromWaypoints(List<Waypoint> waypoints, double totalDistance) {
        GpsUtility.GpsStats stats = new GpsStats();

        double limitHighestPoint = 0;
        double limitLowestPoint = 1000000;

        long climbingTime = 0;
        double climbingDistance = 0;


        Map<String, WaypointSegment> mapKm = new LinkedHashMap();

        int kmTotalDistance = (int) totalDistance;
        int[] elements = new int[kmTotalDistance];
        for (int i = 0; i < kmTotalDistance; i++) {
            elements[i] = i;
        }
        int index = 1;
        int indexArray = 0;
        WaypointSegment waypointSegmentLast = null;
        Waypoint waypointFirst = waypoints.get(0);
        WaypointSegment waypointSegmentFirst = new WaypointSegment();
        waypointSegmentFirst.setEleGained(waypointFirst.getEle());
        waypointSegmentFirst.setEle(waypointFirst.getEle());
        waypointSegmentFirst.setTimeSpent(waypointFirst.getDateRelevation());
        if(waypointFirst.getDateRelevation()!=null) {
            waypointSegmentFirst.setTimeIncrement(waypointFirst.getDateRelevation().getTime());
        }
        Waypoint first = null;
        for (Waypoint waypoint : waypoints) {
            double elevation = waypoint.getEle();
            if(first!=null) {
                double eleGained = waypoint.getEle() - first.getEle();
                if(eleGained>0) {
                    if(waypoint.getDateRelevation()!=null) {
                        climbingTime+= (waypoint.getDateRelevation().getTime()-first.getDateRelevation().getTime());
                    }
                    climbingDistance+=
                            (waypoint.getDistanceFromStartingPoint()-first.getDistanceFromStartingPoint());
                }
            }
            if(elevation > limitHighestPoint) {
                limitHighestPoint = elevation;
            }
            if(elevation < limitLowestPoint) {
                limitLowestPoint = elevation;
            }
            String coordinate = waypoint.getLat() + "-" + waypoint.getLon();
            double newKm = waypoint.getDistanceFromStartingPoint();
            if (newKm > index) {
                WaypointSegment waypointSegment = new WaypointSegment();
                waypointSegment.setKm(index);
                if(waypointSegmentLast ==null) {
                    waypointSegment.setEleGained(waypoint.getEle()- waypointSegmentFirst.getEle());
                    waypointSegment.setEle(waypoint.getEle());
                    waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                    if(waypoint.getDateRelevation()!=null) {
                        waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime()- waypointSegmentFirst.getTimeSpent().getTime());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(waypointSegmentFirst.getTimeSpent());
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(waypoint.getDateRelevation());
                        waypointSegment.setAvgSpeed(waypoint.getDistanceFromStartingPoint()/TimeUtility.getTimeDiff(cal,cal2));
                    }
                }
                else {
                    waypointSegment.setEleGained(waypoint.getEle()- waypointSegmentLast.getEle());
                    waypointSegment.setEle(waypoint.getEle());
                    waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                    if(waypoint.getDateRelevation()!=null) {
                        waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime()- waypointSegmentLast.getTimeSpent().getTime());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(waypointSegmentLast.getTimeSpent());
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(waypoint.getDateRelevation());
                        waypointSegment.setAvgSpeed((waypoint.getDistanceFromStartingPoint()- waypointSegmentLast.getKm())/TimeUtility.getTimeDiff(cal,cal2));
                    }
                }
                mapKm.put(coordinate, waypointSegment);
                waypointSegmentLast = waypointSegment;
                index++;
            }
            //last element
            else if(index>totalDistance && indexArray==waypoints.size()-1) {
                WaypointSegment waypointSegment = new WaypointSegment();
                waypointSegment.setKm(index);
                waypointSegment.setEleGained(waypoint.getEle()- waypointSegmentLast.getEle());
                waypointSegment.setEle(waypoint.getEle());
                waypointSegment.setTimeSpent(waypoint.getDateRelevation());
                if(waypoint.getDateRelevation()!=null) {
                    waypointSegment.setTimeIncrement(waypoint.getDateRelevation().getTime()- waypointSegmentLast.getTimeSpent().getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(waypointSegmentLast.getTimeSpent());
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(waypoint.getDateRelevation());
                    waypointSegment.setAvgSpeed((waypoint.getDistanceFromStartingPoint()- waypointSegmentLast.getKm())/TimeUtility.getTimeDiff(cal,cal2));
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
        stats.setClimbingSpeed(climbingDistance/(climbingTime / (60 * 60 * 1000)));
        stats.setClimbingDistance(climbingDistance);

        return stats;
    }

    public static HashMap<String,WaypointSegment> calculateStatsFromKm(Map<String, WaypointSegment> waypoints) {
        HashMap<String,WaypointSegment> mapResult = new HashMap(6);

        double limitFastestKm = 0;
        double limitSlowestKm = 1000;
        double limitLongestKm = 0;
        double limitShortestKm = 1000000000;
        double limitMostElevated = 0;
        double limitLessElevated = 1000000;

        for (Map.Entry<String, WaypointSegment> entry : waypoints.entrySet()) {
            WaypointSegment waypoint = entry.getValue();

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

    public static List<SlopeSegment> extractSlope(List<Waypoint> waypoints, ProfileSetting profile) {
        List<SlopeSegment> list = new ArrayList();
        double accumulatedDistance = 0;
        double descentDistance = 0;
        double limitDescent = 1.5;
        double limitElevation = 1.5;
        double gradientUp = 2;
        Waypoint gradientFirst = null;
        Waypoint gradientLast = null;

        int waypointStartIndex = 0;
        int waypointEndIndex = 0;

        for (int i=0; i<waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);

            if (waypoint.getGradient() > 0) {
                if (accumulatedDistance == 0) {
                    gradientFirst = waypoint;
                    waypointStartIndex = i;
                }
                else {
                    if (descentDistance < limitDescent) {
                        if (accumulatedDistance < descentDistance) {
                            gradientFirst = waypoint;
                            waypointStartIndex = i;
                        }
                        else {
                            gradientLast = waypoint;
                            waypointEndIndex = i;
                        }
                    }
                    else {
                        if (accumulatedDistance < descentDistance) {
                            gradientFirst = waypoint;
                            waypointStartIndex = i;
                        }
                    }
                }
                accumulatedDistance += waypoint.getDistance();

            }
            else {
                descentDistance += waypoint.getDistance();
                if (descentDistance >= limitDescent && accumulatedDistance < limitElevation) {
                    accumulatedDistance = 0;
                    descentDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
                if (descentDistance >= limitDescent && accumulatedDistance >= limitElevation) {
                    //check if a slope can be combined
                    boolean combined = false;
                    if(list!=null && !list.isEmpty()) {
                        SlopeSegment lastSlope = list.get(list.size()-1);
                        double lastSlopeEndDistance = lastSlope.getEndDistance();
                        if((gradientFirst.getDistanceFromStartingPoint()-lastSlopeEndDistance) < 1) {
                            //fix lastSlope
                            combined = true;
                            lastSlope.setElevation(lastSlope.getElevation()+(gradientLast.getEle() - gradientFirst.getEle()));
                            lastSlope.setDistance(lastSlope.getDistance()+(gradientLast.getDistanceFromStartingPoint()-gradientFirst.getDistanceFromStartingPoint()));
                            lastSlope.setGradient(GpsUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                            lastSlope.setEndLatitude(gradientLast.getLat());
                            lastSlope.setEndLongitude(gradientLast.getLon());
                            lastSlope.setEndDate(gradientLast.getDateRelevation());
                            lastSlope.setEndElevation(gradientLast.getEle());
                            lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                            //calculate waypoint for slope
                            List<Waypoint> waypointsTemp = new ArrayList();
                            for(int z=waypointStartIndex;z<waypointEndIndex;z++) {
                                waypointsTemp.add(waypoints.get(z));
                            }
                            //in case of combined a list of waypoints is already present
                            List<Waypoint> currentWaypoints = lastSlope.getWaypoints();
                            currentWaypoints.addAll(waypointsTemp);
                            lastSlope.setWaypoints(currentWaypoints);

                            Calendar calLast = Calendar.getInstance();
                            if(gradientLast.getDateRelevation()!=null) {
                                calLast.setTime(gradientLast.getDateRelevation());
                            }
                            Calendar calFirst = Calendar.getInstance();
                            if(lastSlope.getStartDate()!=null) {
                                calFirst.setTime(lastSlope.getStartDate());
                            }
                            double timeDiffInHour = TimeUtility.getTimeDiff(calLast, calFirst);
                            double speed = Math.abs(lastSlope.getDistance() / timeDiffInHour);
                            lastSlope.setAvgSpeed(speed);

                            double power = PowerUtility.calculatePower(
                                    profile.getWeight(),
                                    profile.getBikeWeight(),
                                    lastSlope.getDistance(),
                                    lastSlope.getGradient(),
                                    lastSlope.getAvgSpeed()
                            );

                            lastSlope.setPower(power);

                        }
                    }
                    if(!combined) {
                        createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profile);
                    }
                    accumulatedDistance = 0;
                    descentDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
            }
        }

        //last segment
        if(accumulatedDistance >= limitElevation)  {
            boolean combined = false;
            if(list!=null && !list.isEmpty()) {
                SlopeSegment lastSlope = list.get(list.size()-1);
                double lastSlopeEndDistance = lastSlope.getEndDistance();
                if((gradientFirst.getDistanceFromStartingPoint()-lastSlopeEndDistance) < 1) {
                    //fix lastSlope
                    combined = true;
                    lastSlope.setElevation(lastSlope.getElevation()+(gradientLast.getEle() - gradientFirst.getEle()));
                    lastSlope.setDistance(lastSlope.getDistance()+(gradientLast.getDistanceFromStartingPoint()-gradientFirst.getDistanceFromStartingPoint()));
                    lastSlope.setGradient(GpsUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                    lastSlope.setEndLatitude(gradientLast.getLat());
                    lastSlope.setEndLongitude(gradientLast.getLon());
                    lastSlope.setEndDate(gradientLast.getDateRelevation());
                    lastSlope.setEndElevation(gradientLast.getEle());
                    lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                    //calculate waypoint for slope
                    List<Waypoint> waypointsTemp = new ArrayList();
                    for(int z=waypointStartIndex;z<waypointEndIndex;z++) {
                        waypointsTemp.add(waypoints.get(z));
                    }
                    //in case of combined a list of waypoints is already present
                    List<Waypoint> currentWaypoints = lastSlope.getWaypoints();
                    currentWaypoints.addAll(waypointsTemp);
                    lastSlope.setWaypoints(currentWaypoints);

                    Calendar calLast = Calendar.getInstance();
                    calLast.setTime(gradientLast.getDateRelevation());
                    Calendar calFirst = Calendar.getInstance();
                    calFirst.setTime(lastSlope.getStartDate());
                    double timeDiffInHour = TimeUtility.getTimeDiff(calLast, calFirst);
                    double speed = Math.abs(lastSlope.getDistance() / timeDiffInHour);

                    lastSlope.setAvgSpeed(speed);

                    double power = PowerUtility.calculatePower(
                            profile.getWeight(),
                            profile.getBikeWeight(),
                            lastSlope.getDistance(),
                            lastSlope.getGradient(),
                            lastSlope.getAvgSpeed()
                    );

                    lastSlope.setPower(power);

                }
            }
            if(!combined) {
                createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profile);
            }

        }

        return list;
    }

    private static void createSlope(
            List<SlopeSegment> list,
            double accDistance,
            double gradientUp,
            Waypoint gradientFirst,
            Waypoint gradientLast,
            List<Waypoint> waypoints,
            int waypointStartIndex,
            int waypointEndIndex,
            ProfileSetting profile) {
        double gradient = ((gradientLast.getEle() - gradientFirst.getEle()) / (accDistance * 1000)) * 100;
        if (gradient >= gradientUp) {
            SlopeSegment slope = new SlopeSegment();
            double elevation = gradientLast.getEle() - gradientFirst.getEle();
            Calendar calLast = Calendar.getInstance();
            if(gradientLast.getDateRelevation()!=null) {
                calLast.setTime(gradientLast.getDateRelevation());
            }
            Calendar calFirst = Calendar.getInstance();
            if(gradientFirst.getDateRelevation()!=null) {
                calFirst.setTime(gradientFirst.getDateRelevation());
            }
            double timeDiffInHour = TimeUtility.getTimeDiff(calLast, calFirst);
            double speed = Math.abs(accDistance / timeDiffInHour);
            slope.setElevation(elevation);
            slope.setDistance(gradientLast.getDistanceFromStartingPoint() - gradientFirst.getDistanceFromStartingPoint());
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

            double power = PowerUtility.calculatePower(
                    profile.getWeight(),
                    profile.getBikeWeight(),
                    slope.getDistance(),
                    slope.getGradient(),
                    slope.getAvgSpeed()
            );

            slope.setPower(power);

            //calculate waypoint for slope
            List<Waypoint> waypointsTemp = new ArrayList();
            for(int z=waypointStartIndex;z<waypointEndIndex;z++) {
                waypointsTemp.add(waypoints.get(z));
            }
            slope.setWaypoints(waypointsTemp);
            list.add(slope);
        }
    }
}
