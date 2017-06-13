/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.push;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.dao.GroupDAO;
import cat.bcn.vincles.lib.dao.GroupDAOImpl;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.PushMessageDAO;
import cat.bcn.vincles.lib.dao.PushMessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAO;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.PushMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonVinclesGcmHelper {
    private static final String TAG = "CommonVinclesGcmHelper";
    private static InternalPushProcessor internalPushProcessor;
    private PushMessageDAO pushMessageDAO;
    private MessageDAO messageDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private ResourceDAO resourceDAO;
    private GroupDAO groupDAO;

    // VOID APP LISTENER AT FIRST
    public CommonVinclesGcmHelper() {
        pushMessageDAO = new PushMessageDAOImpl();
        messageDAO = new MessageDAOImpl();
        taskDAO = new TaskDAOImpl();
        userDAO = new UserDAOImpl();
        resourceDAO = new ResourceDAOImpl();
        groupDAO = new GroupDAOImpl();

        initInternalPushProcessor();
    }

    // NEED TO DEAL WITH CONCURRENCY (UPDATING TO) AND WITH HTTP LOOPs
    public static synchronized long checkNewNotifications(long from, String accessToken) {
        initInternalPushProcessor();
        ArrayList<PushMessage> pushList = new ArrayList<>();
        long ret = checkNewNotificationsPriv(from, 9999999999999l,accessToken, pushList);
        if (ret != -1) {
            for (int i = pushList.size() - 1; i >= 0; i--) {
                internalPushProcessor.onPushMessageReceived(pushList.get(i));
            }
        }
        Log.d(TAG, "GCM CHECK FOR NOTIFICATIONS FROM: " + from + ", RESULT SIZE PROCESSED: " + pushList.size());
        return ret;
    }

    private static long checkNewNotificationsPriv(long from, long to, String accessToken, ArrayList<PushMessage> list) {
        try {
            MessageService messageService = ServiceGenerator.createService(MessageService.class, accessToken);
            Call<JsonArray> call = messageService.getAllNotification(from, to);
            Response<JsonArray> response = call.execute();
            long ret = from;

            for (int i=0; i<response.body().size(); i++) {
                PushMessage pm = PushMessage.fromJSON(response.body().get(i).getAsJsonObject());
                list.add(pm);
                to = pm.getCreationTime()-1;

                if (i == 0)
                    ret = pm.getCreationTime()+1;
            }

            if (response.body().size() == 10)
                checkNewNotificationsPriv(from, to, accessToken, list);

            return ret;
        } catch (IOException t) {
            return -1l;
        }
    }

    /**
     * Process notifications in old style.
     *
     * @deprecated use {@link #checkNewNotifications(long from, String accessToken)} instead.
     */
    @Deprecated
    public void handleGCMReceived(String pushId, String accessToken) throws IOException {
        if (accessToken == null) throw new java.io.IOException("INVALID ACCESS TOKEN");
        final long idPush = Long.parseLong(pushId);
        Log.i(TAG, "GCM: ID PUSH: " + idPush);

        MessageService messageService = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonObject> call = messageService.getNotification(idPush);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body() != null) {
                    PushMessage pm = PushMessage.fromJSON(response.body());
                    // Persist not needed
                    pm.save();
                    internalPushProcessor.onPushMessageReceived(pm);
                } else internalPushProcessor.onPushMessageError(idPush, new Throwable("PUSH BODY NULL"));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                internalPushProcessor.onPushMessageError(idPush, t);
            }
        });
    }

    public static void setPushListener(VinclesPushListener mPushListener) {
        initInternalPushProcessor();
        internalPushProcessor.setAppPushListener(mPushListener);
    }

    public static VinclesPushListener getPushListener() {
        initInternalPushProcessor();
        return internalPushProcessor.getAppPushListener();
    }

    private static void initInternalPushProcessor() {
        if (internalPushProcessor == null)
            internalPushProcessor = new InternalPushProcessor();
    }
}