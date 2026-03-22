package org.hifly.bikedump.domain.gps;

public class Coordinate {

    private final double decimalLatitude;
    private final double decimalLongitude;
    private AngularCoordinate angularLatitude;
    private AngularCoordinate angularLongitude;

    public Coordinate(double lat,double lon) {
        this.decimalLatitude = lat;
        this.decimalLongitude = lon;
    }

    public static class AngularCoordinate {
        char cardinalPoint;
        int degrees;
        int minutes;
        double seconds;

        public String toString() {
            return degrees + "° " + minutes + "' " + seconds + "'' " + cardinalPoint;
        }
    }

    public double getDecimalLatitude() {
        return decimalLatitude;
    }

    public double getDecimalLongitude() {
        return decimalLongitude;
    }

}
