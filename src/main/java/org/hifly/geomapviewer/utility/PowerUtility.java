package org.hifly.geomapviewer.utility;

/**
 * @author
 * @date 27/02/14
 */
public class PowerUtility {

    //value in watt
    public static double calculatePower(
            double massPerson, double massBike, double distance, double gradient, double avgSpeed) {
        //Fp - (Fa + Fs + Fr + Fb ) = FAcc = ma
        //Wp = Wcη = Fpv = [Ka (v + vw )  + mg (sin(α) + Cr )]v

        double gravitationalConstant = 9.8;
        double frictionOverallFactorη = 0.95;
        double dragFactor = 0.3;
        double dragFiction = 0.003;


        double degreesFromGradient = GPSUtility.roundDoubleStat(
                Math.toDegrees(Math.atan(gradient / 100.0)));
        double massSystem = GPSUtility.roundDoubleStat((massPerson + massBike) * gravitationalConstant);
        double sinDegrees = GPSUtility.roundDoubleStat(Math.sin(Math.toRadians(degreesFromGradient)));

        //newton
        double forceSystem = GPSUtility.roundDoubleStat(massSystem * sinDegrees);
        double avgSpeedMS = GPSUtility.roundDoubleStat(avgSpeed / 3.6);
        //newton
        double forceAir = GPSUtility.roundDoubleStat(dragFactor * Math.pow(avgSpeedMS, 2));
        double forceRotation = GPSUtility.roundDoubleStat(massSystem * dragFiction);
        double totalForce = GPSUtility.roundDoubleStat(forceSystem + forceAir + forceRotation);
        double powerDone = GPSUtility.roundDoubleStat(totalForce * avgSpeedMS);
        double powerBiker = GPSUtility.roundDoubleStat(powerDone / frictionOverallFactorη);

        return powerBiker;
    }

}

