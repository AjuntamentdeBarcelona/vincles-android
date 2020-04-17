package cat.bcn.vincles.mobile.Client.Business;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.VinclesError;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GuestRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetCircleUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetContentInMyGallery;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessagesRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupUserListRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMeetingRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetPublicUserDataRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetServerTimeRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserDataRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserGroupsRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivity;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivityView;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import cat.bcn.vincles.mobile.Utils.RequestsUtils;

import static cat.bcn.vincles.mobile.Utils.MyApplication.getAppContext;

public class NotificationProcessor implements GetMessageRequest.OnResponse, GetGroupMessageRequest.OnResponse, GetUserDataRequest.OnResponse,
        GetCircleUserRequest.OnResponse, GetUserGroupsRequest.OnResponse, GetGroupUserListRequest.OnResponse, GetGroupMessagesRequest.OnResponse,
        GetMeetingRequest.OnResponse, GetContentInMyGallery.OnResponse, GetPublicUserDataRequest.OnResponse, GetServerTimeRequest.OnServerTimeDoneListener {

    private final static String CHANNEL_ID_MESSAGE = "CHANNEL_ID_MESSAGE";
    private final static String CHANNEL_ID_OTHER = "CHANNEL_ID_OTHER";
    public final static String CALL_ERROR_BROADCAST = "call_error_broadcast";

    private boolean cancelledProcessing = false;

    private UserPreferences userPreferences;
    private NotificationsDb notificationsDb;
    private BaseRequest.RenewTokenFailed listener;
    private int notificationId;
    private String type;

    private int messageId;
    private int idUser;
    private int idGroup;
    private int chatId;
    private int idMeeting;
    private String idRoom;
    private long notificationTime;
    private String code;
    private int idGalleryContent;

    private NotificationProcessed callback;

    //to process requests in parallel
    private boolean groupUserListFinished = false;
    private boolean groupMessagesFinished = false;

    private boolean incommingCallIsMissed = false;

    private ArrayList<Integer> downloadedContent = new ArrayList<>();
    private Bundle data;

    private Resources resources;

    NotificationProcessor(int notificationId, String type, Bundle data, NotificationsDb notificationsDb,
                          BaseRequest.RenewTokenFailed listener,
                          NotificationProcessed callback, Resources resources) {
        userPreferences = new UserPreferences();
        this.notificationId = notificationId;
        this.notificationsDb = notificationsDb;
        this.listener = listener;
        this.type = type;
        this.callback = callback;
        this.resources = resources;
        getData(data);
        Log.d("notman","process id:"+notificationId);
    }

    private void getData(Bundle data) {
        Log.d("pronoti","notiProcessor, type: "+type);
        switch (type) {
            case "NEW_MESSAGE":
                messageId = data.getInt("idMessage");
                break;
            case "NEW_CHAT_MESSAGE":
                messageId = data.getInt("idMessage");
                chatId = data.getInt("chatId");
                break;
            case "USER_UPDATED":
            case "USER_LINKED":
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                idUser = data.getInt("idUser");
                break;
            case "ADDED_TO_GROUP":
                idGroup = data.getInt("idGroup");
                break;
            case "NEW_USER_GROUP":
                idUser = data.getInt("idUser");
                idGroup = data.getInt("idGroup");
                break;
            case "REMOVED_FROM_GROUP":
                idGroup = data.getInt("idGroup");
                break;
            case "REMOVED_USER_GROUP":
                idUser = data.getInt("idUser");
                idGroup = data.getInt("idGroup");
                break;
            case "GROUP_UPDATED":
                idGroup = data.getInt("idGroup");
                break;
            case "MEETING_ACCEPTED_EVENT":
            case "MEETING_REJECTED_EVENT":
            case "MEETING_INVITATION_DELETED_EVENT":
                idUser = data.getInt("idUser");
            case "MEETING_INVITATION_EVENT":
            case "MEETING_CHANGED_EVENT":
            case "MEETING_INVITATION_ADDED_EVENT":
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                idMeeting = data.getInt("idMeeting");
                break;
            case "INCOMING_CALL":
                Log.d("incalock","process incoming call");
                idUser = data.getInt("idUser");
                idRoom = data.getString("idRoom");
                notificationTime = data.getLong("notificationTime");
                break;
            case "ERROR_IN_CALL":
                idUser = data.getInt("idUser");
                idRoom = data.getString("idRoom");
                notificationTime = data.getLong("notificationTime");
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                code = data.getString("code");
                idUser = data.getInt("idUser");
                break;
            case "CONTENT_ADDED_TO_GALLERY":
                idGalleryContent = data.getInt("idGalleryContent");
                break;
        }
    }


    void processNotification() {
        switch (type) {
            case "NEW_MESSAGE":
                GetMessageRequest getMessageRequest = new GetMessageRequest(listener,
                        String.valueOf(messageId));
                getMessageRequest.addOnOnResponse(this);
                getMessageRequest.doRequest(new UserPreferences().getAccessToken());
                break;

            case "NEW_CHAT_MESSAGE":
                GetGroupMessageRequest getGroupMessageRequest = new GetGroupMessageRequest(listener,
                        String.valueOf(chatId), String.valueOf(messageId));
                getGroupMessageRequest.addOnOnResponse(this);
                getGroupMessageRequest.doRequest(new UserPreferences().getAccessToken());
                break;
            case "USER_UPDATED":
                GetUserDataRequest getUserDataRequest = new GetUserDataRequest(String.valueOf(idUser));
                getUserDataRequest.addOnOnResponse(this);
                getUserDataRequest.doRequest(userPreferences.getAccessToken());
                break;
            case "USER_LINKED":
                GetCircleUserRequest getCircleUserRequest = new GetCircleUserRequest(null);
                getCircleUserRequest.addOnOnResponse(this);
                getCircleUserRequest.doRequest(userPreferences.getAccessToken());
                break;
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
                GetUser getUser = usersDb.findUserUnmanaged(idUser);
                if (getUser != null) {
                    String name = getUser.getName();
                    notificationsDb.setNotificationUserName(notificationId, name);
                    usersDb.deleteUserCircleIfExists(idUser);

                    Bundle data = new Bundle();
                    data.putString("type", type);
                    data.putInt("idUser", idUser);
                    onNotificationProcessed(data);
                } else {
                    onNotificationSkipped();
                }
                break;
            case "ADDED_TO_GROUP":
                GetUserGroupsRequest getUserGroupsRequest = new GetUserGroupsRequest(null,
                        userPreferences.getAccessToken());
                getUserGroupsRequest.addOnOnResponse(this);
                getUserGroupsRequest.doRequest();
                break;
            case "NEW_USER_GROUP":
                //request user info
                GetGroupUserListRequest getGroupUserListRequest = new GetGroupUserListRequest(
                        null, userPreferences.getAccessToken(), String.valueOf(idGroup));
                getGroupUserListRequest.addOnOnResponse(this);
                getGroupUserListRequest.doRequest();
                break;
            case "REMOVED_FROM_GROUP":
                UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                notificationsDb.setNotificationUserId(notificationId, idGroup);
                GroupRealm group = userGroupsDb.getGroupUnmanaged(idGroup);
                if (group != null) {
                    int dynId = group.getIdDynamizer();
                    chatId = group.getIdChat();
                    notificationsDb.setNotificationChatInfo(notificationId, chatId, group.getName());
                    userGroupsDb.deleteGroup(idGroup);
                    onNotificationProcessed(null);

                    if (!userGroupsDb.checkIfDynamizerShouldBeShown(dynId)) {
                        userGroupsDb.setShouldShowDynamizer(dynId, false);
                    }
                } else {
                    onNotificationSkipped();
                }
                break;
            case "REMOVED_USER_GROUP":
                userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
               // if (userGroupsDb.removeUserFromGroupList(idGroup, idUser)) {
                    userGroupsDb.removeUserFromGroupList(idGroup, idUser);
                    notificationsDb.setNotificationUserId(notificationId, idGroup);
                    group = userGroupsDb.getGroupUnmanaged(idGroup);
                    notificationsDb.setNotificationChatInfo(notificationId, group.getIdChat(), group.getName());
                    onNotificationProcessed(null);
               // } else {
               //     onNotificationSkipped();
               // }
                break;
            case "GROUP_UPDATED":
                getUserGroupsRequest = new GetUserGroupsRequest(null,
                        userPreferences.getAccessToken());
                getUserGroupsRequest.addOnOnResponse(this);
                getUserGroupsRequest.doRequest();
                break;

            case "MEETING_ACCEPTED_EVENT":

            case "MEETING_REJECTED_EVENT":
            case "MEETING_INVITATION_DELETED_EVENT":
                processMeetingUserAction();
                break;
            case "MEETING_INVITATION_EVENT":
            case "MEETING_CHANGED_EVENT":
            case "MEETING_INVITATION_ADDED_EVENT":
                GetMeetingRequest getMeetingRequest = new GetMeetingRequest(null, idMeeting);
                getMeetingRequest.addOnOnResponse(this);
                getMeetingRequest.doRequest(userPreferences.getAccessToken());
                break;
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                processDeleteMeeting();
                break;
            case "INCOMING_CALL":

                Log.d("versionControlAlert", "INCOMING_CALL onMessageReceived");
                Log.d("versionControlAlert", "INCOMING_CALL continueToApp: " + String.valueOf(userPreferences.getcontinueToApp()));

                 GetServerTimeRequest getServerTimeRequest = new GetServerTimeRequest( this);
                 getServerTimeRequest.execute();

                break;
                case "ERROR_IN_CALL":
                processErrorInCall();
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                processUserInvitation();
                break;
            case "CONTENT_ADDED_TO_GALLERY":

                GetContentInMyGallery getContentInMyGallery = new GetContentInMyGallery(
                        null, idGalleryContent);
                getContentInMyGallery.addOnOnResponse(this);
                getContentInMyGallery.doRequest(userPreferences.getAccessToken());
                break;

        }
    }

    @Override
    public void onResponseGetMessageRequest(ChatMessageRest chatMessageRest) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        new UserMessageDb(MyApplication.getAppContext()).saveChatMessageRest(chatMessageRest);

        //todo download fotos and videos
        /*if (chatMessageRest.getIdAdjuntContents() != null
                && chatMessageRest.getIdAdjuntContents().size() > 0
                && !chatMessageRest.getMetadataTipus().toLowerCase().contains("audio")) {
            GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
            for (int contentId : chatMessageRest.getIdAdjuntContents()) {
                galleryDb.insertContent(new GalleryContentRealm(contentId));
            }
        }
        Log.d("qwe","adjuntContents:"+chatMessageRest.getIdAdjuntContents());*/

        data = new Bundle();
        data.putString("type", type);
        data.putLong("messageId", chatMessageRest.getId());
        data.putLong("chatId", chatMessageRest.getIdUserFrom());
        data.putString("text", chatMessageRest.getText());
        Log.d("msgnot","processed notif message:"+chatMessageRest.getText()+", id:"+chatMessageRest.getId());

        int userId = chatMessageRest.getIdUserFrom();
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        GetUser user = usersDb.findUserUnmanaged(userId);

        String name = "";
        if (user != null){
             name = user.getName() + " " + user.getLastname();
        }

        notificationsDb.setNotificationUserUserName(notificationId, userId, name);

        int idMe = new UserPreferences().getUserID();
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        usersDb.setMessagesInfo(userId, userMessageDb.getNumberUnreadMessagesReceived(idMe, userId),
                new NotificationsDb(MyApplication.getAppContext())
                        .getNumberUnreadMissedCallNotifications(userId),
                Math.max(userMessageDb.getLastMessage(new UserPreferences().getUserID(), userId),
                        new NotificationsDb(MyApplication.getAppContext())
                                .getLastMissedCallTime(userId)));


        if (downloadedContent.size() == 0) onNotificationProcessed(data);
    }

    @Override
    public void onFailureGetMessageRequest(Object error) {
        Log.d("qwe","onFailureGetMessageRequest");
        if (error instanceof String && (error.equals("403") || error.equals("409"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetContentInMyGallery(GalleryContentRealm galleryContent) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        Log.d("getMimeType", galleryContent.getMimeType());

        if(!galleryContent.getMimeType().equals("audio/aac") && !galleryContent.getMimeType().equals("audio/mp3") ){
          GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
          galleryDb.insertContent(galleryContent);

        }
      /*downloadedContent.remove(0);
      if (downloadedContent.size() == 0) onNotificationProcessed(data);*/
      onNotificationProcessed(data);
    }

    @Override
    public void onFailureGetContentInMyGallery(Object error) {
        if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetGroupMessageRequest(GroupMessageRest groupMessageRest) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        new GroupMessageDb(MyApplication.getAppContext()).saveGroupMessageRest(groupMessageRest);
        Bundle data = new Bundle();
        data.putString("type", type);
        data.putLong("messageId", groupMessageRest.getId());
        data.putLong("chatId", groupMessageRest.getIdChat());
        data.putString("text", groupMessageRest.getText());
        data.putString("metadatatipus", groupMessageRest.getMetadataTipus());
        data.putString("userSenderName", groupMessageRest.getFullNameUserSender());

        int chatId = groupMessageRest.getIdChat();
        UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
        GroupRealm groupRealm = userGroupsDb.getGroupFromIdChatUnmanaged(chatId);
        String name;
        if (groupRealm != null) {
            idGroup = groupRealm.getIdGroup();
            name = groupRealm.getName();

        } else { //its dynamizer
            Log.d("dynfa","chatId:"+chatId);
            Dynamizer dynamizer = userGroupsDb.findDynamizerFromChatIdUnmanaged(chatId);
            if (dynamizer == null) {
                onNotificationSkipped();
                return;
            }
            else name = dynamizer.getName();

        }

        OtherUtils.updateGroupOrDynChatInfo(groupMessageRest.getIdChat());

        notificationsDb.setNotificationChatInfo(notificationId, chatId, name);

        onNotificationProcessed(data);
    }

    @Override
    public void onFailureGetGroupMessageRequest(Object error) {
        if (error instanceof String && (error.equals("403") || error.equals("409")
                || error.equals("2102"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetUserDataRequest(GetUser user) {
        //Log.d("VIN-566", "NotificationProcessor.onResponseGetUserDataRequest(): START");
        if (cancelledProcessing) onCancelledProcessingFinish();

        //save user
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.saveGetUser(user, true);
        usersDb.setPathAvatarToUser(user.getId(), "");

        //add user ID to group list of user IDs
        Bundle data = new Bundle();
        data.putString("type", type);
        data.putInt("idUser", idUser);
        onNotificationProcessed(data);
        //Log.d("VIN-566", "NotificationProcessor.onResponseGetUserDataRequest(): END");
    }

    @Override
    public void onFailureGetUserDataRequest(VinclesError error) {
        //Log.d("VIN-566", "NotificationProcessor.onFailureGetUserDataRequest(): START");
        // Check the specific errors (errors wrapped inside the JSON in the HTTP response) first
        if ("1002".equals(error.getCode())) {
            //Log.d("VIN-566", String.format("NotificationProcessor.onFailureGetUserDataRequest(): error %s, exec GetPublicUserDataRequest", error.getCode()));
            GetPublicUserDataRequest getPublicUserDataRequest = new GetPublicUserDataRequest(String.valueOf(idUser));
            getPublicUserDataRequest.addOnOnResponse(this);
            getPublicUserDataRequest.doRequest(userPreferences.getAccessToken());
        // Check the HTTP status code
        } else if (error.getHttpStatus() == 403 || error.getHttpStatus() == 409) {
            //Log.d("VIN-566", String.format("NotificationProcessor.onFailureGetUserDataRequest(): error %s, skip notification", error.getHttpStatus()));
            onNotificationSkipped();
        // If no specific error or HTTP status error
        } else if (callback != null) {
            callback.onNotificationFailure();
        }
        //Log.d("VIN-566", "NotificationProcessor.onFailureGetUserDataRequest(): END");
    }

    @Override
    public void onResponseGetPublicUserDataRequest(GetUser user) {
        //Log.d("VIN-566", "NotificationProcessor.onResponseGetPublicUserDataRequest(): START");
        if (cancelledProcessing) onCancelledProcessingFinish();

        //save user
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.updateGetUserWithPublicInfo(user);

        //add user ID to group list of user IDs
        Bundle data = new Bundle();
        data.putString("type", type);
        data.putInt("idUser", idUser);
        onNotificationProcessed(data);
        //Log.d("VIN-566", "NotificationProcessor.onResponseGetPublicUserDataRequest(): END");
    }

    @Override
    public void onFailureGetPublicUserDataRequest(VinclesError error) {
        //Log.d("VIN-566", "NotificationProcessor.onFailureGetPublicUserDataRequest(): START");
        // Check the HTTP status code
        if (error.getHttpStatus() == 403 || error.getHttpStatus() == 409) {
            //Log.d("VIN-566", String.format("NotificationProcessor.onFailureGetPublicUserDataRequest(): error %s, skip notification", error.getHttpStatus()));
            onNotificationSkipped();
        // If no HTTP status error
        } else if (callback != null) callback.onNotificationFailure();
        //Log.d("VIN-566", "NotificationProcessor.onFailureGetPublicUserDataRequest(): END");
    }

    @Override
    public void onResponseGetCircleUserRequest(ArrayList<CircleUser> circleUsers) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        new UsersDb(MyApplication.getAppContext()).saveCircleUsers(circleUsers);
        Bundle data = new Bundle();
        data.putString("type", type);
        onNotificationProcessed(data);
    }

    @Override
    public void onFailureGetCircleUserRequest(Object error) {
        if (error instanceof String && (error.equals("403") || error.equals("409"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetUserGroupsRequest(ArrayList<UserGroup> userGroups) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        notificationsDb.setNotificationUserId(notificationId, idGroup);

        for (UserGroup userGroup : userGroups) {
            if (userGroup.getGroup().getIdGroup() == idGroup) {
                notificationsDb.setNotificationChatInfoGroup(notificationId,
                        userGroup.getGroup().getIdChat(), userGroup.getGroup().getName());

                UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                userGroupsDb.addOrUpdateUserGroup(userGroup);

                if (type.equals("ADDED_TO_GROUP")) {
                    GetGroupUserListRequest getGroupUserListRequest = new GetGroupUserListRequest(
                            null, userPreferences.getAccessToken(),
                            String.valueOf(userGroup.getGroup().getIdGroup()));
                    getGroupUserListRequest.addOnOnResponse(this);
                    getGroupUserListRequest.doRequest();

                    chatId = userGroup.getGroup().getIdChat();
                    GetGroupMessagesRequest getGroupMessagesRequest = new GetGroupMessagesRequest(
                            null, String.valueOf(chatId), 0,0);
                    getGroupMessagesRequest.addOnOnResponse(this);
                    getGroupMessagesRequest.doRequest(new UserPreferences().getAccessToken());
                } else if (type.equals("GROUP_UPDATED")) {
                   Bundle bundle = new Bundle();
                    bundle.putString("type", type);
                    bundle.putInt("idGroup", idGroup);
                    onNotificationProcessed(bundle);

                    GetGroupUserListRequest getGroupUserListRequest = new GetGroupUserListRequest(
                            null, userPreferences.getAccessToken(),
                            String.valueOf(idGroup));
                    getGroupUserListRequest.addOnOnResponse(this);
                    getGroupUserListRequest.doRequest();


                }
                return;
            }
        }

        onNotificationSkipped();

    }

    @Override
    public void onFailureGetUserGroupsRequest(Object error) {
        if (error instanceof String && (error.equals("403") || error.equals("409"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetGroupUserListRequest(ArrayList<GetUser> userList, String groupID) {
        if (cancelledProcessing) onCancelledProcessingFinish();
        Context context = MyApplication.getAppContext();

        if (type.equals("NEW_USER_GROUP")) {
            for (GetUser user : userList) {
                if (user.getId() == idUser) {
                    UsersDb usersDb = new UsersDb(context);
                    usersDb.saveGetUser(user, true);
                    UserGroupsDb userGroupsDb = new UserGroupsDb(context);
                    userGroupsDb.addUserToGroupList(Integer.parseInt(groupID), idUser);
                    onNotificationProcessed(null);
                    break;
                }
            }
        } else {
            ArrayList<Integer> ids = new ArrayList<>();
            UsersDb usersDb = new UsersDb(context);
            for (GetUser user : userList) {
                ids.add(user.getId());
                usersDb.saveGetUserIfNotExists(user);
            }
            UserGroupsDb userGroupsDb = new UserGroupsDb(context);
            userGroupsDb.setUserGroupUsersList(Integer.parseInt(groupID), ids);

            groupUserListFinished = true;
            if (groupMessagesFinished) {
                onNotificationProcessed(null);
            }
        }


    }

    @Override
    public void onFailureGetGroupUserListRequest(Object error) {
        if (error instanceof String && (error.equals("403") || error.equals("409"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    @Override
    public void onResponseGetGroupMessagesRequest(ArrayList<GroupMessageRest> groupMessageRestList, String idChat) {
        if (cancelledProcessing) onCancelledProcessingFinish();

       // List<GroupMessageRest> messages = groupMessageRestList.getChatMessageRestList();
        if (groupMessageRestList != null && groupMessageRestList.size() > 0) {
            GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
            groupMessageDb.saveGroupMessageRestList(groupMessageRestList);
        }

        groupMessagesFinished = true;
        if (groupUserListFinished ) {
            onNotificationProcessed(null);
        }
    }

    @Override
    public void onFailureGetGroupMessagesRequest(Object error) {
        if (callback != null) callback.onNotificationFailure();
    }

    private void processMeetingUserAction() {
        String state = MeetingsDb.USER_ACCEPTED;
        if (type.equals("MEETING_REJECTED_EVENT")) {
            state = MeetingsDb.USER_REJECTED;
        } else if (type.equals("MEETING_INVITATION_DELETED_EVENT")) {
            state = MeetingsDb.USER_DELETED;
        }
        new MeetingsDb(MyApplication.getAppContext())
                .setMeetingUserState(idMeeting, idUser, state);

            if (type.equals("MEETING_ACCEPTED_EVENT") || type.equals("MEETING_REJECTED_EVENT") ) {
                onNotificationSkipped();

            }
            else {
                onNotificationProcessed(null);

            }



    }

    private void processDeleteMeeting() {
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        /*MeetingRealm meetingRealm = meetingsDb.findMeeting(idMeeting);
        notificationsDb.setNotificationMeetingHostDate(messageId, meetingRealm.getHostId(),
                meetingRealm.getDate());*/
        MeetingRealm meetingRealm = meetingsDb.findMeeting(idMeeting);
        if (meetingRealm != null) {
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putInt("hostId", meetingRealm.getHostId());
            notificationsDb.setNotificationUserId(notificationId, meetingRealm.getHostId());
            meetingsDb.deleteMeeting(idMeeting,
                    userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);
            onNotificationProcessed(bundle);
        } else {
            onNotificationSkipped();
        }
    }

    private void processIncomingCall() {
        RequestsUtils.getInstance().cancelGalleryCalls();

        Log.d("callvid", "notif proces processIncomingCall");
        Log.d("incalock","notiProcessor processincomingcall()");

            UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
            GetUser getUser = usersDb.findUserUnmanaged(idUser);
             Dynamizer dinam = usersDb.findDynamizerUnmanaged(idUser);

             if (getUser != null) {
                Log.d("callvid", "getUser");
                Intent intent = new Intent(MyApplication.getAppContext(), CallsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(CallsActivity.EXTRAS_CALL_MODE, CallsActivityView.RECEIVING_CALL);
                bundle.putInt(CallsActivity.EXTRAS_USER_ID, idUser);
                bundle.putString(CallsActivity.EXTRAS_ID_ROOM, idRoom);
                bundle.putString(CallsActivity.EXTRAS_USER_NAME, getUser.getName());
                bundle.putString(CallsActivity.EXTRAS_USER_LASTNAME, getUser.getLastname());
                bundle.putBoolean(CallsActivity.EXTRAS_USER_IS_VINCLES, getUser.getIdCircle() == null || getUser.getIdCircle() != -1);
                bundle.putString(CallsActivity.EXTRAS_USER_AVATAR_PATH, getUser.getPhoto());
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Log.d("callvid","context " +  String.valueOf(MyApplication.getAppContext()));


                 if (((MyApplication)getAppContext()).callsActivity != null && !((MyApplication)getAppContext()).callsActivity.callsManager.isCalling){
                     Log.d("callvid","isShowingCall ");
                     ((MyApplication)getAppContext()).callsActivity.finish();
                 }

                 MyApplication.getAppContext().startActivity(intent);
                Log.d("callvid","process start activity");
                onNotificationProcessed(null);
            }
        else if (dinam != null) {
            Log.d("callvid", "getUser");
            Intent intent = new Intent(MyApplication.getAppContext(), CallsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(CallsActivity.EXTRAS_CALL_MODE, CallsActivityView.RECEIVING_CALL);
            bundle.putInt(CallsActivity.EXTRAS_USER_ID, idUser);
            bundle.putString(CallsActivity.EXTRAS_ID_ROOM, idRoom);
            bundle.putString(CallsActivity.EXTRAS_USER_NAME, dinam.getName());
            bundle.putString(CallsActivity.EXTRAS_USER_LASTNAME, dinam.getLastname());
            bundle.putBoolean(CallsActivity.EXTRAS_USER_IS_VINCLES, false);
            bundle.putString(CallsActivity.EXTRAS_USER_AVATAR_PATH, dinam.getPhoto());
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Log.d("callvid","context " +  String.valueOf(MyApplication.getAppContext()));

            MyApplication.getAppContext().startActivity(intent);
            Log.d("callvid","process start activity");
            onNotificationProcessed(null);
        }else {
                Log.d("callvid", "getUser null");
                onNotificationSkipped();
            }

    }

    private void processMissedCall() {
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        if (usersDb.findUserUnmanaged(idUser) != null) {
            incommingCallIsMissed = true;
            new NotificationsDb(MyApplication.getAppContext()).saveMissedCallNotification(
                    notificationTime, idUser);
            new UsersDb(MyApplication.getAppContext()).setMessagesInfo(idUser,
                    new UserMessageDb(MyApplication.getAppContext())
                            .getNumberUnreadMessagesReceived(new UserPreferences().getUserID(),
                                    idUser),
                    new NotificationsDb(MyApplication.getAppContext())
                            .getNumberUnreadMissedCallNotifications(idUser),
                    Math.max(new UserMessageDb(MyApplication.getAppContext()).getLastMessage(
                            new UserPreferences().getUserID(), idUser),
                            new NotificationsDb(MyApplication.getAppContext())
                                    .getLastMissedCallTime(idUser)));

            onNotificationProcessed(null);
        } else {
            onNotificationSkipped();
        }
    }

    private void processErrorInCall() {
        Intent intent = new Intent(CALL_ERROR_BROADCAST);
        intent.putExtra("idRoom", idRoom);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        onNotificationProcessed(null);
    }

    private void processUserInvitation() {
        onNotificationProcessed(null);
    }

    @Override
    public void onResponseGetMeetingRequest(MeetingRest meetingRest) {
        if (cancelledProcessing) onCancelledProcessingFinish();

        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.saveGetUserIfNotExists(meetingRest.getHost());
        if (meetingRest.getGuests() != null) {
            for (GuestRest guest : meetingRest.getGuests()) {
                usersDb.saveGetUserIfNotExists(guest.getUser());
            }
        }

        notificationsDb.setNotificationUserId(notificationId, meetingRest.getHost().getId());

        new MeetingsDb(MyApplication.getAppContext()).saveMeetingRest(meetingRest,
                userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);

        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putLong("date", meetingRest.getDate());
        bundle.putInt("hostId", meetingRest.getHost().getId());
        onNotificationProcessed(bundle);
    }

    @Override
    public void onFailureGetMeetingRequest(Object error) {
        if (error instanceof String && (error.equals("403") || error.equals("409")
                || error.equals("3104") || error.equals("3106"))) {
            onNotificationSkipped();
        } else if (callback != null) callback.onNotificationFailure();
    }

    private String getUserUnreadMessages() {
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        ArrayList<ChatMessageRest> messages = userMessageDb.getUnreadMessagesReceived(
                userPreferences.getUserID(), idUser);
        Log.d("pronoti","getUserUnreadMessages, messagesSize: "+messages.size());
        StringBuilder res = new StringBuilder();
        for (ChatMessageRest chatMessageRest : messages) {
            res.append(getMessageText(chatMessageRest.getMetadataTipus(),
                    chatMessageRest.getText())).append("\n");
        }
        return res.toString();
    }

    private String getMessageText(String metadataTipus, String text) {
        if(metadataTipus == null){
            return text;
        }
        switch (metadataTipus) {
            case "TEXT_MESSAGE":
                return text;
            case "IMAGES_MESSAGE":
                return resources.getString(R.string.notifications_new_photo);
            case "VIDEO_MESSAGE":
                return resources.getString(R.string.notifications_new_video);
            case "AUDIO_MESSAGE":
                return resources.getString(R.string.notifications_new_audio);
            case "MESSAGE_MULTI": default:
                return resources.getString(R.string.notifications_new_message);
        }
    }

    @Override
    public void onServerTimeDone(long serverTime) {
        if ((serverTime-notificationTime) < 30000){
            processIncomingCall();
        }
        else{
            processMissedCall();
        }
    }

    @Override
    public void onServerTimeError() {
        processErrorInCall();
    }


    public interface NotificationProcessed {
        void onNotificationProcessed(Bundle bundle);
        void onNotificationFailure();
    }

    private void onNotificationProcessed(Bundle bundle) {
        Log.d("pronoti","notiProcessor, onNotificationProcessed type: "+type);
        boolean setShown = type.equals("USER_LINKED")
                || type.equals("USER_UNLINKED")
                || type.equals("USER_LEFT_CIRCLE")
                || type.equals("MEETING_INVITATION_EVENT")
                || type.equals("MEETING_CHANGED_EVENT")
                || type.equals("MEETING_ACCEPTED_EVENT")
                || type.equals("MEETING_REJECTED_EVENT")
                || type.equals("MEETING_INVITATION_REVOKE_EVENT")
                || type.equals("MEETING_DELETED_EVENT")
                || type.equals("ADDED_TO_GROUP")
                /*|| type.equals("INCOMING_CALL")*/
                || type.equals("GROUP_USER_INVITATION_CIRCLE")
                || type.equals("REMOVED_FROM_GROUP");
              //  || type.equals("GROUP_UPDATED");
        notificationsDb.setNotificationToProcessedShown(notificationId, setShown);

        if (type.equals("NEW_MESSAGE")) {
            notificationsDb.setMessageNotificationsNotShownExceptId(type, notificationId,
                    (int) bundle.getLong("chatId"));
        } else if (type.equals("NEW_CHAT_MESSAGE")) {
            notificationsDb.setMessageNotificationsNotShownExceptId(type, notificationId, chatId);
        }

        if (bundle == null) {
            bundle = new Bundle();
            bundle.putString("type", type);
        }

        buildNotification(bundle);

        if (callback != null) callback.onNotificationProcessed(bundle);
        callback = null;
    }

    private void onNotificationSkipped() {
        notificationsDb.setNotificationToProcessedShown(notificationId, false);
        if (callback != null) callback.onNotificationProcessed(null);
        callback = null;
    }

    private void buildNotification(Bundle bundle) {
        boolean showNotification = true;
        String text = "";
        String title = null;
        String channelId = CHANNEL_ID_OTHER;
        String channelName = resources.getString(R.string.notifications_channel_other_name);
        String channelDescription = resources.getString(R.string.notifications_channel_other_description);
        Intent intent = new Intent(MyApplication.getAppContext(), MainFragmentManagerActivity.class);
        Log.d("pronoti","notiProcessor, buildNotification type: "+type);
        switch (type) {
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                if (getUserName(idUser) != null){
                    text = resources.getString(R.string.notifications_no_longer_contact, getUserName(idUser));
                }
                else{
                    showNotification = false;
                }
                break;
            case "USER_LINKED":
                if (getUserName(idUser) != null) {
                    text = resources.getString(R.string.notifications_new_contact, getUserName(idUser));
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("idUser", idUser);
                break;
            case "MEETING_INVITATION_EVENT":
                long meetingDate = bundle.getLong("date");
                Locale loc = resources.getConfiguration().locale;
                int hostId = bundle.getInt("hostId");
                if (getUserName(hostId) != null) {
                    text = resources.getString(R.string.notifications_new_date, getUserName(hostId),
                            DateUtils.getMeetingDetailDate(meetingDate, loc),
                            DateUtils.getFormatedHourMinute(meetingDate));
                }
                else{
                    showNotification = false;
                }

                intent.putExtra("idMeeting", idMeeting);
                break;
            case "MEETING_CHANGED_EVENT":
                meetingDate = bundle.getLong("date");
                loc = resources.getConfiguration().locale;
                hostId = bundle.getInt("hostId");
                if (getUserName(hostId) != null) {
                    text = resources.getString(R.string.notifications_date_updated_system,
                            getUserName(hostId), DateUtils.getMeetingDetailDate(meetingDate, loc),
                            DateUtils.getFormatedHourMinute(meetingDate));
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("idMeeting", idMeeting);
                break;
            case "MEETING_ACCEPTED_EVENT":
                meetingDate = getMeetingDate();
                loc = resources.getConfiguration().locale;
                if (getUserName(idUser) != null) {
                    text = resources.getString(R.string.notifications_date_accepted,
                            getUserName(idUser), DateUtils.getMeetingDetailDate(meetingDate, loc),
                            DateUtils.getFormatedHourMinute(meetingDate));
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("idMeeting", idMeeting);
                break;
            case "MEETING_REJECTED_EVENT":
                meetingDate = getMeetingDate();
                loc = resources.getConfiguration().locale;
                if (getUserName(idUser) != null) {
                    text = resources.getString(R.string.notifications_date_rejected,
                            getUserName(idUser), DateUtils.getMeetingDetailDate(meetingDate, loc),
                            DateUtils.getFormatedHourMinute(meetingDate));
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("idMeeting", idMeeting);
                break;
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                meetingDate = getMeetingDate();
                loc = resources.getConfiguration().locale;
                hostId = bundle.getInt("hostId");
                if (getUserName(hostId) != null) {
                    text = resources.getString(R.string.notifications_date_revoked,
                            getUserName(hostId), DateUtils.getMeetingDetailDate(meetingDate, loc),
                            DateUtils.getFormatedHourMinute(meetingDate));
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("meetingDate", meetingDate);
                break;
            case "ADDED_TO_GROUP":
                text = resources.getString(R.string.notifications_new_group, getGroupName());
                channelName = resources.getString(R.string.notifications_channel_message_name);
                channelDescription = resources.getString(R.string.notifications_channel_other_description);
                intent.putExtra("chatId", chatId);
                break;
            case "REMOVED_FROM_GROUP":
                text = resources.getString(R.string.notifications_deleted_group, getGroupName());
                channelName = resources.getString(R.string.notifications_channel_other_name);
                channelDescription = resources.getString(R.string.notifications_channel_message_description);
                break;

            case "NEW_MESSAGE":
                idUser = (int) bundle.getLong("chatId");
                text = getUserUnreadMessages();
                if (getUserName(idUser) != null) {
                    title = getUserName(idUser);
                }
                else{
                    showNotification = false;
                }
                channelId = CHANNEL_ID_MESSAGE;
                intent.putExtra("idUser", idUser);
                break;
            case "NEW_CHAT_MESSAGE":
                title = getGroupName();
                text = bundle.getString("userSenderName")+": "+ getMessageText(
                        bundle.getString("metadatatipus"),
                        bundle.getString("text")) +"\n...";
                channelId = CHANNEL_ID_MESSAGE;
                intent.putExtra("chatId", chatId);
                break;
            case "INCOMING_CALL":
                Log.d("pronoti","buildNoti, missed calll: "+idUser);
                if (incommingCallIsMissed) {
                    type = NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE;
                    text = resources.getString(R.string.notifications_missed_call, getUserName(idUser));
                    intent.putExtra("idUser", idUser);
                } else {
                    showNotification = false;
                }

                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                Log.d("gdp","system notif id:"+idUser);
                if (getUserName(idUser) != null) {
                    text = resources.getString(R.string.group_detail_invite_notification,
                            getUserName(idUser), code);
                }
                else{
                    showNotification = false;
                }
                intent.putExtra("code", code);
                break;
            default:
                showNotification = false;
                break;
        }

        if (showNotification) {
            showNotification(type, channelId, channelName, channelDescription, idUser, chatId,
                    intent, text, title, resources);
        }

    }

    public static void buildMissedCallNotification(Resources resources, int idUser) {
        Intent intent = new Intent(MyApplication.getAppContext(), MainFragmentManagerActivity.class);
        intent.putExtra("idUser", idUser);

        String text = resources.getString(R.string.notifications_missed_call, getUserName(idUser));
        String channelName = resources.getString(R.string.notifications_channel_other_name);
        String channelDescription = resources.getString(
                R.string.notifications_channel_message_description);
        showNotification(NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE, CHANNEL_ID_OTHER,
                channelName, channelDescription, idUser, -1, intent, text, null,
                resources);
    }

    private static void showNotification(String type, String channelId, String channelName,
                                         String channelDescription, int idUser, int chatId,
                                         Intent intent, String text, String title,
                                         Resources resources) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = (type.equals("NEW_MESSAGE") || type.equals("NEW_CHAT_MESSAGE")) ?
                    NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = MyApplication.getAppContext()
                    .getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        int notificationId;
        if (type.equals("NEW_MESSAGE") || type.equals("NEW_CHAT_MESSAGE")) {
            //to update if there are multiple notifications
            notificationId = type.equals("NEW_MESSAGE") ? idUser : chatId;
        } else {
            notificationId = new UserPreferences(MyApplication.getAppContext())
                    .notificationSystemUpperIdGetAndSubtract();
        }


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("type", type);
        Log.d("pronoti","notiProcessor, buildNotification put extra type: "+type);
        Log.d("pronoti","notiProcessor, buildNotification get extra type: "+intent.getExtras().getString("type"));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                MyApplication.getAppContext(), notificationId, intent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                MyApplication.getAppContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (title != null) mBuilder.setContentTitle(title);

        if (type.equals("NEW_MESSAGE") || type.equals("NEW_CHAT_MESSAGE")) {
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            //to update if there are multiple notifications
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(
                MyApplication.getAppContext());
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private static String getUserName(int idUser) {
        Log.d("pronoti","getUserName, idUser: "+idUser);
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        GetUser user = usersDb.findUserUnmanaged(idUser);
        if (user == null){
            Dynamizer dyn = usersDb.findDynamizerUnmanaged(idUser);
            return dyn.getName();
        }
        return user.getName();
    }

    private String getGroupName() {
        Log.d("pronoti","getGroupName, chatId: "+chatId);
        UserGroupsDb db = new UserGroupsDb(MyApplication.getAppContext());
        GroupRealm groupRealm = db.getGroupFromIdChatUnmanaged(chatId);
        if (groupRealm != null) return groupRealm.getName();
        Dynamizer dynamizer = db.findDynamizerFromChatIdUnmanaged(chatId);
        if (dynamizer==null) return "";
        return db.findDynamizerFromChatIdUnmanaged(chatId).getName();
    }

    private long getMeetingDate() {
        return new MeetingsDb(MyApplication.getAppContext()).findMeeting(idMeeting).getDate();
    }

    void cancelProcessing() {
        cancelledProcessing = true;
    }

    private void onCancelledProcessingFinish() {
        if (callback != null) callback.onNotificationProcessed(null);
        callback = null;
    }

}
