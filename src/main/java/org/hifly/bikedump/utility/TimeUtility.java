package org.hifly.bikedump.utility;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TimeUtility {

    public static final String ITA_DATE_FORMAT = "dd-MM-yyyy";

    public static double getTimeDiffHour(Calendar first, Calendar second) {
        if(first == null || second == null)
            return 0;
        final double millis = second.getTimeInMillis() - first.getTimeInMillis();
        return millis / (60 * 60 * 1000);
    }

    public static double getTimeDiffSecond(Calendar first, Calendar second) {
        if(first == null || second == null)
            return 0;
        final double millis = second.getTimeInMillis() - first.getTimeInMillis();
        return Math.abs(millis / 1000);
    }

    public static String toStringFromTimeDiff(long value) {
        long diffSeconds2 = Math.abs(value / 1000 % 60);
        long diffMinutes2 = Math.abs(value / (60 * 1000) % 60);
        long diffHours2 = Math.abs(value / (60 * 60 * 1000) % 24);
        long diffDays2 = Math.abs(value / (24 * 60 * 60 * 1000));

        return diffDays2+"D,"+diffHours2+"H,"+diffMinutes2+"M,"+diffSeconds2+"S";
    }

    public static String convertToString(String format,Date date) {
        if(date == null)
            return "";
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static Date convertToDate(String format,String date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date convertedDate = dateFormat.parse(date);
        return convertedDate;
    }

    public static Map.Entry<String,String> getSunriseSunsetTime(
            double lat, double lon, Date date) {
        if(date == null) {
            return null;
        }
        Location location = new Location(lat, lon);
        //TODO remove timezone constant
        //TODO consider AM/PM
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Rome");
        String officialSunrise = calculator.getOfficialSunriseForDate(c1);
        String officialSunset = calculator.getOfficialSunsetForDate(c1);
        return new AbstractMap.SimpleImmutableEntry<>(officialSunrise, officialSunset);
    }


}
