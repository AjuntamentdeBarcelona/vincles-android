/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.Calendar;
import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.BuildConfig;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsListActivity;
import cat.bcn.vincles.tablet.contracts.OnNotificationFragmentInteractionListener;
import cat.bcn.vincles.tablet.fragment.ChatNotificationFragment;
import cat.bcn.vincles.tablet.fragment.DefaultNotificationFragment;
import cat.bcn.vincles.tablet.fragment.EventNotificationFragment;
import cat.bcn.vincles.tablet.fragment.GroupNotificationFragment;
import cat.bcn.vincles.tablet.fragment.LostCallNotificationFragment;
import cat.bcn.vincles.tablet.fragment.MessageNotificationFragment;
import cat.bcn.vincles.tablet.fragment.NewUserNotificationFragment;
import cat.bcn.vincles.tablet.fragment.NotificationFragmentTemplate;
import cat.bcn.vincles.tablet.fragment.RememberEventNotificationFragment;
import cat.bcn.vincles.tablet.fragment.StickyEventNotificationFragment;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.monitors.FreeSpaceMonitor;
import cat.bcn.vincles.tablet.push.AppFCMDefaultListenerImpl;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class TaskMainActivity extends TaskActivity implements OnNotificationFragmentInteractionListener {
    private static final String TAG = "TaskMainActivity";
    private TextView txtVersion;
    private TextView txtName;
    private View texMainlogo;
    private long lastDown;
    private long keyPressedDuration;

    FeedItem lastNotification;
    private FeedModel feedModel;
    NotificationFragmentTemplate notificationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_main);

        feedModel = FeedModel.getInstance();

        // Update view
        txtName = (TextView) findViewById(R.id.txtName);
        txtVersion = (TextView) this.findViewById(R.id.txtVersion);
        texMainlogo =  findViewById(R.id.texMainlogo);

        // CHECK NOTIFICATIONS ONLY IN HOME ACTIVITY
        mainModel.checkNewNotifications();

        // TEST PURPOUSE ONLY
//        feedModel.clearFeed();

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = false;
        taskModel.isPrivateChat = false;

        Log.d(TAG, "Firebase TOKEN: " + FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onResume() {
        super.onResume();

        FreeSpaceMonitor.checkFreeSpace(this);

        int density= getResources().getDisplayMetrics().densityDpi;
        String res = " ";
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                res += "LDPI";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                res += "MDPI";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                res += "HDPI";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                res += "XHDPI";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                res += "XXHDPI";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                res += "XXXHDPI";
                break;
            case DisplayMetrics.DENSITY_TV:
                res += "TV";
                break;
            default:
                res += density + "";
                break;
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 6)
            txtName.setText(getResources().getString(R.string.welcome_night) + ", " + mainModel.currentUser.alias);
        else if (hour < 14)
            txtName.setText(getResources().getString(R.string.welcome_day) + ", " + mainModel.currentUser.alias);
        else if (hour < 19)
            txtName.setText(getResources().getString(R.string.welcome_afternoon) + ", " + mainModel.currentUser.alias);
        else
            txtName.setText(getResources().getString(R.string.welcome_night) + ", " + mainModel.currentUser.alias);

        if (BuildConfig.DEBUG)
            txtVersion.setText(BuildConfig.VERSION_NAME + " - " + res +
                    " - " + BuildConfig.FLAVOR + "-" + VinclesApp.getVinclesApp().getAppFlavour());
        else txtVersion.setVisibility(View.INVISIBLE);
        texMainlogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (BuildConfig.DEBUG) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) goToHome();
                }
                else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        lastDown = System.currentTimeMillis();
                        Log.i(TAG, "lastDown: " + Long.toString(lastDown));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        keyPressedDuration = System.currentTimeMillis() - lastDown;
                        Log.i(TAG, "keyPressedDuration: " + Long.toString(keyPressedDuration));
                        if (keyPressedDuration > 4000) {
                            goToHome();
                        }
                    }
                }
                return true;
            }
        });

        // RECEIVE PUSH NOTIFICATIONS
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(final PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                refreshNotificationFragment();
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
                Log.d(null, "GCM: ERROR TRYING TO ACCESS PUSHID: " + idPush);
            }
        });

        refreshNotificationFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(mainModel.getPushListener());
    }

    @Override
    public void goToHome() {
        startActivity(new Intent(this, TaskLoginActivity.class));
    }

    public void goToView(View view) {
        int id = view.getId();

        String idName = view.getResources().getResourceEntryName(view.getId());
        Log.i(TAG, "Got to activity: " + idName.substring(3));

        switch (idName) {
            case ("btnNetwork"):
                startActivity(new Intent(this, TaskNetworkListActivity.class));
                break;
            case ("btnCall"):
                startActivity(new Intent(this, TaskCallActivity.class));
                break;
            case ("btnMessage"):
                startActivity(new Intent(this, TaskMessageListActivity.class));
                break;
            case ("btnImage"):
                startActivity(new Intent(this, TaskImageDetailActivity.class));
                break;
            case ("btnCalendar"):
                taskModel.view = "";//TaskModel.TASK_TODAY;
                startActivity(new Intent(this, TaskCalendarActivity.class));
                break;
            case ("btnGroups"):
                startActivity(new Intent(this, GroupsListActivity.class));
                break;
        }
    }

    public void switch_theme(View view) {
        if (mainModel.getResourceTheme() == R.style.VinclesFoscaTheme) {
            mainModel.changeTheme(VinclesTabletConstants.CLARA_THEME);
        } else {
            mainModel.changeTheme(VinclesTabletConstants.FOSCA_THEME);
        }
        finish();
    }

    public void refreshNotificationFragment() {
        lastNotification = feedModel.getLastItem(true);
        if (lastNotification == null || lastNotification.getIdData() == null)
            notificationFragment = DefaultNotificationFragment.newInstance();

        else {
            switch (lastNotification.getType()) {
                case FeedItem.FEED_TYPE_NEW_MESSAGE:
                case FeedItem.FEED_TYPE_NEW_AUDIO_MESSAGE:
                case FeedItem.FEED_TYPE_NEW_VIDEO_MESSAGE:
                case FeedItem.FEED_TYPE_NEW_IMAGE_MESSAGE:
                case FeedItem.FEED_TYPE_NEW_TEXT_MESSAGE:
                    notificationFragment = MessageNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_NEW_EVENT:
                case FeedItem.FEED_TYPE_EVENT_UPDATED:
                case FeedItem.FEED_TYPE_DELETED_EVENT:
                    notificationFragment = EventNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_INVITATION_SENT:
                    notificationFragment = GroupNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_ADDED_TO_GROUP:
                    notificationFragment = GroupNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_REMEMBER_EVENT:
                    notificationFragment = RememberEventNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_EVENT_FROM_AGENDA:
                    notificationFragment = StickyEventNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_USER_LINKED:
                    notificationFragment = NewUserNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_LOST_CALL:
                    notificationFragment = LostCallNotificationFragment.newInstance();
                    break;
                case FeedItem.FEED_TYPE_NEW_CHAT:
                    notificationFragment = ChatNotificationFragment.newInstance();
                    break;
                default:
                    Log.e(TAG, "FEED ITEM FRAGMENT TYPE NOT FOUND!!! - " + lastNotification.getType());
                    discardNotificationFragment(); // This restart the function with the next one
                    return;
            }
            notificationFragment.setFeedItem(lastNotification);
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_notification, notificationFragment).commitAllowingStateLoss();
    }

    @Override
    public synchronized void discardNotificationFragment() {
        if (lastNotification != null)
            feedModel.remove(lastNotification);
        refreshNotificationFragment();
    }
}