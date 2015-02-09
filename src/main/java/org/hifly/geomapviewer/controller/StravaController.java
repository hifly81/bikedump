package org.hifly.geomapviewer.controller;


import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.athlete.Athlete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StravaController {

    protected static Logger log = LoggerFactory.getLogger(StravaController.class);

    private JStravaV3 strava;

    public StravaController(String accessToken) {
        strava = new JStravaV3(accessToken);
    }

    public Athlete getAthleteInfo() {
         return strava.getCurrentAthlete();
    }

}
