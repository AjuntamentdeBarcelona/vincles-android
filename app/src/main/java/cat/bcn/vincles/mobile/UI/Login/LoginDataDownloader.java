package cat.bcn.vincles.mobile.UI.Login;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Business.Firebase.FirebaseInstanceIDListener;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GuestRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.GetAuthenticatedUserDataRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetCircleUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentsRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupLastAccessRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessagesRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupUserListRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMeetingsRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetServerTimeRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserCircleRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserGroupsRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserMessagesRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.MigrationPostLogin;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class LoginDataDownloader extends Fragment implements GetAuthenticatedUserDataRequest.OnResponse,
        GetUserPhotoRequest.OnResponse, GetGalleryContentsRequest.OnResponse,
        GetGroupUserListRequest.OnResponse, GetUserGroupsRequest.OnResponse,
        GetCircleUserRequest.OnResponse, GetUserCircleRequest.OnResponse,
        GetGroupMessagesRequest.OnResponse, GetUserMessagesRequest.OnResponse,
        GetMeetingsRequest.OnResponse, GetGroupLastAccessRequest.OnResponse,
        MigrationPostLogin.OnResponse, GetServerTimeRequest.OnServerTimeDoneListener {

    private boolean isDone = false;

    private UserPreferences userPreferences;
    private OnResult listener;

    ArrayList<Integer> requestedGroupChats = new ArrayList<>();
    ArrayList<Integer> requestedGroupUserList = new ArrayList<>();
    ArrayList<Integer> requestedUserChats = new ArrayList<>();

    boolean userPhotoRequested = false;
    boolean photosInfoRequested = false;
    boolean circleUserRequested = false;
    boolean userCircleRequested = false;
    boolean groupsRequested = false;
    boolean meetingsRequested = false;
    boolean getServerTimeRequested = false;

    Object pendingError;
    boolean pendingFinish = false;

    long timeOfEarliestMeeting = System.currentTimeMillis();
    boolean shouldKeepDownloading = timeOfEarliestMeeting > System.currentTimeMillis();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    public void initUserDataDownloader(UserPreferences userPreferences, OnResult listener) {
        this.userPreferences = userPreferences;
        this.listener = listener;

        if (pendingError != null) {
            listener.onError(pendingError);
        } else if (pendingFinish) listener.onFinished();
    }

    private void onRequestsCompleted() {
        if (userPhotoRequested && photosInfoRequested && groupsRequested && circleUserRequested
                && userCircleRequested && meetingsRequested && getServerTimeRequested) {
            isDone = true;

            //added last step. When all requests work and it is ready to login, we do this last one
            MigrationPostLogin migrationPostLogin = new MigrationPostLogin();
            migrationPostLogin.addOnOnResponse(this);
            migrationPostLogin.doRequest(userPreferences.getAccessToken());
        }
    }

    @Override
    public void onResponseMigrationPostLogin() {
        if (listener == null) {
            pendingFinish = true;
        } else {
            listener.onFinished();
        }
    }

    @Override
    public void onFailureMigrationPostLogin(Object error) {
        onError(error);
    }

    void doRequests() {
        GetAuthenticatedUserDataRequest getAuthenticatedUserDataRequest = new GetAuthenticatedUserDataRequest();
        getAuthenticatedUserDataRequest.addOnOnResponse(this);
        getAuthenticatedUserDataRequest.doRequest(userPreferences.getAccessToken());
    }

    @Override
    public void onResponseGetAuthenticatedUserDataRequest(GetUser userRegister) {
        userPreferences.setUserID(userRegister.getId());
        userPreferences.setIdInstallation(userRegister.getIdInstallation());
        userPreferences.setIdCircle(userRegister.getIdCircle());
        userPreferences.setIdLibrary(userRegister.getIdLibrary());
        userPreferences.setIdCalendar(userRegister.getIdCalendar());
        userPreferences.setAlias(userRegister.getAlias());
        userPreferences.setUsername(userRegister.getUsername());
        userPreferences.setName(userRegister.getName());
        userPreferences.setLastName(userRegister.getLastname());
        userPreferences.setEmail(userRegister.getEmail());
        userPreferences.setBirthdate(userRegister.getBirthdate());
        userPreferences.setPhone(userRegister.getPhone());
        userPreferences.setGender(userRegister.getGender());
        userPreferences.setLivesInBarcelona(userRegister.getLiveInBarcelona());
        userPreferences.setIsUserSenior(userRegister.getIdCircle()!= -1);

        FirebaseInstanceIDListener.forceRefreshToken(MyApplication.getAppContext());

        doParallelRequests();
    }

    private void doParallelRequests() {

        //request user photo
        String accessToken = userPreferences.getAccessToken();
        String userID = String.valueOf(userPreferences.getUserID());
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(null, userID);
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(accessToken);

        //request gallery photos
        requestPhotosInfo();

        String token = userPreferences.getAccessToken();
        if (userPreferences.getIsUserSenior()) {
            userCircleRequested = true;

            //request groups
            GetUserGroupsRequest getUserGroupsRequest = new GetUserGroupsRequest(null, token);
            getUserGroupsRequest.addOnOnResponse(this);
            getUserGroupsRequest.doRequest();

            //request CircleUser
            GetCircleUserRequest getCircleUserRequest = new GetCircleUserRequest(null);
            getCircleUserRequest.addOnOnResponse(this);
            getCircleUserRequest.doRequest(userPreferences.getAccessToken());

        } else {
            groupsRequested = true;
            circleUserRequested = true;

            //request UserCircle
            GetUserCircleRequest getUserCircleRequest = new GetUserCircleRequest(null,
                    userPreferences.getAccessToken());
            getUserCircleRequest.addOnOnResponse(this);
            getUserCircleRequest.doRequest();
        }

        //request meetings
        GetMeetingsRequest getMeetingsRequest = new GetMeetingsRequest(null,
                0, 0);
        getMeetingsRequest.addOnOnResponse(this);
        getMeetingsRequest.doRequest(userPreferences.getAccessToken());

        //request server time
        GetServerTimeRequest getServerTimeRequest = new GetServerTimeRequest( this);
        getServerTimeRequest.execute();

    }

    @Override
    public void onFailureGetAuthenticatedUserDataRequest(Object error) {
        Log.d("logdown","onFailureGetAuthenticatedUserDataRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        if (isDone) return;
        //Bitmap bm = BitmapFactory.decodeStream(stream);

        //Uri uriImage = imageUtils.getImageUri(this,bm);
        userPreferences.setUserAvatar(photo.toString());

        userPhotoRequested = true;
        onRequestsCompleted();
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        Log.d("logdown","onFailureGetUserPhotoRequest ERROR: "+error);
        onError(error);
    }

    private void requestPhotosInfo() {
        String accessToken = userPreferences.getAccessToken();
        GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(null,
                System.currentTimeMillis());
        getContentRequest.addOnOnResponse(this);
        getContentRequest.doRequest(accessToken);
    }

    @Override
    public void onResponseGetGalleryContentsRequest(ArrayList<GalleryContentRealm> galleryContentList) {
        if (isDone) return;

        Log.d("lgn","onResponseGetGalleryContentsRequest ");
        GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        if (galleryContentList.size() > 0) {
            galleryDb.insertMultipleContent(galleryContentList);
            /*for (GalleryContentRealm galleryContent :galleryContentList) {
                if (!galleryDb.existsContentById(galleryContent.getId())) {

                       Realm realm = Realm.getDefaultInstance();

                    galleryDb.insertContent(new GalleryContentRealm(galleryContent.getId(),
                            galleryContent.getIdContent(), galleryContent.getMimeType(),
                            galleryContent.getUser().getId(), galleryContent.getInclusionTime()));

                    GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", galleryContent.getUser().getId()).findFirst();

                    if(getUserRealm == null){
                        Log.d("room id", String.valueOf(galleryContent.getUser().getId()));
                        usersDb.saveGetUser(galleryContent.getUser(), true);
                    }
                }

            }*/
        }
        photosInfoRequested = true;
        onRequestsCompleted();
    }

    @Override
    public void onFailureGetGalleryContentsRequest(Object error) {
        Log.d("logdown","onFailureGetGalleryContentsRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetUserGroupsRequest(ArrayList<UserGroup> userGroups) {
        if (isDone) return;

        new UserGroupsDb(MyApplication.getAppContext()).saveCurrentUsersGroups(userGroups);

        if (userGroups.size() == 0) {
            groupsRequested = true;
            onRequestsCompleted();
        } else {
            for (UserGroup userGroup : userGroups) {
                requestedGroupChats.add(userGroup.getGroup().getIdChat());
                requestedGroupUserList.add(userGroup.getGroup().getIdChat());

                GetGroupUserListRequest getGroupUserListRequest = new GetGroupUserListRequest(
                        null, userPreferences.getAccessToken(),
                        String.valueOf(userGroup.getGroup().getIdGroup()));
                getGroupUserListRequest.addOnOnResponse(this);
                getGroupUserListRequest.doRequest();

                GetGroupLastAccessRequest getGroupLastAccessRequest = new GetGroupLastAccessRequest(
                        null, String.valueOf(userGroup.getGroup().getIdChat()));
                getGroupLastAccessRequest.addOnOnResponse(this);
                getGroupLastAccessRequest.doRequest(new UserPreferences().getAccessToken());

            }
        }


    }

    @Override
    public void onFailureGetUserGroupsRequest(Object error) {
        Log.d("logdown","onFailureGetUserGroupsRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetGroupUserListRequest(ArrayList<GetUser> userList, String groupID) {
        if (isDone) return;


        Context context = MyApplication.getAppContext();
        ArrayList<Integer> ids = new ArrayList<>();
        UsersDb usersDb = new UsersDb(context);
        for (GetUser user : userList) {
            ids.add(user.getId());
        }
        usersDb.saveGetUserListIfNotExists(userList);
        UserGroupsDb userGroupsDb = new UserGroupsDb(context);
        userGroupsDb.setUserGroupUsersList(Integer.parseInt(groupID), ids);
        Log.d("gdp","login download group list, size:"+ids.size()+" missing size:"+requestedGroupUserList.size());

        requestedGroupUserList.remove(0);
        if (requestedGroupChats.size() == 0 && requestedGroupUserList.size() == 0) {
            groupsRequested = true;
            onRequestsCompleted();
        }
    }

    @Override
    public void onFailureGetGroupUserListRequest(Object error) {
        Log.d("logdown","onFailureGetGroupUserListRequest ERROR: "+error);
        onError(error);
    }

    private void onUserObtained(int userId) {
        requestedUserChats.add(userId);

        GetUserMessagesRequest getUserMessagesRequest = new GetUserMessagesRequest(null,
                String.valueOf(userId), 0, 0);
        getUserMessagesRequest.addOnOnResponse(this);
        getUserMessagesRequest.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void onResponseGetCircleUserRequest(ArrayList<CircleUser> circleUsers) {
        if (isDone) return;
        new UsersDb(MyApplication.getAppContext()).saveCircleUsers(circleUsers);

        if (circleUsers.size() == 0) {
            circleUserRequested = true;
            userCircleRequested = true;
            onRequestsCompleted();
        } else {
            for (CircleUser circleUser : circleUsers) {
                onUserObtained(circleUser.getUser().getId());
            }
        }
    }

    @Override
    public void onFailureGetCircleUserRequest(Object error) {
        Log.d("logdown","onFailureGetCircleUserRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetUserCircleRequest(ArrayList<UserCircle> userCircles) {
        if (isDone) return;
        new UsersDb(MyApplication.getAppContext()).saveUserCircles(userCircles);

        if (userCircles.size() == 0) {
            circleUserRequested = true;
            userCircleRequested = true;
            onRequestsCompleted();
        } else {
            for (UserCircle userCircle : userCircles) {
                onUserObtained(userCircle.getCircle().getUser().getId());
            }
        }

    }

    @Override
    public void onFailureGetUserCircleRequest(Object error) {
        Log.d("logdown","onFailureGetUserCircleRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetGroupMessagesRequest(ArrayList<GroupMessageRest> groupMessageRestList,
                                                  String idChat) {
        if (isDone) return;
        //List<GroupMessageRest> messages = groupMessageRestList;
        if (groupMessageRestList != null && groupMessageRestList.size() > 0) {
            GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
            groupMessageDb.saveGroupMessageRestList(groupMessageRestList);

            OtherUtils.updateGroupOrDynChatInfo(groupMessageRestList
                    .get(0).getIdChat());
        }

        requestedGroupChats.remove((Integer)Integer.parseInt(idChat));
        if (requestedGroupChats.size() == 0 && requestedGroupUserList.size() == 0) {
            groupsRequested = true;
            onRequestsCompleted();
        }
    }

    @Override
    public void onResponseGetGroupLastAccessRequest(long date, String idChat) {
        Log.d("grpwatch","on response watched, date:"+date+" idChat:"+idChat);
        new UserGroupsDb(MyApplication.getAppContext()).setGroupLastAccess(Integer.parseInt(idChat), date);

        GetGroupMessagesRequest getGroupMessagesRequest = new GetGroupMessagesRequest(
                null, String.valueOf(idChat), 0,0);
        getGroupMessagesRequest.addOnOnResponse(this);
        getGroupMessagesRequest.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void onFailureGetGroupLastAccessRequest(Object error) {
        onError(error);
    }

    @Override
    public void onFailureGetGroupMessagesRequest(Object error) {
        Log.d("logdown","onFailureGetGroupMessagesRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onResponseGetUserMessagesRequest(ArrayList<ChatMessageRest> messages,
                                                 String idUserSender) {
        if (isDone) return;
        if (messages != null && messages.size() > 0) {
            UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
            userMessageDb.saveChatMessageRestList(messages);


            int idMe = new UserPreferences().getUserID();
            int userId = Integer.parseInt(idUserSender);
            Context context = MyApplication.getAppContext();
            userMessageDb = new UserMessageDb(context);
            new UsersDb(context).setMessagesInfo(userId, userMessageDb.getNumberUnreadMessagesReceived(idMe, userId),
                    0,
                    Math.max(userMessageDb.getLastMessage(new UserPreferences().getUserID(), userId),
                            new NotificationsDb(MyApplication.getAppContext())
                                    .getLastMissedCallTime(userId)));
            Log.d("unrd","Login, user:"+userId+" totalNum:"
                    +userMessageDb.getTotalNumberMessages(idMe, userId));

        }

        requestedUserChats.remove((Integer)Integer.parseInt(idUserSender));
        if (requestedUserChats.size() == 0) {
            circleUserRequested = true;
            userCircleRequested = true;
            onRequestsCompleted();
        }
    }

    @Override
    public void onFailureGetUserMessagesRequest(Object error, String idUserSender) {
        Log.d("logdown","onFailureGetUserMessagesRequest ERROR: "+error);
        if (error instanceof String && error.equals("1101")) {
            new UsersDb(MyApplication.getAppContext()).deleteUserCircleIfExists(
                    Integer.parseInt(idUserSender));

            requestedUserChats.remove((Integer)Integer.parseInt(idUserSender));
            if (requestedUserChats.size() == 0) {
                circleUserRequested = true;
                userCircleRequested = true;
                onRequestsCompleted();
            }
        } else {
            onError(error);
        }
    }

    private void onError(Object error) {
        if (!isDone) {
            isDone = true;
            if (listener == null) {
                pendingError = error;
            } else {
                listener.onError(error);
            }
        }
    }

    @Override
    public void onResponseGetMeetingsRequest(ArrayList<MeetingRest> meetingRestList) {
        shouldKeepDownloading = timeOfEarliestMeeting > System.currentTimeMillis();

        if (meetingRestList != null) {
            UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
           // List<MeetingRest> meetingRests = meetingRestList.getChatMessageRestList();
            for (MeetingRest meetingRest : meetingRestList) {

                //save users missing in db
                usersDb.saveGetUserIfNotExists(meetingRest.getHost());
                if (meetingRest.getGuests() != null) {
                    ArrayList<GetUser> usersGuests = new ArrayList<>();
                    for (GuestRest guest : meetingRest.getGuests()) {
                        GetUser us = guest.getUser();
                        if (us.getIdCircle()==null)us.setIdCircle(-1);
                        usersGuests.add(us);
                        //usersDb.saveGetUserIfNotExists(guest.getUser());
                    }
                    usersDb.saveGetUserListIfNotExists(usersGuests);
                }
            }

            // Get the date for the most recent message, only if there are messages
            int chatMessageRestListSize = meetingRestList.size();
            if (chatMessageRestListSize > 0) {
                timeOfEarliestMeeting = meetingRestList.get(chatMessageRestListSize-1).getDate();
            }

            MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
            meetingsDb.saveMeetingsRestList(meetingRestList,
                    userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);
        }

        if (shouldKeepDownloading) {
            GetMeetingsRequest getMeetingsRequest = new GetMeetingsRequest(null,
                    0, timeOfEarliestMeeting);
            getMeetingsRequest.addOnOnResponse(this);
            getMeetingsRequest.doRequest(userPreferences.getAccessToken());
        } else {
            meetingsRequested = true;
            onRequestsCompleted();
        }


    }

    @Override
    public void onFailureGetMeetingsRequest(Object error) {
        Log.d("logdown","onFailureGetMeetingsRequest ERROR: "+error);
        onError(error);
    }

    @Override
    public void onServerTimeDone(long serverTime) {
        userPreferences.setLastDownloadedNotification(serverTime);
        userPreferences.saveServerTime(serverTime);
        getServerTimeRequested = true;
        onRequestsCompleted();

    }

    @Override
    public void onServerTimeError() {
        userPreferences.setLastDownloadedNotification(System.currentTimeMillis());
        getServerTimeRequested = true;
        onRequestsCompleted();

    }

    interface OnResult {
        void onError(Object error);
        void onFinished();
    }


}
