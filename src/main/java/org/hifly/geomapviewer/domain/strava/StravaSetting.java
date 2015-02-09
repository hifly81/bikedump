package org.hifly.geomapviewer.domain.strava;

import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 17/03/14
 */
public class StravaSetting implements Serializable {

    private List<StravaAthlete> stravaAthletes;
    private StravaAthlete currentAthleteSelected;

    public List<StravaAthlete> getStravaAthletes() {
        return stravaAthletes;
    }

    public void setStravaAthletes(List<StravaAthlete> stravaAthletes) {
        this.stravaAthletes = stravaAthletes;
    }

    public StravaAthlete getCurrentAthleteSelected() {
        return currentAthleteSelected;
    }

    public void setCurrentAthleteSelected(StravaAthlete currentAthleteSelected) {
        this.currentAthleteSelected = currentAthleteSelected;
    }



}
