/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.push;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.activity.operation.TaskVideoConferenceCallActivity;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class VinclesFcmListenerService extends FirebaseMessagingService {
    private static final String TAG = "VinclesGcmListenerSvc";
    private MainModel mainModel = MainModel.getInstance();

    public VinclesFcmListenerService() {
        super();
    }

    @Override
    public void onMessageReceived(final RemoteMessage message){
        Log.d(TAG, "GCM From: " + message.getFrom());
        try {
            String accessToken = mainModel.getAccessToken();
            if (accessToken == null) {
                mainModel.initialize(this, true);
                mainModel.login(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            String accessToken = mainModel.getAccessToken();
                            handleReceived(message.getData(), accessToken);
                        }

                        @Override
                        public void onFailure(Object error) {

                        }
                    }, mainModel.currentUser.username
                    , mainModel.getPassword(mainModel.currentUser));

            } else {
                handleReceived(message.getData(), accessToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceived(Map data, String accessToken) {
        String type = (String)data.get("push_notification_type");
        if (type != null) {
            if (type.equals(PushMessage.TYPE_INCOMING_CALL)) {
                long userId = Long.parseLong(String.valueOf(data.get("idUser")));
                if (TaskVideoConferenceCallActivity.isInVC) {
                    FeedModel.getInstance().addLostCall(userId);

                } else {
                    // MORE THAN 10 SECONDS BETWEEN SEND AND RECEIVE NOTIFICATION MEANS LOST CALL
                    if (System.currentTimeMillis() - Long.parseLong(String.valueOf(data.get("push_notification_time"))) > VinclesTabletConstants.VC_NOTIFICATION_TIMEOUT_MS) {
                        FeedModel.getInstance().addLostCall(userId);
                        // FAKE A PUSH TO REFRESH FEED IF LOST CALL
                        PushMessage pushMessage = new PushMessage();
                        pushMessage.setType("REFRESH");
                        CommonVinclesGcmHelper.getPushListener().onPushMessageReceived(pushMessage);
                    } else {
                        String idRoom = (String)data.get("idRoom");
                        TaskVideoConferenceCallActivity.startActivityForIncomingCall(this, userId, idRoom);
                    }
                }
            }
        } else {
            try {
                mainModel.checkNewNotifications();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}