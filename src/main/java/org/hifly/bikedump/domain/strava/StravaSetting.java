package org.hifly.bikedump.domain.strava;

import java.io.Serializable;
import java.util.List;

public class StravaSetting implements Serializable {

    private static final long serialVersionUID = 9L;

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
