package org.hifly.bikedump.utility;

import org.hifly.bikedump.domain.Bike;
import org.hifly.bikedump.domain.Profile;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.gps.Waypoint;

import java.util.*;

public class SlopeUtility {

    private static List<SlopeSegment> newSlopes;

    public static Map<String,List<SlopeSegment>> organizeSlopesBySteepness(List<SlopeSegment> slopes) {
        if(slopes!=null && !slopes.isEmpty()) {
            Map<String,List<SlopeSegment>> map = new HashMap(5);
            List<SlopeSegment> slopes1 = new ArrayList();
            List<SlopeSegment> slopes2 = new ArrayList();
            List<SlopeSegment> slopes3 = new ArrayList();
            List<SlopeSegment> slopes4 = new ArrayList();
            List<SlopeSegment> slopes5 = new ArrayList();

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

    //FIXME too intensive, similar to createSlopes
    public static SlopeSegment totalAltimetricProfile(List<Waypoint> waypoints, ProfileSetting profileSetting) {

        final double limitDescent = 1.5;
        final double limitElevation = 1.5;
        final double limitMinElevationForShortSlopes = 0.2;
        final double gradientUp = 3;

        Profile profile = profileSetting.getSelectedProfile();
        List<SlopeSegment> list = new ArrayList();
        double accumulatedSlopeDistance = 0;
        double accumulatedSlopeElevation = 0;
        double descentDistance = 0;
        Waypoint gradientFirst = null;
        Waypoint gradientLast = null;

        int waypointStartIndex = 0;
        int waypointEndIndex = 0;

        //TODO remove bike constants
        double bikeWeight = 9;
        List<Bike> bikes = profileSetting.getBikes();
        if(bikes != null && !bikes.isEmpty()) {
            for(Bike bike:bikes) {
                if(bike.isSelected()) {
                    bikeWeight = bike.getBikeWeight();
                    break;
                }
            }
        }

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);

            if (waypoint.getGradient() > 0) {
                if (accumulatedSlopeDistance == 0) {
                    gradientFirst = waypoint;
                    waypointStartIndex = i;
                }
                else {
                    gradientLast = waypoint;
                    waypointEndIndex = i;
                }
                accumulatedSlopeDistance += waypoint.getDistance();
                accumulatedSlopeElevation += waypoint.getEle();

            }
            else {
                descentDistance += waypoint.getDistance();

                boolean shortSlopeCandidade = accumulatedSlopeDistance < limitDescent && accumulatedSlopeDistance > limitMinElevationForShortSlopes &&
                        GPSUtility.gradientPercentage(accumulatedSlopeElevation, accumulatedSlopeDistance) > gradientUp;

                if (descentDistance >= limitDescent && accumulatedSlopeDistance < limitElevation && !shortSlopeCandidade) {
                    accumulatedSlopeDistance = 0;
                    accumulatedSlopeElevation = 0;
                    descentDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
                if (descentDistance >= limitDescent && (accumulatedSlopeDistance >= limitElevation || shortSlopeCandidade)) {
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
                            List<Waypoint> waypointsTemp = new ArrayList();
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

                            double power = PowerUtility.calculatePower(
                                    profile == null? 0: profile.getWeight(),
                                    bikeWeight,
                                    lastSlope.getDistance(),
                                    lastSlope.getGradient(),
                                    lastSlope.getAvgSpeed()
                            );

                            lastSlope.setPower(power);
                            lastSlope.setVam((lastSlope.getElevation()/TimeUtility.getTimeDiffSecond(calLast, calFirst))*3600);

                        }
                    }
                    if(!combined)
                        createSlope(list, accumulatedSlopeDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profileSetting);
                    accumulatedSlopeDistance = 0;
                    accumulatedSlopeElevation = 0;
                    descentDistance = 0;
                    gradientFirst = null;
                    gradientLast = null;
                }
            }
        }

        boolean shortSlopeCandidade =
                GPSUtility.gradientPercentage(accumulatedSlopeElevation, accumulatedSlopeDistance) > gradientUp;

        //last segment
        if(accumulatedSlopeDistance >= limitElevation || shortSlopeCandidade)  {
            boolean combined = false;
            if(list != null && !list.isEmpty()) {
                SlopeSegment lastSlope = list.get(list.size()-1);
                double lastSlopeEndDistance = lastSlope.getEndDistance();
                if((gradientFirst.getDistanceFromStartingPoint()-lastSlopeEndDistance) < 1) {
                    //fix lastSlope
                    combined = true;
                    if(lastSlope != null && gradientLast != null && gradientFirst != null) {
                        lastSlope.setElevation(lastSlope.getElevation() + (gradientLast.getEle() - gradientFirst.getEle()));
                        lastSlope.setDistance(lastSlope.getDistance() + (gradientLast.getDistanceFromStartingPoint() - gradientFirst.getDistanceFromStartingPoint()));
                        lastSlope.setGradient(GPSUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                        lastSlope.setEndLatitude(gradientLast.getLat());
                        lastSlope.setEndLongitude(gradientLast.getLon());
                        lastSlope.setEndDate(gradientLast.getDateRelevation());
                        lastSlope.setEndElevation(gradientLast.getEle());
                        lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                        //calculate waypoint for slope
                        List<Waypoint> waypointsTemp = new ArrayList();
                        for (int z = waypointStartIndex; z < waypointEndIndex; z++)
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

                        double power = PowerUtility.calculatePower(
                                profile == null ? 0 : profile.getWeight(),
                                bikeWeight,
                                lastSlope.getDistance(),
                                lastSlope.getGradient(),
                                lastSlope.getAvgSpeed()
                        );

                        lastSlope.setPower(power);
                        lastSlope.setVam((lastSlope.getElevation() / TimeUtility.getTimeDiffSecond(calLast, calFirst)) * 3600);
                    }

                }
            }
            if(!combined)
                createSlope(list, accumulatedSlopeDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profileSetting);

        }
        //FIXME new
        newSlopes = new ArrayList();
        mergeSlopes(list, profile, bikeWeight);
        if(newSlopes != null && newSlopes.size() > 0)
            return newSlopes.get(0);
        else
            return null;
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

        //get bikeWeight
        //TODO remove bike constants
        double bikeWeight = 9;
        List<Bike> bikes = profile.getBikes();
        if(bikes!=null && !bikes.isEmpty()) {
            for(Bike bike:bikes) {
                if(bike.isSelected()) {
                    bikeWeight = bike.getBikeWeight();
                    break;
                }
            }
        }

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
                            lastSlope.setGradient(GPSUtility.gradientPercentage(lastSlope.getElevation(), lastSlope.getDistance()));
                            lastSlope.setEndLatitude(gradientLast.getLat());
                            lastSlope.setEndLongitude(gradientLast.getLon());
                            lastSlope.setEndDate(gradientLast.getDateRelevation());
                            lastSlope.setEndElevation(gradientLast.getEle());
                            lastSlope.setEndDistance(gradientLast.getDistanceFromStartingPoint());
                            //calculate waypoint for slope
                            List<Waypoint> waypointsTemp = new ArrayList();
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
                            if(profile.getSelectedProfile() != null) {
                                power = PowerUtility.calculatePower(
                                        profile.getSelectedProfile().getWeight(),
                                        bikeWeight,
                                        lastSlope.getDistance(),
                                        lastSlope.getGradient(),
                                        lastSlope.getAvgSpeed()
                                );
                            }

                            lastSlope.setPower(power);
                            lastSlope.setVam((lastSlope.getElevation()/TimeUtility.getTimeDiffSecond(calLast, calFirst))*3600);

                        }
                    }
                    if(!combined)
                        createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profile);
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
                    List<Waypoint> waypointsTemp = new ArrayList();
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
                    if(profile.getSelectedProfile() != null) {
                        power = PowerUtility.calculatePower(
                                profile.getSelectedProfile().getWeight(),
                                bikeWeight,
                                lastSlope.getDistance(),
                                lastSlope.getGradient(),
                                lastSlope.getAvgSpeed()
                        );
                    }

                    lastSlope.setPower(power);
                    lastSlope.setVam((lastSlope.getElevation()/TimeUtility.getTimeDiffSecond(calLast, calFirst))*3600);

                }
            }
            if(!combined)
                createSlope(list, accumulatedDistance, gradientUp, gradientFirst, gradientLast, waypoints, waypointStartIndex, waypointEndIndex, profile);
        }

        return list;
    }

    private static void mergeSlopes(List<SlopeSegment> slopes, Profile profile, double bikeWeight) {
        if(slopes == null || slopes.isEmpty() || slopes.size() == 1) {
            if(slopes.size() == 1)
                newSlopes.add(slopes.get(0));
            return;
        }
        List<SlopeSegment> subList = slopes.subList(0, 2);
        SlopeSegment first = subList.get(0);
        SlopeSegment second = subList.get(1);
        double distance = first.getDistance() + second.getDistance();
        double distanceBetweenSegments = second.getStartDistance() - first.getEndDistance();

        double percentageDistance = (100.0 * distanceBetweenSegments) / distance;
        SlopeSegment result = null;
        if(percentageDistance <= 20.0 && first != null && second != null) {
            result = new SlopeSegment();
            result.setDistance(second.getEndDistance() - first.getStartDistance());
            result.setElevation(second.getEndElevation() - first.getStartElevation());
            result.setStartElevation(first.getStartElevation());
            result.setEndElevation(second.getEndElevation());
            result.setStartDate(first.getStartDate());
            result.setEndDate(second.getEndDate());
            result.setStartDistance(first.getStartDistance());
            result.setEndDistance(second.getEndDistance());
            result.setStartLatitude(first.getStartLatitude());
            result.setEndLatitude(second.getEndLatitude());
            result.setStartLongitude(first.getStartLongitude());
            result.setEndLongitude(second.getEndLongitude());
            result.setGradient(GPSUtility.gradientPercentage(result.getElevation(), result.getDistance()));
            if(second.getEndDate() != null && first.getEndDate() != null) {
                Calendar calLast = Calendar.getInstance();
                calLast.setTime(second.getEndDate());
                Calendar calFirst = Calendar.getInstance();
                calFirst.setTime(first.getStartDate());
                double timeDiffInHour = TimeUtility.getTimeDiffHour(calLast, calFirst);
                double speed = Math.abs(result.getDistance() / timeDiffInHour);
                result.setAvgSpeed(speed);
                double power = PowerUtility.calculatePower(
                        profile == null ? 0 : profile.getWeight(),
                        bikeWeight,
                        result.getDistance(),
                        result.getGradient(),
                        result.getAvgSpeed()
                );
                result.setPower(power);
                result.setVam((result.getElevation() / TimeUtility.getTimeDiffSecond(calLast, calFirst)) * 3600);
            }
            first.getWaypoints().addAll(second.getWaypoints());
            result.setWaypoints(first.getWaypoints());
            //newSlopes.add(result);
        }
        else
            newSlopes.add(first);

        List<SlopeSegment> temp = new ArrayList<>();
        int i = 1;
        if(result != null) {
            temp.add(result);
            i = 2;
        }
        for(;i < slopes.size(); i++)
            temp.add(slopes.get(i));
        mergeSlopes(temp, profile, bikeWeight);
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
            ProfileSetting profileSetting) {

        Profile profile = profileSetting.getSelectedProfile();
        //get bikeWeight
        //TODO remove bike constants
        double bikeWeight = 9;
        List<Bike> bikes = profileSetting.getBikes();
        if(bikes!=null && !bikes.isEmpty()) {
            for(Bike bike:bikes) {
                if(bike.isSelected()) {
                    bikeWeight = bike.getBikeWeight();
                    break;
                }
            }
        }

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

                double power = PowerUtility.calculatePower(
                        profile == null ? 0 : profile.getWeight(),
                        bikeWeight,
                        slope.getDistance(),
                        slope.getGradient(),
                        slope.getAvgSpeed()
                );

                slope.setPower(power);
                slope.setVam((slope.getElevation() / TimeUtility.getTimeDiffSecond(calLast, calFirst)) * 3600);

                //calculate waypoint for slope
                List<Waypoint> waypointsTemp = new ArrayList();
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
