package org.hifly.bikedump.controller;


import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.domain.gps.Coordinate;
import org.hifly.bikedump.domain.gps.Waypoint;
import org.hifly.bikedump.domain.strava.StravaActivity;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.PropUtility;
import org.hifly.bikedump.utility.SlopeUtility;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StravaController {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected static Logger log = LoggerFactory.getLogger(StravaController.class);

    private static Map<String, StravaController> instances = new HashMap<>();

    private JStravaV3 strava;

    private String accessToken;

    private StravaController(String accessToken) {
        strava = new JStravaV3(accessToken);
    }

    public static StravaController getInstance(String accessToken) {
        StravaController sc = instances.get(accessToken);
        if (instances.get(accessToken) == null) {
            sc = new StravaController(accessToken);
            sc.accessToken = accessToken;
            instances.put(accessToken, sc);
        }
        return sc;
    }

    public Track getFullInfoFromStrava(Track track, ProfileSetting profileSetting) {
        Map<String, List> map = getInfoFromStrava(track.getStartDate(), track.getFileName());
        track.setCoordinates(map.get("COORDINATES"));
        GPSUtility.GpsStats stats = GPSUtility.extractInfoFromWaypoints(map.get("WAYPOINTS"), track.getTotalDistance());
        track.setCoordinatesNewKm(stats.getWaypointsKm());
        if(track.getCoordinatesNewKm() != null)
            track.setStatsNewKm(GPSUtility.calculateStatsInUnit(track.getCoordinatesNewKm()));
        track.setMaxAltitude(stats.getMaxAltitude());
        track.setMinAltitude(stats.getMinAltitude());
        track.setAltimetricProfile(SlopeUtility.totalAltimetricProfile(map.get("WAYPOINTS")));
        track.setSlopes(SlopeUtility.extractSlope(map.get("WAYPOINTS"), profileSetting));
        track.setClimbingSpeed(stats.getClimbingSpeed());
        track.setClimbingTimeMillis(stats.getClimbingTime());
        track.setClimbingDistance(stats.getClimbingDistance());

        return track;

    }

    public Map<String, List> getInfoFromStrava(Date trackStart, String trackFilename) {

        Map<String, List> map = new HashMap<>();

        List<Coordinate> coordinates = new ArrayList<>();
        List<Waypoint> waypoints = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        Double eleGained;
        Double lastEle = null;
        Double lastLat = null;
        Double lastLon = null;
        Double totalDistance = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(trackFilename))) {
            String line;
            while ((line = br.readLine()) != null) {

                Double latitude = Double.valueOf(line.split(";")[0]);
                Double longitude = Double.valueOf(line.split(";")[1]);
                if(lastLat == null)
                    lastLat = latitude;
                if(lastLon == null)
                    lastLon = longitude;
                Double currentTimeSec = Double.valueOf(line.split(";")[2]);
                Double eleCurrent = Double.valueOf(line.split(";")[4]);
                Date currentTime = new Date(trackStart.getTime() + (currentTimeSec.longValue() * 1000));

                Coordinate coordinate = new Coordinate(latitude, longitude);
                coordinates.add(coordinate);

                if(lastEle == null)
                    eleGained = 0.0;
                else
                    eleGained = eleCurrent - lastEle;

                double distance = GPSUtility.haversine(latitude, longitude, lastLat, lastLon);
                totalDistance += distance;

                waypoints.add(GPSUtility.createWaypointWrapper(
                        latitude,
                        longitude,
                        distance,
                        lastEle == null ? eleCurrent: lastEle,
                        eleGained,
                        totalDistance,
                        0,
                        currentTime));

                lastEle = eleCurrent;
                lastLat = latitude;
                lastLon = longitude;

            }

        } catch (Exception ex) {
            sb.append("can't load:").append(trackFilename);
        }

        map.put("COORDINATES", coordinates);
        map.put("WAYPOINTS", waypoints);

        return map;
    }

    public Track getTrackFromStravaFile(String stravaFile) {

        Track track = new Track();
        Properties prop = new Properties();

        try(InputStream input = new FileInputStream(stravaFile)) {
            prop.load(input);

            track.setFromStrava(true);
            track.setName(prop.getProperty("name"));
            track.setFileName(FilenameUtils.removeExtension(stravaFile));
            track.setTotalDistance(Double.valueOf(prop.getProperty("distance")) / 1000);
            track.setEffectiveTime(Integer.valueOf(prop.getProperty("moving")) * 1000);
            track.setRealTime(Integer.valueOf(prop.getProperty("elapsed")) * 1000);
            track.setCalculatedElevation(Double.valueOf(prop.getProperty("elevation")));
            track.setRealElevation(Double.valueOf(prop.getProperty("elevation")));
            track.setCalculatedDescent(Double.valueOf(prop.getProperty("descent")));
            track.setRealDescent(Double.valueOf(prop.getProperty("descent")));
            track.setHeartFrequency(Double.valueOf(prop.getProperty("avgHeart")));
            track.setAvgTemperature(Double.valueOf(prop.getProperty("avgTemp")));
            track.setHeartMax(Double.valueOf(prop.getProperty("maxHeart")));
            track.setCalories(Double.valueOf(prop.getProperty("calories")).longValue());
            track.setEffectiveAvgSpeed(Double.valueOf(prop.getProperty("avgSpeed")));
            track.setMaxSpeed(Double.valueOf(prop.getProperty("maxSpeed")));
            try {
                Date startDate = dateFormat.parse(prop.getProperty("date"));
                Date endDate = new Date(startDate.getTime() + track.getRealTime());
                track.setStartDate(startDate);
                track.setEndDate(endDate);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return track;

    }

    //TODO use a token with private grants; public doesn't show private zones
    public String getActivityData(StravaActivity activity) {
        String activityId = activity.getId();
        String filename = System.getProperty("user.home") + "/.geomapviewer/strava/" + this.accessToken + "/" + activityId;
        File file = new File(filename);
        BufferedWriter bw = null;
        double descent = 0;
        if (!file.exists()) {
            List<Stream> streams = getStreamByActivity(activityId, new String[]{"time", "latlng", "distance", "altitude", "heartrate", "temp"});
            /*0 - latlng
              1 - time
              2 - distance
              3 - altitude
              4 - temp  */
            if (streams != null && !streams.isEmpty()) {
                try {
                    Files.createParentDirs(file);
                    Files.touch(file);
                    bw = new BufferedWriter(new FileWriter(file, true));
                    int elements = streams.get(0).getData().size();
                    int streams_count = streams.size();
                    for (int i = 0; i < elements; i++) {
                        StringBuilder line = new StringBuilder();
                        for (int k = 0; k < streams_count; k++) {
                            String type = streams.get(k).getType();
                            if (type.equalsIgnoreCase("latlng")) {
                                List temp = (List) streams.get(k).getData().get(i);
                                line.append(temp.get(0).toString());
                                line.append(";");
                                line.append(temp.get(1).toString());
                            }
                            else if (type.equalsIgnoreCase("altitude")) {
                                if(descent == 0) {
                                    List temp = (List) streams.get(k).getData();
                                    double previousAlt = 0;
                                    double currentAlt;
                                    for (Object tmp : temp) {
                                        currentAlt = (Double) tmp;
                                        if (currentAlt < previousAlt) {
                                            descent += (previousAlt - currentAlt);
                                        }
                                        previousAlt = currentAlt;
                                    }
                                }

                                line.append(streams.get(k).getData().get(i).toString());
                            }
                            else {
                                line.append(streams.get(k).getData().get(i).toString());
                            }
                            line.append(";");
                        }
                        bw.write(line.toString());
                        bw.newLine();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //write the property
                Properties prop = new Properties();
                PropUtility.insertInProperties(prop, "name", activity.getName());
                PropUtility.insertInProperties(prop, "date", activity.getDate());
                PropUtility.insertInProperties(prop, "avgHeart", String.valueOf(activity.getAvgHeart()));
                PropUtility.insertInProperties(prop, "avgSpeed", String.valueOf(activity.getAvgSpeed()));
                PropUtility.insertInProperties(prop, "avgTemp", String.valueOf(activity.getAvgTemp()));
                PropUtility.insertInProperties(prop, "calories", String.valueOf(activity.getCalories()));
                PropUtility.insertInProperties(prop, "elapsed", String.valueOf(activity.getElapsedTimeSec()));
                PropUtility.insertInProperties(prop, "elevation", String.valueOf(activity.getElevation()));
                PropUtility.insertInProperties(prop, "maxHeart", String.valueOf(activity.getMaxHeart()));
                PropUtility.insertInProperties(prop, "maxSpeed", String.valueOf(activity.getMaxSpeed()));
                PropUtility.insertInProperties(prop, "moving", String.valueOf(activity.getMovingTimeSec()));
                PropUtility.insertInProperties(prop, "distance", String.valueOf(activity.getDistance()));
                PropUtility.insertInProperties(prop, "descent", String.valueOf(descent));

                OutputStream out = null;
                try {
                    String filenameProp = System.getProperty("user.home") + "/.geomapviewer/strava/" + this.accessToken + "/" + activityId + ".prop";
                    File fileProp = new File(filenameProp);
                    Files.touch(fileProp);
                    out = new FileOutputStream(fileProp);
                    prop.store(out, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
        return filename;
    }

    public List<StravaActivity> getAllActivities() {
        List<StravaActivity> activities = new ArrayList<>();
        int page = 1;
        int size = 100;
        while (size == 100) {
            List<StravaActivity> temp = getActivitiesInPage(page, size);
            if (temp != null && !temp.isEmpty()) {
                activities.addAll(temp);
                size = temp.size();
                page++;
            } else {
                size = 0;
            }

        }
        return activities;

    }

    private List<Stream> getStreamByActivity(String activityId, String[] types) {
        return strava.findActivityStreams(Integer.valueOf(activityId), types);
    }

    private List<StravaActivity> getActivitiesInPage(int page, int size) {
        List<Activity> strava_activities = strava.getCurrentAthleteActivities(page, size);
        List<StravaActivity> activities = null;
        if (strava_activities != null && !strava_activities.isEmpty()) {
            activities = new ArrayList<>(strava_activities.size());
            for (Activity strava_activity : strava_activities) {
                StravaActivity activity = new StravaActivity();
                activity.setId(String.valueOf(strava_activity.getId()));
                activity.setName(strava_activity.getName());
                activity.setDate(strava_activity.getStart_date_local());
                activity.setAvgHeart(strava_activity.getAverage_heartrate());
                activity.setAvgSpeed((float) (strava_activity.getAverage_speed() * 3.6));
                activity.setAvgTemp(strava_activity.getAverage_temp());
                activity.setCalories(strava_activity.getCalories());
                activity.setElapsedTimeSec(strava_activity.getElapsed_time());
                activity.setElevation(strava_activity.getTotal_elevation_gain());
                activity.setMaxHeart(strava_activity.getMax_heartrate());
                activity.setMaxSpeed((float) (strava_activity.getMax_speed() * 3.6));
                activity.setMovingTimeSec(strava_activity.getMoving_time());
                activity.setDistance(strava_activity.getDistance());
                activities.add(activity);
            }
        }
        return activities;
    }

}
