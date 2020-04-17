package cat.bcn.vincles.mobile.UI.Calendar;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestSendModel;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMeetingUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendMeetingRequest;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CalendarDateDetailPresenter extends Fragment implements
        CalendarDateDetailPresenterContract, GetUserPhotoRequest.OnResponse, GetMeetingUserPhotoRequest.OnResponse {


    BaseRequest.RenewTokenFailed listener;
    CalendarDateDetailFragmentView view;
    MeetingRealm meeting;
    int meetingId;
    int hostId;
    MeetingRestSendModel meetingUpdated;
    UserPreferences userPreferences;

    ArrayList<Integer> contactIds;
    ArrayList<Contact> contacts;
    String creatorName, creatorPath;

    UsersDb usersDb;

    Object error;

    public CalendarDateDetailPresenter(){
        contactIds = new ArrayList<>();
        usersDb = new UsersDb(getContext());
    }

    public static CalendarDateDetailPresenter newInstance(BaseRequest.RenewTokenFailed listener,
                                                          CalendarDateDetailFragmentView view,
                                                          Bundle savedInstanceState) {
        CalendarDateDetailPresenter fragment = new CalendarDateDetailPresenter();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setExternalVars(listener, view, savedInstanceState);

        return fragment;
    }


    public void setExternalVars(BaseRequest.RenewTokenFailed listener,
                                CalendarDateDetailFragmentView view,
                                Bundle savedInstanceState) {
        this.listener = listener;
        this.view = view;
        userPreferences = new UserPreferences(MyApplication.getAppContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        view = null;
    }


    @Override
    public void onCreateView() {
    }


    @Override
    public void loadMeeting(int meetingId) {
        this.meetingId = meetingId;
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        meeting = meetingsDb.findMeeting(meetingId);
        hostId = meeting.getHostId();
        contactIds = new ArrayList<>(meeting.getGuestIDs());

        if (meeting.getHostId() == userPreferences.getUserID()) {
            creatorName = null;
            creatorPath = userPreferences.getUserAvatar();
        } else {
            Dynamizer dynamizer = new UserGroupsDb(MyApplication.getAppContext())
                    .findDynamizerUnmanaged(meeting.getHostId());
            if (dynamizer != null) {
                creatorName = dynamizer.getName() + " " + dynamizer.getLastname();
                creatorPath = dynamizer.getPhoto();
            } else {
                GetUser host = usersDb.findUserUnmanaged(meeting.getHostId());
                creatorName = host.getName() + " " + host.getLastname();
                creatorPath = host.getPhoto();
            }

        }

        if (creatorPath == null || creatorPath.length() == 0) {
            loadCreatorPicture();
        }

        contacts = getContactList();
        if (view != null) view.showMeeting(meeting, contacts, creatorName, creatorPath);
    }

    private ArrayList<Contact> getContactList() {
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        UserGroupsDb groupsDb = new UserGroupsDb(MyApplication.getAppContext());
        ArrayList<Contact> contactsList = new ArrayList<>();
        for (int id : contactIds) {
            Dynamizer dynamizer = groupsDb.findDynamizerUnmanaged(id);
            if (dynamizer != null) {
                contactsList.add(createContact(dynamizer, getGuestState(id)));
            } else {
                GetUser user = usersDb.findUserUnmanaged(id);
                contactsList.add(createContact(user, getGuestState(id)));
            }
        }
        return contactsList;
    }

    private String getGuestState(int id) {
        for (int i = 0; i<meeting.getGuestIDs().size(); i++) {
            if (id == meeting.getGuestIDs().get(i)) {
                return meeting.getGuestStates().get(i);
            }
        }
        return "";
    }

    private Contact createContact(GetUser getUser, String state) {
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setState(state);
        contact.setIdContentPhoto(getUser.getIdContentPhoto() == null ? 0
                : getUser.getIdContentPhoto());
        contact.setPath(getUser.getPhoto());

        return contact;
    }

    private Contact createContact(Dynamizer dynamizer, String state) {
        Contact contact = new Contact();
        contact.setId(dynamizer.getId());
        contact.setName(dynamizer.getName());
        contact.setLastname(dynamizer.getLastname());
        contact.setState(state);
        contact.setIdContentPhoto(dynamizer.getIdContentPhoto());
        contact.setPath(dynamizer.getPhoto());

        return contact;
    }


    @Override
    public void stopedShowingErrorDialog() {

    }

    @Override
    public void loadContactPicture(int contactId) {
        GetMeetingUserPhotoRequest getMeetingUserPhotoRequest = new GetMeetingUserPhotoRequest(
                listener, meetingId, contactId+"");
        getMeetingUserPhotoRequest.addOnOnResponse(this);
        getMeetingUserPhotoRequest.doRequest(userPreferences.getAccessToken());
    }

    @Override
    public void loadCreatorPicture() {
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener,
                ""+meeting.getHostId());
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(userPreferences.getAccessToken());
    }

    public ArrayList<Integer> getContactIds() {
        return contactIds;
    }

    @Override
    public void onResponseGetUserPhotoRequest(final Uri photo, String userID, final int viewID, int contactType) {
        new UserGroupsDb(MyApplication.getAppContext()).setGroupDynamizerAvatarPath(Integer.parseInt(userID), photo.getPath());
        new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());

        if (view != null && Integer.parseInt(userID) == hostId) {
            view.updateHostImage(photo.getPath());
        } else if (view != null) {

        }
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        if (view != null) view.updateHostImage("placeholder");
    }

    @Override
    public void onResponseGetMeetingUserPhotoRequest(Uri photo, String userID) {
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
    public void onFailureGetMeetingUserPhotoRequest(Object error, String userID) {
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath("placeholder");
                if (view != null) view.notifyContactChange();
                break;
            }
        }
    }
}
