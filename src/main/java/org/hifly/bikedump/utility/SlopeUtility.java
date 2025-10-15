package org.hifly.bikedump.utility;

import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.gps.Waypoint;

import java.util.*;

public class SlopeUtility {

    public static Map<String,List<SlopeSegment>> organizeSlopesBySteepness(List<SlopeSegment> slopes) {
        if(slopes!=null && !slopes.isEmpty()) {
            Map<String,List<SlopeSegment>> map = new HashMap<>(5);
            List<SlopeSegment> slopes1 = new ArrayList<>();
            List<SlopeSegment> slopes2 = new ArrayList<>();
            List<SlopeSegment> slopes3 = new ArrayList<>();
            List<SlopeSegment> slopes4 = new ArrayList<>();
            List<SlopeSegment> slopes5 = new ArrayList<>();

            for(SlopeSegment slope: slopes) {
                double gradient = slope.getGradient();
                if(gradient <= 4)
                    slopes1.add(slope);
                else if(gradient > 4 && gradient <= 8)
                    slopes2.add(slope);
                else if(gradient > 4 && gradient <= 8)
                    slopes3.add(slope);
                else if(gradient > 4 && gradient <=8)
                    slopes4.add(slope);
                else
                    slopes5.add(slope);

                map.put("0% - 4%",slopes1);
                map.put("4% - 8%",slopes2);
                map.put("8% - 10%",slopes3);
                map.put("10% - 15%",slopes4);
                map.put(">15%",slopes5);

            }

            return map;
        }
        else {
            return null;
        }
    }

    public static SlopeSegment totalAltimetricProfile(List<Waypoint> waypoints) {
        SlopeSegment slopeSegment = new SlopeSegment();
        
        if (waypoints == null || waypoints.isEmpty()) {
            return slopeSegment;
        }
        
        slopeSegment.setWaypoints(waypoints);
        
        // Get first and last waypoints
        Waypoint first = waypoints.get(0);
        Waypoint last = waypoints.get(waypoints.size() - 1);
        
        // Calculate total elevation gain/loss
        double totalElevationGain = 0;
        double totalElevationLoss = 0;
        Waypoint previous = null;
        
        for (Waypoint waypoint : waypoints) {
            if (previous != null) {
                double elevationDiff = waypoint.getEle() - previous.getEle();
                if (elevationDiff > 0) {
                    totalElevationGain += elevationDiff;
                } else {
                    totalElevationLoss += Math.abs(elevationDiff);
                }
            }
            previous = waypoint;
        }
        
        // Set basic properties
        slopeSegment.setStartLatitude(first.getLat());
        slopeSegment.setStartLongitude(first.getLon());
        slopeSegment.setEndLatitude(last.getLat());
        slopeSegment.setEndLongitude(last.getLon());
        slopeSegment.setStartElevation(first.getEle());
        slopeSegment.setEndElevation(last.getEle());
        slopeSegment.setStartDistance(first.getDistanceFromStartingPoint());
        slopeSegment.setEndDistance(last.getDistanceFromStartingPoint());
        
        // Calculate total distance
        double totalDistance = last.getDistanceFromStartingPoint() - first.getDistanceFromStartingPoint();
        slopeSegment.setDistance(totalDistance);
        
        // Set elevation (net elevation change)
        double netElevation = last.getEle() - first.getEle();
        slopeSegment.setElevation(netElevation);
        
        // Calculate average gradient
        if (totalDistance > 0) {
            double gradient = GPSUtility.gradientPercentage(netElevation, totalDistance);
            slopeSegment.setGradient(gradient);
        }
        
        // Set dates and calculate time-based statistics
        slopeSegment.setStartDate(first.getDateRelevation());
        slopeSegment.setEndDate(last.getDateRelevation());
        
        if (first.getDateRelevation() != null && last.getDateRelevation() != null) {
            Calendar calFirst = Calendar.getInstance();
            calFirst.setTime(first.getDateRelevation());
            Calendar calLast = Calendar.getInstance();
            calLast.setTime(last.getDateRelevation());
            
            double timeDiffInHour = TimeUtility.getTimeDiffHour(calLast, calFirst);
            if (timeDiffInHour > 0) {
                double avgSpeed = Math.abs(totalDistance / timeDiffInHour);
                slopeSegment.setAvgSpeed(avgSpeed);
                
                // Calculate VAM (Vertical Ascent Meters per hour)
                double timeDiffInSeconds = TimeUtility.getTimeDiffSecond(calLast, calFirst);
                if (timeDiffInSeconds > 0) {
                    double vam = (totalElevationGain / timeDiffInSeconds) * 3600;
                    slopeSegment.setVam(vam);
                }
            }
        }
        
        return slopeSegment;
    }

    //TODO review slope ranges
    public static List<SlopeSegment> extractSlope(List<Waypoint> waypoints) {
        List<SlopeSegment> list = new ArrayList<>();
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
                        } else {
                            gradientLast = waypoint;
                            waypointEndIndex = i;
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
                            lastSlope.setGradient(GPSUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                            lastSlope.setEndLatitude(gradientLast.getLat());
                            lastSlope.setEndLongitude(gradientLast.getLon());
                            lastSlope.setEndDate(gradientLast.getDateRelevation());
                            lastSlope.setEndElevation(gradientLast.getEle());
                            lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                            //calculate waypoint for slope
                            List<Waypoint> waypointsTemp = new ArrayList<>();
                            for(int z = waypointStartIndex; z < waypointEndIndex; z++)
                                waypointsTemp.add(waypoints.get(z));
                            //in case of combined a list of waypoints is already present
                            List<Waypoint> currentWaypoints = lastSlope.getWaypoints();
                            currentWaypoints.addAll(waypointsTemp);
                            lastSlope.setWaypoints(currentWaypoints);

                            Calendar calLast = Calendar.getInstance();
                            if(gradientLast.getDateRelevation() != null)
                                calLast.setTime(gradientLast.getDateRelevation());
                            Calendar calFirst = Calendar.getInstance();
                            if(lastSlope.getStartDate() != null)
                                calFirst.setTime(lastSlope.getStartDate());
                            double timeDiffInHour = TimeUtility.getTimeDiffHour(calLast, calFirst);
                            double speed = Math.abs(lastSlope.getDistance() / timeDiffInHour);
                            lastSlope.setAvgSpeed(speed);

                            double power = 0.0;
                            lastSlope.setPower(power);
                            lastSlope.setVam((lastSlope.getElevation()/TimeUtility.getTimeDiffSecond(calLast, calFirst))*3600);

                        }
                    }
                    if(!combined)
                        createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex);
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
            if(list != null && !list.isEmpty()) {
                SlopeSegment lastSlope = list.get(list.size()-1);
                double lastSlopeEndDistance = lastSlope.getEndDistance();
                if((gradientFirst.getDistanceFromStartingPoint() - lastSlopeEndDistance) < 1) {
                    //fix lastSlope
                    combined = true;
                    lastSlope.setElevation(lastSlope.getElevation()+(gradientLast.getEle() - gradientFirst.getEle()));
                    lastSlope.setDistance(lastSlope.getDistance()+(gradientLast.getDistanceFromStartingPoint()-gradientFirst.getDistanceFromStartingPoint()));
                    lastSlope.setGradient(GPSUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                    lastSlope.setEndLatitude(gradientLast.getLat());
                    lastSlope.setEndLongitude(gradientLast.getLon());
                    lastSlope.setEndDate(gradientLast.getDateRelevation());
                    lastSlope.setEndElevation(gradientLast.getEle());
                    lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                    //calculate waypoint for slope
                    List<Waypoint> waypointsTemp = new ArrayList<>();
                    for(int z = waypointStartIndex; z < waypointEndIndex; z++)
                        waypointsTemp.add(waypoints.get(z));
                    //in case of combined a list of waypoints is already present
                    List<Waypoint> currentWaypoints = lastSlope.getWaypoints();
                    currentWaypoints.addAll(waypointsTemp);
                    lastSlope.setWaypoints(currentWaypoints);

                    Calendar calLast = Calendar.getInstance();
                    calLast.setTime(gradientLast.getDateRelevation());
                    Calendar calFirst = Calendar.getInstance();
                    calFirst.setTime(lastSlope.getStartDate());
                    double timeDiffInHour = TimeUtility.getTimeDiffHour(calLast, calFirst);
                    double speed = Math.abs(lastSlope.getDistance() / timeDiffInHour);

                    lastSlope.setAvgSpeed(speed);

                    double power = 0.0;
                    lastSlope.setPower(power);
                    lastSlope.setVam((lastSlope.getElevation()/TimeUtility.getTimeDiffSecond(calLast, calFirst))*3600);

                }
            }
            if(!combined)
                createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex);
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
            int waypointEndIndex) {

        try {
            double gradient = ((gradientLast.getEle() - gradientFirst.getEle()) / (accDistance * 1000)) * 100;
            if (gradient >= gradientUp) {
                SlopeSegment slope = new SlopeSegment();
                double elevation = gradientLast.getEle() - gradientFirst.getEle();
                Calendar calLast = Calendar.getInstance();
                if (gradientLast.getDateRelevation() != null)
                    calLast.setTime(gradientLast.getDateRelevation());
                Calendar calFirst = Calendar.getInstance();
                if (gradientFirst.getDateRelevation() != null)
                    calFirst.setTime(gradientFirst.getDateRelevation());
                double timeDiffInHour = TimeUtility.getTimeDiffHour(calLast, calFirst);
                double speed = Math.abs(accDistance / timeDiffInHour);
                slope.setElevation(elevation);
                slope.setDistance(gradientLast.getDistanceFromStartingPoint() - gradientFirst.getDistanceFromStartingPoint());
                slope.setGradient(GPSUtility.gradientPercentage(elevation, accDistance));
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

                double power = 0.0;
                slope.setPower(power);
                slope.setVam((slope.getElevation() / TimeUtility.getTimeDiffSecond(calLast, calFirst)) * 3600);

                //calculate waypoint for slope
                List<Waypoint> waypointsTemp = new ArrayList<>();
                for (int z = waypointStartIndex; z < waypointEndIndex; z++)
                    waypointsTemp.add(waypoints.get(z));
                slope.setWaypoints(waypointsTemp);
                list.add(slope);
            }
        }
        catch (Exception ex) {
            //TODO define exception
        }
    }
}
