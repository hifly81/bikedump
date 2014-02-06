package org.hifly.geomapviewer.utility;

import java.util.Calendar;

/**
 * @author
 * @date 02/02/14
 */
public class TimeUtility {

    public static double getTimeDiff(Calendar first, Calendar second) {
        final double millis = second.getTimeInMillis() - first.getTimeInMillis();
        return millis / (60 * 60 * 1000);
    }

    public static String toStringFromTimeDiff(long value) {
        long diffSeconds2 = Math.abs(value / 1000 % 60);
        long diffMinutes2 = Math.abs(value / (60 * 1000) % 60);
        long diffHours2 = Math.abs(value / (60 * 60 * 1000) % 24);
        long diffDays2 = Math.abs(value / (24 * 60 * 60 * 1000));

        return diffDays2+"D,"+diffHours2+"H,"+diffMinutes2+"M,"+diffSeconds2+"S";

    }
}
