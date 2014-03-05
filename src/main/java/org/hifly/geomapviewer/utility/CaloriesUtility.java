package org.hifly.geomapviewer.utility;

import java.util.HashMap;

/**
 * @author
 * @date 03/03/14
 */
public class CaloriesUtility {

    private static HashMap<String,Double> mapSportMET = new HashMap<String,Double>();
    {
        mapSportMET.put("MOUNTAIN_BIKING",6.0);
        mapSportMET.put("CYCLING_SPORT",5.5);

    }

    //TODO consider even avgSpeed
    public static double calculateMETCalories(double weight, double durationMinutes, String sportType) {
        double met = mapSportMET.get(sportType);
        if(met!=0.0) {
            return GpsUtility.roundDoubleStat(durationMinutes * ((met*3.5*weight)/200));
        }
        //Total Calories Burned = Duration in Minutes
        // x (((MET – a number representing the type and intensity of your workout)
        // x 3.5 x your weight in kg)/200).

        //e.g Total Calories Burned = 60 x ((5 x 3.5 x 70)/200) = 367 calories.

        //http://en.wikipedia.org/wiki/Metabolic_equivalent

        return 0.0;
    }
}
