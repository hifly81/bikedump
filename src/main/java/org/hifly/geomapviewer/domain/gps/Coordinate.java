package org.hifly.geomapviewer.domain.gps;

/**
 * @author
 * @date 31/01/14
 */
public class Coordinate {

    private double decimalLatitude;
    private double decimalLongitude;
    private AngularCoordinate angularLatitude;
    private AngularCoordinate angularLongitude;

    public Coordinate() {

    }

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

    public void createAngularCoordinates() throws Exception {
        AngularCoordinate angularLatitude = calculateAngularValues(decimalLatitude);
        AngularCoordinate angularLongitude = calculateAngularValues(decimalLongitude);

        if (angularLatitude != null && angularLatitude.getDegrees() < 0)
            angularLatitude.setCardinalPoint('S');
        else
            angularLatitude.setCardinalPoint('N');

        if (angularLongitude != null && angularLongitude.getDegrees() < 0)
            angularLongitude.setCardinalPoint('W');
        else
            angularLongitude.setCardinalPoint('E');

        angularLongitude.setDegrees(angularLongitude.getDegrees());
        angularLongitude.setMinutes(angularLongitude.getMinutes());
        angularLongitude.setSeconds(angularLongitude.getSeconds());

        this.setAngularLatitude(angularLatitude);
        this.setAngularLongitude(angularLongitude);
    }

    private AngularCoordinate calculateAngularValues(double decimalCoordinate) {
        decimalCoordinate = Math.abs(decimalCoordinate);

        int degrees = 0;
        int minutes = 0;
        double seconds = 0;

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
