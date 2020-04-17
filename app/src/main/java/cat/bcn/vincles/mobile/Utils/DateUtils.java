package cat.bcn.vincles.mobile.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cat.bcn.vincles.mobile.R;
import io.realm.RealmList;

public class DateUtils {

    public static String getFormattedHourMinutesFromMillis(Context context, long millis) {
        return getFormatedHourMinute(millis);
    }


    public static String getFormatedDate(boolean isCatalan, int day, int month, int year) {
        Configuration configuration = MyApplication.getAppContext().getResources().getConfiguration();
        if (isCatalan) {
            configuration.locale = new Locale("ca");
        } else {
            configuration.locale = new Locale("es");
        }
        Resources resources = MyApplication.getAppContext().getResources();
        resources.updateConfiguration(configuration, null);
        Log.d("lng", "updateConfiguration 2");

        String[] months = resources.getStringArray(R.array.months);
        String monthText = months[month];

        String of = "de ";

        if(isCatalan && (month == 3 || month == 7 || month == 9 )){
            of = "d'";
        }

        return day + " " + of + monthText + " " + year;
    }

    public static String getFormatedDate(Locale locale, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));

        return getFormatedDate(isCatalan(locale), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    public static String getFormatedHourMinute(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));

        return getTimeFromHourAndMinute(
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                String.valueOf(calendar.get(Calendar.MINUTE)));
    }

    public static String getTimeFromHourAndMinute(String hour, String minute) {
        return (hour.length() == 1 ? "0"+hour : hour) + ":"
                + (minute.length() == 1 ? "0"+minute : minute);
    }

    public static String getFormatedTimeFromMillis(int millis) {
        int seconds = (millis/1000)%60;
        String secondsString = String.valueOf(seconds);
        if (seconds < 10) secondsString = "0" + secondsString;

        int minutes = millis/1000/60;
        String minutesString = String.valueOf(minutes);
        if (minutes < 10) minutesString = "0" + minutesString;

        return minutesString + ":" + secondsString;
    }

    public static String getFormatedTimeFromMillisRecorder(int millis) {
        int seconds = 60 - ((millis/1000)%60);
        String secondsString = String.valueOf(seconds);
        if (seconds < 10) secondsString = "0" + secondsString;

        int minutes = millis/1000/60;
        String minutesString = String.valueOf(minutes);
        if (minutes < 10) minutesString = "0" + minutesString;

        return  minutesString + ":" + secondsString;
    }

    public static String getFormatedTimeFromMillisAudioRecorder(int millisUntilFinished) {


        return  String.format(Locale.getDefault(),"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
    }


    private static String getOfText(Date date, boolean isCatalan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        String of = "de ";
        if(isCatalan && (month == 3 || month == 7 || month == 9 )){
            of = "d'";
        }
        return of;
    }

    public static String getNotificationFormatedTime(long millis, Locale locale) {
        Date date = new Date(millis);
        String of = getOfText(date, isCatalan(locale));
        of = of.replace("'","''");
        String pattern = "EEEE d MMMM 'de' yyyy kk':'mm'h'";
        Log.d("asd","pattern:"+pattern);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        return simpleDateFormat.format(date);
    }

    public static String getAlertFormatedTime(long millis, Locale locale) {
        Date date = new Date(millis);
        String of = getOfText(date, isCatalan(locale));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM 'de' yyyy',' kk':'mm", locale);
        return simpleDateFormat.format(date);
    }

    public static String getCalendarDate(long millis, Locale locale) {
        Date date = new Date(millis);
        String of = getOfText(date, isCatalan(locale));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM 'de' yyyy", locale);
        return simpleDateFormat.format(date);
    }

    public static String getNewMeetingDate(long millis, Locale locale) {
        Date date = new Date(millis);
        String of = getOfText(date, isCatalan(locale));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yyyy kk':'mm", locale);
        return simpleDateFormat.format(date);
    }

    public static String getMeetingDetailDate(long millis, Locale locale) {
        Date date = new Date(millis);
        String of = getOfText(date, isCatalan(locale));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM 'de' yyyy", locale);
        return simpleDateFormat.format(date);
    }

    public static String getCalendarDay(long millis, Locale locale) {
        Date date = new Date(millis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", locale);
        return simpleDateFormat.format(date);
    }

    public static String getCalendarMonthYear(long millis, Resources resources) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(millis));
        String[] months = resources.getStringArray(R.array.months);
        return months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
    }

    public static boolean isCatalan(Locale locale) {
        return locale.getLanguage().contains("ca");
    }


}
