package cat.bcn.vincles.mobile.Client.Business.Firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Business.NotificationsManager;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.UI.Splash.SplashScreenActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class FirebaseListenerService extends FirebaseMessagingService {

    public final static String FIREBASE_PUSH_INTENT = "firebase_push_intent";

    @Override
    public void onMessageReceived(final RemoteMessage message){

        Intent serviceIntent = new Intent(this, NotificationsManager.class);
        Map<String, String> params = new HashMap<>();
        try
        {
            params = message.getData();
            JSONObject object = new JSONObject(params);
            serviceIntent.putExtra("EXTRA", object.toString());
            if (params.size() != 0){
                ContextCompat.startForegroundService(this, serviceIntent);
            }
        }catch (Exception e){
            Log.e("FIREBASE", "notification bundle Error");
        }

        // Check if message contains a notification payload.
        if (params.size() == 0 && message.getData() != null) {
            createNotificationChannelAndShow(message.getData().get("title"), message.getData().get("body"), message.getSentTime());

        }
    }

    private void createNotificationChannelAndShow(String title, String body, long sentTime) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        String channelId = "CHANNEL_ID_NOTIFICATIONS";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getAppContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("notifManual", "CHANNEL_ID_NOTIFICATIONS ");
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.notifications_channel_notifications_name), importance);
            channel.setDescription(getString(R.string.notifications_channel_notifications_description));
            NotificationManager notificationManager = MyApplication.getAppContext()
                    .getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            else{
                Log.d("notifManual", "notificationManager es null ");

            }
            builder = new NotificationCompat.Builder(MyApplication.getAppContext(), channelId);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                UserPreferences userPreferences = new UserPreferences();


        Intent resultIntent = new Intent(MyApplication.getAppContext(), getCurrentClass());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
                builder.setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(body))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOngoing(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        else{
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MyApplication.getAppContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify((int) sentTime, builder.build());

        Log.d("notifManual", "notificationManager notify ");

    }

    public Class getCurrentClass() {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = null;
        if (manager != null) {
            runningTaskInfo = manager.getRunningTasks(1);
            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
            return componentInfo.getClass();
        }

        return SplashScreenActivity.class;


    }


    public static boolean isBackgroundRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        if (runningProcesses == null) return true;
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return false;
                    }
                }
            }
        }


        return true;
    }
}
