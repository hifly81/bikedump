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


        double degreesFromGradient = GpsUtility.roundDoubleStat(
                Math.toDegrees(Math.atan(gradient/100.0)));
        double massSystem = GpsUtility.roundDoubleStat((massPerson+massBike)*gravitationalConstant);
        double sinDegrees = GpsUtility.roundDoubleStat(Math.sin(Math.toRadians(degreesFromGradient)));

        //newton
        double forceSystem = GpsUtility.roundDoubleStat(massSystem * sinDegrees);
        double avgSpeedMS = GpsUtility.roundDoubleStat(avgSpeed/3.6);
        //newton
        double forceAir = GpsUtility.roundDoubleStat(dragFactor * Math.pow(avgSpeedMS,2));
        double forceRotation = GpsUtility.roundDoubleStat(massSystem * dragFiction);
        double totalForce = GpsUtility.roundDoubleStat(forceSystem + forceAir + forceRotation);
        double powerDone = GpsUtility.roundDoubleStat(totalForce * avgSpeedMS);
        double powerBiker = GpsUtility.roundDoubleStat(powerDone / frictionOverallFactorη);

        return powerBiker;
    }

    public static void main(String[]args) {
      PowerUtility p = new PowerUtility();
      p.calculatePower(55.9,7.3,13.84,7.9,22);
    }

}

