package cat.bcn.vincles.mobile.Utils;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import cat.bcn.vincles.mobile.Client.Db.RealmMigrations;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivity;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MyApplication extends Application {
    private static Context context;

    /**
     *
     * When a notification requires the update of the UI, a broadcast is used.
     *
     * When there is a missed call due to timeout, sending a broadcast to the MainActivity does
     * not work because it is not running (the current activity is CallsActivity.
     *
     * This boolean is used to let the activity know that there is a missed call that requires
     * updating the UI
     *
     */
    private static boolean pendingMissedCallBroadcast = false;

    private AppInterface appInterface;

    public boolean isBackground = true;
    public boolean isShowingCall = false;
    public CallsActivity callsActivity = null;

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        // Initialise Crashlytics (after migration from Fabric's Crashlytics to Firebase Crashlitics,
        // this must be done unless the Manifest hookup is used to initialise it automatically)
        Fabric.with(this, new Crashlytics());

        checkContinueToApp();

        setupLifecycleListener();

        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                                                .schemaVersion(9)
                                                .migration(new RealmMigrations())
                                                .compactOnLaunch()
                                                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        sAnalytics = GoogleAnalytics.getInstance(this);
    }

    private void checkContinueToApp() {
        // Ensure that the Version Available dialog shows up if there is a new
        // version available
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.continueToApp(true);
        userPreferences.setContinueToAppCheck(true);
    }

    public void initAppInterface(AppInterface appInterface) {
        this.appInterface = appInterface;
    }

    private void setupLifecycleListener() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleListener);
    }

    public LifecycleObserver lifecycleListener = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void onMoveToForeground() {
            isBackground = false;
            Log.d("LifecycleObserver", "Returning to foreground…");
            UserPreferences userPreferences = new UserPreferences();
//            Log.d("continuecheck", String.format("onMoveToForeground.getcontinueToApp() = %b", userPreferences.getcontinueToApp()));
            if (userPreferences.getcontinueToApp())
            if (appInterface!=null)appInterface.showVersionControl();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        void onMoveToBackground() {
            isBackground = true;
            Log.d("LifecycleObserver", "Moving to background…");
        }
    };

    public interface AppInterface{
        void showVersionControl();
    }



    public static Context getAppContext() {
        return context;
    }

    public static boolean isPendingMissedCallBroadcast() {
        return pendingMissedCallBroadcast;
    }

    public static void setPendingMissedCallBroadcast(boolean pendingMissedCallBroadcast) {
        MyApplication.pendingMissedCallBroadcast = pendingMissedCallBroadcast;
    }

    private BaseActivity mCurrentActivity = null;
    public BaseActivity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(BaseActivity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(getResources().getString(R.string.analytic_key));
        }

        return sTracker;
    }
}
