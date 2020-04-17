package cat.bcn.vincles.mobile.Client.Business;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.RealmResults;

public class CalendarSyncManager {

    Context context;

    public CalendarSyncManager() {
        this.context = MyApplication.getAppContext();
    }

    public long addCalendar() {
        createCalendar();

        long createdCalendarId = getId();
        if (createdCalendarId == -1) {
            createCalendar();
            return getId();
        }

        return createdCalendarId;
    }




    public long createCalendar() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, "Vincles");
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        contentValues.put(CalendarContract.Calendars.NAME, "Vincles");
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Vincles");
        contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, context.getResources().getColor(R.color.colorPrimary));


        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        uri = uri.buildUpon().appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "Vincles")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();
        Uri insertedCalendar = context.getContentResolver().insert(uri, contentValues);
        Log.d("calsync","InsertedCal:"+insertedCalendar);

        return getId();
    }

    private long getId() {
        String[] mProjection = { CalendarContract.Calendars._ID };
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.NAME + " = ?))";
        String[] selectionArgs = new String[]{"Vincles", CalendarContract.ACCOUNT_TYPE_LOCAL, "Vincles"};

        if (hasCalendarPermissions(context)) {
            ContentResolver cr = context.getContentResolver();
            @SuppressLint("MissingPermission") Cursor cursor = cr.query(
                    CalendarContract.Calendars.CONTENT_URI, mProjection, selection, selectionArgs, null);

            if (cursor == null || cursor.getCount() == 0) return -1;
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));

        }
        return -2;
    }

    public static boolean hasCalendarPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean permissionsDenied(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_DENIED &&  ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_DENIED;
    }

    @SuppressLint("MissingPermission")
    public long addEvent(long calendarId, MeetingRealm meeting) {

        if (hasCalendarPermissions(context)) {
            ContentResolver cr = context.getContentResolver();

            Uri event = cr.insert(CalendarContract.Events.CONTENT_URI,
                    getContentValuesFromMeetingRealm(meeting, calendarId));
            return Long.parseLong(event.getLastPathSegment());
        }
        return -1;
    }

    private ContentValues getContentValuesFromMeetingRealm(MeetingRealm meeting, long calendarId) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(new Date(meeting.getDate()));

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(new Date(meeting.getDate() + meeting.getDuration() * 60 * 1000));

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, meeting.getDescription());
        values.put(CalendarContract.Events.DESCRIPTION, getListOfUsers(meeting.getGuestIDs()));
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Madrid");

        return values;
    }

    public void updateEvent(long calendarId, long eventId, MeetingRealm meeting) {

        if (hasCalendarPermissions(context)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CalendarContract.Events.DESCRIPTION, "Afegida descripció");


            String mSelectionClause = CalendarContract.Events._ID+ " = ?";
            String[] mSelectionArgs = {String.valueOf(eventId)};

            @SuppressLint("MissingPermission") int updCount = context.getContentResolver().update(
                    CalendarContract.Events.CONTENT_URI, getContentValuesFromMeetingRealm(meeting,
                            calendarId), mSelectionClause, mSelectionArgs);
        }
    }

    @SuppressLint("MissingPermission")
    public void deleteEvent(long calendarId, long eventId) {
        if (hasCalendarPermissions(context)) {

            String selection = CalendarContract.Events._ID+ " = ?";
            String[] selectionArgs = {String.valueOf(eventId)};

            ContentResolver cr = context.getContentResolver();
            int deletedRows = cr.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs);

        }
    }

    @SuppressLint("MissingPermission")
    public void deleteCalendar() {
        if (hasCalendarPermissions(context)) {

            String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + CalendarContract.Calendars.NAME + " = ?))";
            String[] selectionArgs = new String[]{"Vincles", CalendarContract.ACCOUNT_TYPE_LOCAL, "Vincles"};

            ContentResolver cr = context.getContentResolver();
            cr.delete(CalendarContract.Calendars.CONTENT_URI, selection, selectionArgs);

        }
    }

    public void addAllMeetings(long calendarId, List<MeetingRealm> meetings) {
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        for (MeetingRealm meeting : meetings) {
            long eventId = addEvent(calendarId, meeting);
            meeting.setAndroidCalendarEventId(eventId);
        }
        meetingsDb.updateMeetings(meetings);
    }

    public String getListOfUsers(List<Integer> userIds) {
        int myId = new UserPreferences(MyApplication.getAppContext()).getUserID();
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        if (userIds.size() == 0) return "";
        StringBuilder usersString = new StringBuilder();
        boolean putComma = false;
        for (Integer userId : userIds) {
            if (putComma) {
                usersString.append(", ");
            } else {
                putComma = true;
            }
            if (myId == userId) {
                usersString.append(context.getResources().getString(R.string.chat_username_you));
            } else {
                GetUser getUser = usersDb.findUserUnmanaged(userId);
                if (getUser != null) usersString.append(getUser.getName()).append(" ").append(getUser.getLastname());
            }
        }

        if (usersString.length() > 0) return context.getResources().getString(
                R.string.calendar_date_guests, (usersString + "."));
        return "";
    }

}
