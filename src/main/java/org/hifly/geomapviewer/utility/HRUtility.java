package org.hifly.geomapviewer.utility;


import org.hifly.geomapviewer.domain.hr.HRZone;

public class HRUtility {

    //TODO use HR zone in details
    public static HRZone getHRZone(double avgHr, double actualHr) {
        double actualLimitHr =  (actualHr * 100.0) / avgHr;
        if(actualLimitHr < HRZone.ZONE1.getLimit()) {
            return HRZone.ZONE1;
        }
        else if(actualLimitHr >= HRZone.ZONE1.getLimit() && actualLimitHr < HRZone.ZONE2.getLimit()) {
            return HRZone.ZONE2;
        }
        else if(actualLimitHr >= HRZone.ZONE2.getLimit() && actualLimitHr < HRZone.ZONE3.getLimit()) {
            return HRZone.ZONE3;
        }
        else if(actualLimitHr >= HRZone.ZONE3.getLimit() && actualLimitHr < HRZone.ZONE4.getLimit()) {
            return HRZone.ZONE4;
        }
        else {
            return HRZone.ZONE5;
        }
    }

}
