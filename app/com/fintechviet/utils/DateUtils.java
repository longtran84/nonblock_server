package com.fintechviet.utils;

import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by tungn on 9/1/2017.
 */
public class DateUtils {
    private static String FORMAT_DATE = "dd/MM/yyyy HH:mm:SS";
    private static String FORMAT_DATE_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static String FORMAT_DATE1 = "dd/MM/yyyy";
    private static String FORMAT_DATE_STR = "dd/MM/yyyy";
    private static String FORMAT_DATE2 = "yyyy-MM-dd HH:mm:ss";
    public static String EQUAL = "EQUAL";
    public static String BEFORE = "BEFORE";
    public static String AFTER = "AFTER";

    public static Date convertStringToDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE);
            return dateFormat.parse(dateStr);
        } catch (ParseException ex){
        }
        return null;
    }

    public static Date convertStringToOnlyDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE1);
            return dateFormat.parse(dateStr);
        } catch (ParseException ex){
        }
        return null;
    }

    public static String convertDateToStringUTC(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE_UTC);
        return dateFormat.format(date);
    }

    public static Date convertStringToDate2(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE2);
            return dateFormat.parse(dateStr);
        } catch (ParseException ex){
        }
        return null;
    }

    public static String compare(String d1, String d2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
            Date date1 = sdf.parse(d1);
            Date date2 = sdf.parse(d2);

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);

            if (cal1.after(cal2)) {
                return DateUtils.AFTER;
            }

            if (cal1.before(cal2)) {
                return DateUtils.BEFORE;
            }

            if (cal1.equals(cal2)) {
                return DateUtils.EQUAL;
            }
        } catch (ParseException ex){
        }
        return DateUtils.EQUAL;
    }

    public static Tuple2<Date, Date> getCurrentWeekInterval() {
        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar first, last;

        first = (Calendar) today.clone();
        int dayOfWeek = first.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                last = (Calendar) first.clone();
                first.add(Calendar.DAY_OF_WEEK, -6);

                break;
            default:
                first.add(Calendar.DAY_OF_WEEK, Calendar.MONDAY - dayOfWeek);

                last = (Calendar) first.clone();
                last.add(Calendar.DAY_OF_WEEK, 6);

                break;
        }

        return Tuple.of(first.getTime(), last.getTime());
    }
}
