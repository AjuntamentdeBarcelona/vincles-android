package cat.bcn.vincles.mobile.UI.Splash;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import cat.bcn.vincles.mobile.Client.Business.VersionControlAlert;
import cat.bcn.vincles.mobile.Client.Db.DatabaseUtils;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Migration.Fase1SQLiteHelper;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Chats.ChatFragment;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.UI.TermsAndConditions.TermsAndConditionsActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static cat.bcn.vincles.mobile.Utils.MyApplication.getAppContext;


public class SplashScreenActivity extends AppCompatActivity  implements AlertMessage.AlertMessageInterface, MyApplication.AppInterface, VersionControlAlert.ControlVersionInterface, LifecycleObserver {

    private static final long SPLASH_SCREEN_DELAY = 2000;
    public final static String LOGOUT_BROADCAST = "logout_broadcast";
    public static final String FRAGMENT_CHAT = "fragment_chat";

    Context context;

    @Override
    protected void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this, getResources().getString(R.string.tracking_terms_splash));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
        context = this;
        setContentView(R.layout.activity_splash_screen);

        ((MyApplication)getAppContext()).initAppInterface(this);

        //showVersionControl();

       // startTimer();
    }

    @Override
    public void showVersionControl() {
        new Environment(getApplicationContext());
        new VersionControlAlert(
                Environment.getVersionControlUrl(), this, this
        );
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                UserPreferences userPreferences = new UserPreferences(getApplication());
                int userID = userPreferences.getUserID();
                boolean isLogged = userPreferences.getLoginDataDownloaded();

                //old preferences for migration
                SharedPreferences preferences = getSharedPreferences(
                        "cat.bcn.vincles.mobile.app-preferences", Context.MODE_PRIVATE);
                final long fase1UserId = preferences.getLong("cat.bcn.vincles.mobile.user-id", 0L);

                if (fase1UserId != 0) {
                    Log.d("migrt","fase 1 user id:"+fase1UserId);
                    Fase1SQLiteHelper sqLiteHelper = new Fase1SQLiteHelper(context);
                    try {
                        String[] userPwd = sqLiteHelper.getUserPassword((int) fase1UserId);

                        Intent intent = new Intent().setClass(
                                SplashScreenActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        Bundle bundle = new Bundle();
                        bundle.putString("username", userPwd[0]);
                        bundle.putString("password", userPwd[1]);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }catch (Exception e){
                        Log.e("migrt","ERROR");
                        //Go to Login
                        startActivityIntent(SplashScreenActivity.this, LoginActivity.class);
                    }

                } else if (userID != 0 && isLogged) {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, MainFragmentManagerActivity.class);
                    startActivity(intent);
                } else if (userID != 0) {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, TermsAndConditionsActivity.class);
                    startActivity(intent);
                }
            }};

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    private void startActivityIntent(Context context, Class theClass) {
        Intent intent = new Intent().setClass(
                context, theClass);
        startActivity(intent);
    }

    public void renewTokenFailure(){
        AlertMessage alertMessage = new AlertMessage(this, getResources().getString(R.string.close_session));
        alertMessage.showMessage(this, getResources().getString(R.string.error_default), "renewTokenError");
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        if (type.equals("renewTokenError")) {
            UserPreferences userPreferences = new UserPreferences();
//            Log.d("continuecheck", "Clearing preferences from SplashScreenActivity");
            userPreferences.clear();
            goToLoginAfterLogout();
        }
    }

    public void goToLoginAfterLogout() {
        finishAffinity();
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CHAT);
        if (chatFragment != null) {
            chatFragment.onLogout();
        }
        Intent intent = new Intent(LOGOUT_BROADCAST);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        DatabaseUtils.dropAllTables();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void continueToApp(boolean b) {
        if (!b){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            return;
        }
        else{
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }

        startTimer();
    }

    @Override
    protected void onStop() {
        new UserPreferences().continueToApp(true);
        super.onStop();
    }
}
