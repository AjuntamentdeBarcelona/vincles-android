package cat.bcn.vincles.mobile.UI.Calendar;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestSendModel;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendMeetingRequest;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CalendarNewDatePresenter extends Fragment implements CalendarNewDatePresenterContract,
        SendMeetingRequest.OnResponse, GetUserPhotoRequest.OnResponse {

    boolean isEditing;

    BaseRequest.RenewTokenFailed listener;
    CalendarNewDateFragmentView view;
    MeetingRealm meeting;
    int meetingId;
    MeetingRestSendModel meetingUpdated;
    UserPreferences userPreferences;

    ArrayList<Integer> contactIds;
    ArrayList<Contact> contacts;
    boolean contactIdsLoaded = false;

    boolean showingWaitDialog, showingErrorDialog, shouldGoBack;
    Object error;

    public CalendarNewDatePresenter(){
        contactIds = new ArrayList<>();
    }

    public static CalendarNewDatePresenter newInstance(BaseRequest.RenewTokenFailed listener,
                                                       CalendarNewDateFragmentView view,
                                                       Bundle savedInstanceState,
                                                       boolean isEditing) {
        CalendarNewDatePresenter fragment = new CalendarNewDatePresenter();
        Bundle args = new Bundle();
        args.putBoolean("isEditing", isEditing);
        fragment.setArguments(args);
        fragment.setExternalVars(listener, view, savedInstanceState);

        return fragment;
    }


    public void setExternalVars(BaseRequest.RenewTokenFailed listener,
                                CalendarNewDateFragmentView view,
                                Bundle savedInstanceState) {
        this.listener = listener;
        this.view = view;
        userPreferences = new UserPreferences(MyApplication.getAppContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        isEditing = getArguments().getBoolean("isEditing");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        view = null;
    }


    @Override
    public void onCreateView() {
        if (view != null) {
            if (shouldGoBack) {
                view.goBack();
            } else if (showingErrorDialog) {
                view.showError(error);
            } else if (showingWaitDialog) {
                view.showWaitDialog();
            }
        }
        if (contactIdsLoaded) {
            if (view != null) {
                if (contacts == null) {
                    contacts = getContactList();
                    view.showContacts(contacts);
                } else {
                    contacts.clear();
                    contacts.addAll(getContactList());
                    view.showContacts(contacts);
                }
            }
        }

    }


    @Override
    public void loadMeeting(int meetingId) {
        this.meetingId = meetingId;
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        meeting = meetingsDb.findMeeting(meetingId);
        Log.d("conreg","load meeting sz before:"+(contactIds != null ? contactIds.size() : 0));
        if (!contactIdsLoaded) contactIds = new ArrayList<>(meeting.getGuestIDs());
        contactIdsLoaded = true;
        Log.d("conreg","load meeting sz after:"+(contactIds != null ? contactIds.size() : 0));


        contacts = getContactList();
        if (view != null) view.showMeeting(meeting, contacts);
    }

    private ArrayList<Contact> getContactList() {
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        ArrayList<Contact> contactsList = new ArrayList<>();
        for (int id : contactIds) {
            contactsList.add(createContact(usersDb.findUserUnmanaged(id)));
        }
        return contactsList;
    }

    private Contact createContact(GetUser getUser) {
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setIdContentPhoto(getUser.getIdContentPhoto() != null ? getUser.getIdContentPhoto() : 0);
        contact.setPath(getUser.getPhoto());

        return contact;
    }

    @Override
    public void onSaveClicked(String description, long startDate, int lengthDate) {
        if (isEditing && description.equals(meeting.getDescription())
                && startDate == meeting.getDate() && lengthDate == meeting.getDuration()
                && OtherUtils.arraysEqual(contactIds, meeting.getGuestIDs())) {
            if (view == null) {
                shouldGoBack = true;
            } else {
                view.goBack();
                shouldGoBack = false;
            }
        } else if (description.length() == 0) {
            if (view != null) view.showEmptyTitleError();
        } else {
            meetingUpdated = new MeetingRestSendModel(startDate,
                    lengthDate, description, OtherUtils.convertIntegers(contactIds));
            if (view != null) view.showWaitDialog();
            SendMeetingRequest sendMeetingRequest = new SendMeetingRequest(listener,
                    isEditing ? meetingId : SendMeetingRequest.CREATE_MEETING, meetingUpdated);
            sendMeetingRequest.addOnOnResponse(this);
            sendMeetingRequest.doRequest(userPreferences.getAccessToken());
        }
    }

    @Override
    public void stopedShowingErrorDialog() {
        showingErrorDialog = false;
    }

    @Override
    public void addContacts(ArrayList<Integer> contactIds) {
        Log.d("conreg","addContacts:"+(contactIds != null ? contactIds.size() : 0));
        if (this.contactIds.size() == 0 && contactIds != null) {
            this.contactIds = contactIds;
            contactIdsLoaded = true;
        } else if (contactIds != null) {
            this.contactIds.clear();
            this.contactIds.addAll(contactIds);
            contactIdsLoaded = true;
        }
        if (view != null) {
            if (contacts == null) {
                contacts = getContactList();
                view.showContacts(contacts);
            } else {
                contacts.clear();
                contacts.addAll(getContactList());
                view.notifyContactChange();
            }
        }
    }

    @Override
    public void onResponseCreateMeeting(Response<ResponseBody> response) {
        try {
            int id = Integer.parseInt(response.body().string().replaceAll("[\\D]", ""));

            RealmList<String> guestStates = new RealmList<>();
            for (int i = 0; i < meetingUpdated.getInviteTo().length; i++) {
                guestStates.add("PENDING");
            }

            MeetingRealm meetingRealm = new MeetingRealm(id, meetingUpdated.getDate(), meetingUpdated.getDuration(), meetingUpdated.getDescription(), userPreferences.getUserID(),
                    OtherUtils.convertIntegersToRealmList(meetingUpdated.getInviteTo()), guestStates, true);
            long androidCalendarId = userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1;
            if (androidCalendarId != -1) {
                meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                        .addEvent(androidCalendarId, meetingRealm));
            }

            MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
            meetingsDb.saveMeeting(meetingRealm);
            /*meetingsDb.saveMeeting(id, userPreferences.getUserID(),
                    meetingUpdated.getDescription(), meetingUpdated.getDate(),
                    meetingUpdated.getDuration(), meetingUpdated.getInviteTo(),
                    userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);*/


            if (view != null) view.hideWaitDialog();
            if (view != null) view.onMeetingCreatedOrUpdated();
            showingWaitDialog = false;
            if (view == null) {
                shouldGoBack = true;
            } else {
                view.goBack();
                shouldGoBack = false;
            }
        } catch (IOException e) {
            Log.e("CalendarNewDateP", "exception");
        }
    }

    @Override
    public void onResponseUpdateMeeting() {
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        meetingsDb.updateMeeting(meetingId, meetingUpdated.getDescription(),
                meetingUpdated.getDate(), meetingUpdated.getDuration(), meetingUpdated.getInviteTo(),
                userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);
        if (view != null) view.hideWaitDialog();
        if (view != null) view.onMeetingCreatedOrUpdated();
        showingWaitDialog = false;
        if (view == null) {
            shouldGoBack = true;
        } else {
            view.goBack();
            shouldGoBack = false;
        }
    }

    @Override
    public void onFailureSendMeeting(Object error) {
        this.error = error;
        if (view != null) view.hideWaitDialog();
        if (view != null) view.showError(error);
        showingErrorDialog = true;
        showingWaitDialog = false;
    }

    @Override
    public ArrayList<Integer> getContactIds() {
        return contactIds;
    }

    @Override
    public void removeContact(int id) {
        contactIds.remove((Integer)id);
    }

    public void loadContactPicture(int contactId) {
        String accessToken = new UserPreferences().getAccessToken();

        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener, String.valueOf(contactId));
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(accessToken);
    }

    @Override
    public void onResponseGetUserPhotoRequest(final Uri photo, String userID, final int viewID, int contactType) {
        new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath(photo.getPath());
                if (view != null) view.notifyContactChange();
                break;
            }
        }
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath("placeholder");
                if (view != null) view.notifyContactChange();
                break;
            }
        }
    }

}
