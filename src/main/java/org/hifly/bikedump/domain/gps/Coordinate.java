package org.hifly.bikedump.domain.gps;


public class Coordinate {

    private double decimalLatitude;
    private double decimalLongitude;
    private AngularCoordinate angularLatitude;
    private AngularCoordinate angularLongitude;

    public Coordinate() {}

    public Coordinate(double lat,double lon) {
        this.decimalLatitude = lat;
        this.decimalLongitude = lon;
    }

    public class AngularCoordinate {
        char cardinalPoint;
        int degrees;
        int minutes;
        double seconds;

        public char getCardinalPoint() {
            return cardinalPoint;
        }

        public void setCardinalPoint(char cardinalPoint) {
            this.cardinalPoint = cardinalPoint;
        }

        public int getDegrees() {
            return degrees;
        }

        public void setDegrees(int degrees) {
            this.degrees = degrees;
        }

        public int getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }

        public double getSeconds() {
            return seconds;
        }

        public void setSeconds(double seconds) {
            this.seconds = seconds;
        }

        public String toString() {
            return degrees + "° " + minutes + "' " + seconds + "'' " + cardinalPoint;
        }


    }

    public double getDecimalLatitude() {
        return decimalLatitude;
    }

    public void setDecimalLatitude(double decimalLatitude) {
        this.decimalLatitude = decimalLatitude;
    }

    public double getDecimalLongitude() {
        return decimalLongitude;
    }

    public void setDecimalLongitude(double decimalLongitude) {
        this.decimalLongitude = decimalLongitude;
    }

    public AngularCoordinate getAngularLatitude() {
        return angularLatitude;
    }

    public void setAngularLatitude(AngularCoordinate angularLatitude) {
        this.angularLatitude = angularLatitude;
    }

    public AngularCoordinate getAngularLongitude() {
        return angularLongitude;
    }

    public void setAngularLongitude(AngularCoordinate angularLongitude) {
        this.angularLongitude = angularLongitude;
    }

    private AngularCoordinate calculateAngularValues(double decimalCoordinate) {
        decimalCoordinate = Math.abs(decimalCoordinate);

        int degrees;
        int minutes;
        double seconds;

        degrees = (int)decimalCoordinate;
        double numberOfMinutes = (decimalCoordinate-(int)decimalCoordinate)*60;
        minutes = (int)numberOfMinutes;
        double numberOfSeconds = (numberOfMinutes-(int)numberOfMinutes)*60;
        seconds = numberOfSeconds;

        AngularCoordinate angularCoordinate = new AngularCoordinate();
        angularCoordinate.setDegrees(degrees);
        angularCoordinate.setMinutes(minutes);
        angularCoordinate.setSeconds(seconds);

        return angularCoordinate;
    }

}
