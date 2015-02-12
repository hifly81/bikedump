package org.hifly.geomapviewer.controller;


import com.google.common.io.Files;
import org.hifly.geomapviewer.domain.strava.StravaActivity;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.athlete.Athlete;
import org.jstrava.entities.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StravaController {

    protected static Logger log = LoggerFactory.getLogger(StravaController.class);

    private static Map<String,StravaController> instances = new HashMap<>();

    private JStravaV3 strava;

    private String accessToken;

    public static StravaController getInstance(String accessToken) {
        StravaController sc = instances.get(accessToken);
        if (instances.get(accessToken) == null) {
            sc = new StravaController(accessToken);
            sc.accessToken = accessToken;
            instances.put(accessToken, sc);
        }
        return sc;
    }

    private StravaController(String accessToken) {
        strava = new JStravaV3(accessToken);
    }

    public String getActivity(String activityId) {

        String filename = System.getProperty("user.home") + "/.geomapviewer/strava/" + this.accessToken + "/" +activityId;
        File file = new File(filename);
        BufferedWriter bw = null;
        if(!file.exists()) {
            List<Stream> streams = getStreamByActivity(activityId, new String[]{"time", "latlng", "distance" ,"altitude", "heartrate", "temp"});
            if(streams!= null && !streams.isEmpty()) {
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
                            } else {
                                line.append(streams.get(k).getData().get(i).toString());
                            }
                            line.append(";");
                        }
                        bw.write(line.toString());
                        bw.newLine();
                    }
                }
                catch (Exception ex) {
                  ex.printStackTrace();
                }
                finally {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return filename;
    }

    public Athlete getAthleteInfo() {
        return strava.getCurrentAthlete();
    }

    public List<StravaActivity> getAllActivities() {
        List<StravaActivity> activities = new ArrayList<>();
        int page = 1;
        int size = 100;
        while( size == 100) {
            List<StravaActivity> temp = getActivitiesInPage(page, size);
            if(temp != null && !temp.isEmpty()) {
                activities.addAll(temp);
                size = temp.size();
                page++;
            }
            else {
                size = 0;
            }

        }
        return activities;

    }

    private List<Stream> getStreamByActivity(String activityId, String [] types) {
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
                activity.setDate(strava_activity.getStart_date());
                activities.add(activity);
            }
        }
        return activities;
    }


}
