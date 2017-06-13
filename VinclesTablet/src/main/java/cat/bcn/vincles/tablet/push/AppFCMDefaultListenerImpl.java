/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.push;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.push.VinclesPushListener;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.VinclesActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskCallActivity;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.ResourceModel;

public class AppFCMDefaultListenerImpl implements VinclesPushListener {
    private static final String TAG = AppFCMDefaultListenerImpl.class.getSimpleName();
    private Context context;
    private MainModel mainModel = MainModel.getInstance();
    private GroupModel groupModel = GroupModel.getInstance();
    private VinclesActivity actualActivity;
    private Dialog dialog;
    private FeedModel feedModel;

    public AppFCMDefaultListenerImpl(Context ctx) {
        context = ctx;
        feedModel = FeedModel.getInstance();
    }

    @Override
    public void onPushMessageReceived(final PushMessage pushMessage) {
        // DO WHATEVER NEED TO DO YOUR APP HERE
        Log.d(TAG, "GCM: LLEGAN A LA APP!!");

        Task taskTemp = null;
        Log.d(null, "GCM PROCESS CONTENT:" + pushMessage.getRawDataJson());
        JsonObject json = null;
        if (pushMessage.getRawDataJson() != null)
            json = new JsonParser().parse(pushMessage.getRawDataJson()).getAsJsonObject();

        final FeedItem feedItem = new FeedItem().fromPushMessage(pushMessage);
        switch (pushMessage.getType()) {
            case PushMessage.TYPE_USER_UPDATED:
                mainModel.getUserInfo(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // FAKE A PUSH TO REFRESH USER PHOTO ASYNC
                        pushMessage.setType("REFRESH");
                        CommonVinclesGcmHelper.getPushListener().onPushMessageReceived(pushMessage);
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, pushMessage.getIdData());
                break;
            case PushMessage.TYPE_USER_LINKED:
                mainModel.getUserInfo(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // THE ACTIVITY WAY
//                        Intent intent = new Intent(context, TaskNotificationNewUserActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        User user = (User)result;
//                        intent.putExtra("userId", user.getId());
//                        context.startActivity(intent);

                        // THE FEED NOTIFICATION FRAGMENT WAY
                        feedModel.addItem(feedItem);

                        // FAKE A PUSH TO REFRESH USER PHOTO ASYNC
                        pushMessage.setType("NONE");
                        CommonVinclesGcmHelper.getPushListener().onPushMessageReceived(pushMessage);
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, pushMessage.getIdData());
                break;

            case PushMessage.TYPE_INVITATION_SENT:
                feedModel.addItem(feedItem);
                groupModel.getInvitation(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Nothing to do!!!
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, feedItem.getIdData());
                break;

            case PushMessage.TYPE_ADDED_TO_GROUP:
                feedModel.addItem(feedItem);

                // Get user list of group added
                groupModel.getGroupUserServerList(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Nothing to do!!!
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, feedItem.getIdData());
                break;

            case PushMessage.TYPE_NEW_USER_GROUP:
                feedModel.addItem(feedItem);

                // Get user of group added
                groupModel.getGroupUserServer(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Nothing to do!!!
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, feedItem.getIdData(), feedItem.getExtraId());
                break;

            case PushMessage.TYPE_NEW_MESSAGE:
                Message messageTemp = Message.findById(Message.class, pushMessage.getIdData());
                feedModel.addItem(feedItem.setType("FEED_NEW_" + messageTemp.metadataTipus));
                break;

            case PushMessage.TYPE_EVENT_ACCEPTED:
            case PushMessage.TYPE_EVENT_REJECTED:
            case PushMessage.TYPE_EVENT_UPDATED:
            case PushMessage.TYPE_REMEMBER_EVENT:
            case PushMessage.TYPE_NEW_EVENT:
                taskTemp = new TaskDAOImpl().get(pushMessage.getIdData());
            case PushMessage.TYPE_DELETED_EVENT:
                if (taskTemp == null) taskTemp = Task.fromJSON(json);
                feedModel.addItem(feedItem
                        .setFixedData(taskTemp.owner.alias, taskTemp.description, taskTemp.getDate().getTime())
                        .setExtraId(taskTemp.owner.getId()));
                break;

            case PushMessage.TYPE_NEW_CHAT:
                ResourceModel.getInstance().loadChatGroupResource(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onFailure(Object error) {
                    }
                }, new ChatDAOImpl().get(pushMessage.getIdData()));
                feedModel.addItem(feedItem);
                break;

            // MONITORS
            case PushMessage.TYPE_BATTERY_OKAY:
                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                        dialog = null;
                    } catch (Exception e) {
                    }
                    ;
                }
                if (actualActivity != null && !actualActivity.isFinishing()) {
                    actualActivity.checkBatteryStatus();
                }
                break;

            case PushMessage.TYPE_BATTERY_LOW:
                if (actualActivity != null && !actualActivity.isFinishing()) {
                    if (actualActivity instanceof TaskActivity)
                        actualActivity.checkBatteryStatus();
                }

                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                        dialog = null;
                    } catch (Exception e) {
                    }
                }
                dialog = new Dialog(actualActivity, R.style.DialogCustomTheme);
                dialog.setContentView(R.layout.alert_dialog_battery);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                TextView alertText = (TextView) dialog.findViewById(R.id.item_message_title);
                alertText.setText(context.getString(R.string.battery_low_message, mainModel.currentUser.alias));
                View close_btn = dialog.findViewById(R.id.btnClose);
                close_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        dialog = null;
                    }
                });
                break;

            case PushMessage.TYPE_STRENGTH_CONNECTION_LOW:
                if (actualActivity != null && actualActivity instanceof TaskCallActivity) {
                    actualActivity.checkStrengthSignalStatus();
                }
                break;
            case PushMessage.TYPE_STRENGTH_CONNECTION_OK:
                if (actualActivity != null && actualActivity instanceof TaskCallActivity) {
                    actualActivity.removeStrengthSignalStatus();
                }
                break;

            case PushMessage.TYPE_CONNECTION_OKAY:
                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                        dialog = null;
                    } catch (Exception e) {
                    }
                }
                if (actualActivity != null && !actualActivity.isFinishing()) {
                    actualActivity.checkInternetStatus();
                }
                break;

            case PushMessage.TYPE_NO_CONNECTION:
                if (actualActivity != null && !actualActivity.isFinishing()) {
                    if (actualActivity instanceof TaskActivity)
                        actualActivity.checkInternetStatus();
                }

                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                        dialog = null;
                    } catch (Exception e) {
                    }
                }
                dialog = new Dialog(actualActivity, R.style.DialogCustomTheme);
                dialog.setContentView(R.layout.alert_dialog_connection);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                TextView alertText2 = (TextView)dialog.findViewById(R.id.item_message_title);
                alertText2.setText(context.getString(R.string.signal_low_message, mainModel.currentUser.alias));
                View close_btn2 = dialog.findViewById(R.id.btnClose);
                close_btn2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        dialog = null;
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onPushMessageError(long idPush, Throwable t) {
        Log.d(TAG, "GCM: ERROR TRYING TO ACCESS PUSHID: " + idPush);
    }

    public void setActualActivity(VinclesActivity activity) {
        actualActivity = activity;
    }
}
