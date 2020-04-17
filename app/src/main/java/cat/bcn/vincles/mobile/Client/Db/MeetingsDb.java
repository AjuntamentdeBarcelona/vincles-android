package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MeetingsDb extends BaseDb {

    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_ACCEPTED = "ACCEPTED";
    public static final String USER_REJECTED = "REJECTED";

    public MeetingsDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(MeetingRealm.class).findAll().deleteAllFromRealm();
                    realm.where(MeetingUserInfoRest.class).findAll().deleteAllFromRealm();
                }
            });
        }
    }


    public void updateMeeting(final int meetingId, final String description, final long date, final int duration,
                              final int[] guests, final long androidCalendarId) {
        try (Realm realmInstance = Realm.getDefaultInstance()){

            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    MeetingRealm meetingRealm  = realm.where(MeetingRealm.class)
                            .equalTo("id", meetingId)
                            .findFirst();
                    if (meetingRealm==null){
                        meetingRealm = new MeetingRealm();
                    }

                    RealmList<Integer> ids = meetingRealm.getGuestIDs();
                    ArrayList<String> states = new ArrayList<>(meetingRealm.getGuestStates());
                    meetingRealm.getGuests().clear();
                    for (int i = 0; i < guests.length; i++) {
                        if (!ids.contains(guests[i])) {
                            states.add(i, "PENDING");
                        }
                        GetUser getUser = realm.where(GetUser.class).equalTo("id", guests[i]).findFirst();
                        if (getUser!=null){
                            if (!meetingRealm.getGuests().contains(getUser))
                            meetingRealm.getGuests().add(getUser);
                        }
                    }

                    GetUser getUser = realm.where(GetUser.class).equalTo("id", meetingRealm.getHostId()).findFirst();
                    meetingRealm.setHost(getUser);
                    meetingRealm.setDescription(description);
                    meetingRealm.setDate(date);
                    meetingRealm.setDuration(duration);
                    meetingRealm.setGuestIDs(OtherUtils.convertIntegersToRealmList(guests));
                    meetingRealm.setGuestStates(OtherUtils.convertStringsToRealmList(states));
                    realm.copyToRealmOrUpdate(meetingRealm);
                    if (androidCalendarId != -1) {
                        new CalendarSyncManager().updateEvent(androidCalendarId,
                                meetingRealm.getAndroidCalendarEventId(), meetingRealm);
                    }
                }
            });

        }


    }

    public void updateMeetings(final List<MeetingRealm> meetings){
        try (Realm realmInstance = Realm.getDefaultInstance()){

            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    for (MeetingRealm meetingRealm : meetings){
                        GetUser host = realm.where(GetUser.class)
                                .equalTo("id", meetingRealm.getHost().getId())
                                .findFirst();
                        if (host!=null){
                            meetingRealm.getHost().setIdCircle(host.getIdCircle());
                        }
                        int i = 0;
                        for (GetUser user : meetingRealm.getGuests()){
                            GetUser userRealm = realm.where(GetUser.class)
                                    .equalTo("id", user.getId())
                                    .findFirst();

                            if (userRealm != null && meetingRealm.getGuests().get(i) != null){
                                meetingRealm.getGuests().get(i).setIdCircle(userRealm.getIdCircle());
                            }
                            i++;
                        }
                    }
                    realm.copyToRealmOrUpdate(meetings);
                }
            });
        }
    }


    public void setAlertShownMeeting(final int meetingId) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    MeetingRealm meetingRealm = realm.where(MeetingRealm.class)
                            .equalTo("id", meetingId)
                            .findFirst();
                    if (meetingRealm != null) {
                        meetingRealm.setAlertShown(true);
                    }
                }
            });

        }
    }

    public void setShouldShowMeeting(final int meetingId, final boolean shouldShow, final long androidCalendarId) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    MeetingRealm meetingRealm = realm.where(MeetingRealm.class)
                            .equalTo("id", meetingId)
                            .findFirst();
                    if (meetingRealm == null) {
                        return;
                    }
                    if (androidCalendarId != -1) {
                            new CalendarSyncManager().deleteEvent(androidCalendarId,
                                    meetingRealm.getAndroidCalendarEventId());
                    }
                    meetingRealm.setShouldShow(shouldShow);

                }
            });
        }

    }


    public void deleteMeeting(int meetingId, long androidCalendarId) {
        setShouldShowMeeting(meetingId, false, androidCalendarId);
    }

    public void setMeetingToAccepted(final int meetingId, final Integer guestId) {

        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    MeetingRealm meetingRealm = realm.where(MeetingRealm.class)
                            .equalTo("id", meetingId)
                            .findFirst();
                    int position;
                    if (meetingRealm != null) {
                        for (position = 0; position < meetingRealm.getGuestIDs().size(); position++) {
                            if (meetingRealm.getGuestIDs().size() > position && meetingRealm.getGuestIDs().get(position) != null && meetingRealm.getGuestIDs().get(position).equals(guestId)){
                                break;
                            }
                        }
                        meetingRealm.getGuestStates().set(position, MeetingUserInfoRest.ACCEPTED);
                    }

                }
            });
        }

    }

    public void saveMeeting(final MeetingRealm meetingRealm ) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser getUser = realm.where(GetUser.class).equalTo("id", meetingRealm.getHostId()).findFirst();
                    if (getUser!=null){
                        if(meetingRealm.getGuestIDs().size()!=0){
                            Integer[] guestsIds = OtherUtils.convertIntegersP(meetingRealm.getGuestIDs());

                            RealmResults<GetUser> guestsResult = realm.where(GetUser.class).in("id", guestsIds).findAll();

                            if (guestsResult!=null){
                                RealmList <GetUser> guestsResultList = new RealmList<GetUser>();
                                guestsResultList.addAll(guestsResult.subList(0, guestsResult.size()));
                                meetingRealm.setGuests(guestsResultList);
                            }
                        }
                        meetingRealm.setHost(getUser);
                        realm.copyToRealmOrUpdate(meetingRealm);
                    }

                }
            });
        }
    }

    public void saveMeetingRest(MeetingRest meetingRest, long androidCalendarId) {

        MeetingRealm meetingRealm = new MeetingRealm(meetingRest);
        if (androidCalendarId != -1) {
            MeetingRealm oldMeeting = findMeeting(meetingRest.getId());
            if (oldMeeting != null) {
                new CalendarSyncManager().updateEvent(androidCalendarId,
                        oldMeeting.getAndroidCalendarEventId(), meetingRealm);
                meetingRealm.setAndroidCalendarEventId(oldMeeting.getAndroidCalendarEventId());
            } else {
                meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                        .addEvent(androidCalendarId, meetingRealm));
            }
        }

        saveMeeting(meetingRealm);
    }

    public void saveMeetingsRestList(List<MeetingRest> meetings, long androidCalendarId) {

        List<MeetingRealm> meetingRealmList = new ArrayList<>();
        for (MeetingRest messageRest : meetings) {
            MeetingRealm meetingRealm = new MeetingRealm(messageRest);
            if (androidCalendarId != -1) {
                meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                        .addEvent(androidCalendarId, meetingRealm));
            }
            meetingRealmList.add(meetingRealm);
        }

        updateMeetings(meetingRealmList);

    }

    public List<MeetingRealm> findAllShownMeetings() {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<MeetingRealm> meetingRealms = realmInstance.where(MeetingRealm.class)
                    .sort("date", Sort.ASCENDING)
                    .equalTo("shouldShow", true)
                    .findAll();
            if (meetingRealms == null) return null;
            return realmInstance.copyFromRealm(meetingRealms);
        }
    }

    public RealmResults<MeetingRealm> findAllShownMeetingsAsync(Realm realm) {
        //WARNING
        //Remember to close the realm instance where you use this method
     //   Realm realm = Realm.getDefaultInstance();
        return realm.where(MeetingRealm.class)
                .sort("date", Sort.ASCENDING)
                .equalTo("shouldShow", true)
                .findAll();

    }

    public MeetingRealm findMeeting(int id) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            MeetingRealm meetingRealm = realmInstance.where(MeetingRealm.class)
                    .equalTo("id", id)
                    .findFirst();
            if (meetingRealm==null) return null;
            return realmInstance.copyFromRealm(meetingRealm);
        }
    }

    public void setMeetingUserState(final int meetingId, final int userId, final String state) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    MeetingRealm meetingRealm = realm.where(MeetingRealm.class)
                            .equalTo("id", meetingId)
                            .findFirst();

                    if (meetingRealm==null) return;

                    List<Integer> ids = meetingRealm.getGuestIDs();
                    Iterator<Integer> it = ids.iterator();
                    int index = 0;
                    while (it.hasNext()) {
                        Integer theId = it.next();
                        if (theId == userId){
                            if (state.equals(USER_DELETED)) {
                                meetingRealm.getGuestIDs().remove(index);
                                meetingRealm.getGuestStates().remove(index);
                            } else {
                                meetingRealm.getGuestStates().set(index, state);
                            }
                            return;
                        }
                        index++;
                    }
                }
            });
        }
    }

    public int getNumberOfMeetingsPending() {
        Integer id = new UserPreferences(MyApplication.getAppContext()).getUserID();
        int res = 0;
        Calendar cal = Calendar.getInstance();

        try(Realm realmInstance = Realm.getDefaultInstance()) {
            RealmResults<MeetingRealm> results = realmInstance.where(MeetingRealm.class)
                    .sort("date", Sort.ASCENDING)
                    .greaterThan("date", cal.getTimeInMillis())
                    .equalTo("shouldShow", true)
                    .findAll();

            for (MeetingRealm meetingRealm : results) {
                RealmList<Integer> guestIds = meetingRealm.getGuestIDs();
                RealmList<String> guestStates = meetingRealm.getGuestStates();
                for (int i = 0; i < guestIds.size(); i++) {
                    if (guestIds.get(i) != null && guestIds.get(i).equals(id)) {
                        if (guestStates.get(i).equalsIgnoreCase(MeetingUserInfoRest.PENDING)) {
                            res++;
                        }
                        break;
                    }
                }
            }
            return res;
        }
    }

    public MeetingRealm findFirstMeetingAfterTime(long time) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            MeetingRealm meetingRealm = realmInstance.where(MeetingRealm.class)
                    .sort("date", Sort.ASCENDING)
                    .equalTo("shouldShow", true)
                    .equalTo("alertShown", false)
                    .greaterThan("date", time)
                    .findFirst();
            if (meetingRealm == null)return null;
            return realmInstance.copyFromRealm(meetingRealm);
        }
    }

}