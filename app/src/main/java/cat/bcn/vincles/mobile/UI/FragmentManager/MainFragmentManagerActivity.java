package cat.bcn.vincles.mobile.UI.FragmentManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cat.bcn.ratememaybe.RateMeMaybe;
import cat.bcn.vincles.mobile.Client.Business.AlertsManager;
import cat.bcn.vincles.mobile.Client.Business.NotificationsManager;
import cat.bcn.vincles.mobile.Client.Business.UserBusiness;
import cat.bcn.vincles.mobile.Client.Business.VersionControlAlert;
import cat.bcn.vincles.mobile.Client.Db.DatabaseUtils;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.NetworkUsage.DataUsageUtils;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.LogoutRequest;
import cat.bcn.vincles.mobile.Client.Requests.PutGroupLastAccessRequest;
import cat.bcn.vincles.mobile.Client.Requests.SetMessageWatchedRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.About.AboutFragment;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Calendar.CalendarDateDetailFragment;
import cat.bcn.vincles.mobile.UI.Calendar.CalendarFragment;
import cat.bcn.vincles.mobile.UI.Calendar.CalendarNewDateFragment;
import cat.bcn.vincles.mobile.UI.Chats.ChatFragment;
import cat.bcn.vincles.mobile.UI.Chats.GroupDetailFragment;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.AddContactFragment;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsFragment;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsPresenter;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsPresenterContract;
import cat.bcn.vincles.mobile.UI.ContentDetail.ContentDetailAugmentedActivity;
import cat.bcn.vincles.mobile.UI.ContentDetail.ContentDetailAugmentedFragment;
import cat.bcn.vincles.mobile.UI.ContentDetail.ContentDetailFragment;
import cat.bcn.vincles.mobile.UI.Gallery.GalleryFragment;
import cat.bcn.vincles.mobile.UI.Home.HomeFragment;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.UI.Notifications.NotificationsFragment;
import cat.bcn.vincles.mobile.UI.Profile.ProfileEditFragment;
import cat.bcn.vincles.mobile.UI.Profile.ProfileFragment;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class MainFragmentManagerActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener,
        View.OnClickListener, LogoutRequest.OnResponse, AlertMessage.AlertMessageInterface,
        ContentDetailFragment.OnFragmentInteractionListener,
        GalleryFragment.OnFragmentInteractionListener,
        ContactsFragment.OnFragmentInteractionListener, BaseFragment.FragmentResumed,
        BaseRequest.RenewTokenFailed, ContentDetailFragment.ContentDetailListener,  ContentDetailAugmentedFragment.ContentDetailAugmentedListener,
        ChatFragment.OnFragmentInteractionListener,
        NotificationsFragment.OnFragmentInteractionListener,
        CalendarFragment.OnFragmentInteractionListener,
        CalendarNewDateFragment.OnFragmentInteractionListener,
        CalendarDateDetailFragment.OnFragmentInteractionListener,
        GroupDetailFragment.OnFragmentInteractionListener,
        ProfileEditFragment.OnFragmentInteractionListener, AlertMessage.CancelMessageInterface,
        VersionControlAlert.ControlVersionInterface {

    private static final String FRAGMENT_HOME = "fragment_home";
    private static final String FRAGMENT_CONTACTS = "fragment_contacts";
    private static final String FRAGMENT_GALLERY = "fragment_gallery";
    private static final String FRAGMENT_CONTENT_DETAIL = "fragment_content_detail";
    private static final String FRAGMENT_NOTIFICATIONS = "fragment_notifications";
    private static final String FRAGMENT_PROFILE = "fragment_profile";
    private static final String FRAGMENT_ABOUT = "fragment_about";
    private static final String FRAGMENT_ADD_CONTACT = "fragment_add_contact";
    private static final String FRAGMENT_CALENDAR = "fragment_calendar";
    private static final String FRAGMENT_CALENDAR_NEW_DATE = "fragment_calendar_new_date";
    private static final String FRAGMENT_CALENDAR_DATE_DETAIL = "fragment_calendar_date_detail";
    private static final String FRAGMENT_GROUP_DETAIL = "fragment_group_detail";


    private int currentScreen = BaseFragment.FragmentResumed.FRAGMENT_HOME;

    UserPreferences userPreferences;
    AlertMessage genericError;
    AlertMessage logoutConfirm;
    boolean isSenior = false;
    NavigationView navigationView;
    ImageView userAvatar;
    GalleryDb galleryDB;
    UsersDb usersDb;
    UserGroupsDb userGroupsDb;
    int indexPhoto = 0;
    private String idUserSender;
    private boolean isGroupChat;
    private List<Integer> selectedContactIds;

    private AlertsManager alertsManager = null;

    DrawerLayout drawer;
    protected MyApplication mMyApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXIT_EXTRA, false)) {
            finish();
            return;
        }

        new Environment(getApplicationContext());

        userPreferences = new UserPreferences(this);
        Log.d("lng onCreate",getResources().getConfiguration().locale.getLanguage());

        //if user is not logged (coming from notification, after token error) we go to login screen
        if (!userPreferences.getLoginDataDownloaded()) {
            Intent intent = new Intent().setClass(
                    MainFragmentManagerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        Log.d("lng bis",getResources().getConfiguration().locale.getLanguage());

        if (getResources().getConfiguration().locale.getLanguage().contains("es") &&
                userPreferences.getUserLanguage().equals(UserRegister.CAT)
                || (getResources().getConfiguration().locale.getLanguage().contains("ca") &&
                userPreferences.getUserLanguage().equals(UserRegister.ESP))) {
            setLocale(userPreferences.getUserLanguage().equals(UserRegister.CAT) ? "ca" : "es");

            recreate();
        }

        alertsManager = new AlertsManager(this, savedInstanceState);

        setContentView(R.layout.activity_main_fragment_manager);

        galleryDB = new GalleryDb(this);
        userGroupsDb = new UserGroupsDb(this);
        usersDb = new UsersDb(this);

        ImageView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu);
        toolbar.setTitleTextAppearance(this, R.style.AkkuratTextAppearance);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                OtherUtils.hideKeyboard(MainFragmentManagerActivity.this);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userAvatar = navigationView.getHeaderView(0).findViewById(R.id.ivHeader);
        updateAvatar();

        final TextView usernameTV = navigationView.getHeaderView(0).findViewById(R.id.header_textview);
        usernameTV.setText(userPreferences.getName());

        if (savedInstanceState == null) {
            askForRating();
        }

        if (savedInstanceState == null) {
            Fragment fragment = HomeFragment.newInstance(this);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.frameContent, fragment, FRAGMENT_HOME).commit();
            LoginActivity.screenOrientation = this.getResources().getConfiguration().orientation;

        } else {
            this.currentScreen = savedInstanceState.getInt("currentScreen");
            LoginActivity.screenOrientation = savedInstanceState.getInt("screenOrientation");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d("pronoti","onCreate with extras");
            processNotificationClicked(extras);
        }

        mMyApp = (MyApplication) this.getApplicationContext();

        checkIfDataUsageToSend();
    }

    private void checkIfDataUsageToSend() {
        DataUsageUtils dataUsageUtils = new DataUsageUtils(this);
        dataUsageUtils.init();
    }


    private void clearReferences(){
        if (mMyApp !=null){
            Activity currActivity = mMyApp.getCurrentActivity();
            if (this.equals(currActivity))
                mMyApp.setCurrentActivity(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMyApp == null){
            mMyApp = (MyApplication) this.getApplicationContext();
        }

        if (alertsManager != null) alertsManager.start();

        startNotificationsManager();

        LoginActivity.screenOrientation = this.getResources().getConfiguration().orientation;
        mMyApp.setCurrentActivity(this);

    }

    public void startNotificationsManager() {

        Intent serviceIntent = new Intent(this, NotificationsManager.class);
        startService(serviceIntent);

        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(notificationProcessedBroadcastReceiver,
                    new IntentFilter(NotificationsManager.NOTIFICATION_PROCESSED_BROADCAST));
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (alertsManager != null) alertsManager.restartMeetingRunnable();

        if (MyApplication.isPendingMissedCallBroadcast()) {
            MyApplication.setPendingMissedCallBroadcast(false);
            Bundle bundle = new Bundle();
            bundle.putString("type", "MISSED_CALL");
            onNotificationProcessed(bundle);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("notupd","on UNregister noti broadcast");

        if (alertsManager != null){
            alertsManager.stop();
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationProcessedBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        clearReferences();
    }

    private BroadcastReceiver notificationProcessedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("notupd","on receive noti broadcast");
            onNotificationProcessed(intent.getExtras().getBundle("bundle"));
        }
    };

    @Override
    public void onDestroy() {
        if (logoutConfirm != null && logoutConfirm.alert != null && logoutConfirm.alert.isShowing()) {
            logoutConfirm.alert.dismiss();
        }
        super.onDestroy();


        clearReferences();
    }
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("currentScreen", currentScreen);
        bundle.putInt("screenOrientation",LoginActivity.screenOrientation);
        if (alertsManager != null) alertsManager.onSaveInstanceState(bundle);
    }

    public void updateAvatar() {
        String pathAvatar = userPreferences.getUserAvatar();
        if (!pathAvatar.equals("")) {
            Uri avatarUri = Uri.parse(pathAvatar);
            userAvatar.setImageURI(avatarUri);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        onScreenExit();

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        onScreenExit();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if (id == R.id.home) {
            OtherUtils.clearFragmentsBackstack(getSupportFragmentManager());
            fragment = HomeFragment.newInstance(this);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameContent, fragment, FRAGMENT_HOME);
            ft.addToBackStack(null);
            ft.commit();
            OtherUtils.hideKeyboard(this);
        } else if (id == R.id.contacts) {
            launchContactsFragment(0); //all contacts
        } else if (id == R.id.media) {
            launchGalleryFragment(false);
        } else if (id == R.id.calendar) {
            launchCalendarFragment();
        } else if (id == R.id.alerts) {
            launchAlertsFragment();
        } else if (id == R.id.configuration) {
            launchProfileFragment();
        } else if (id == R.id.about_vincles) {
            launchAboutFragment();
        } else if (id == R.id.logout) {

            int idLibrary = userPreferences.getIdLibrary();
            int idCalendar = userPreferences.getIdCalendar();

            UserBusiness userBusiness = new UserBusiness();
            isSenior = userBusiness.isUserAuthenticatedUserVincles(idLibrary, idCalendar);

            logoutConfirm = new AlertMessage(this, getResources().getString(R.string.close_session));
            logoutConfirm.setCancelMessageInterface(this);
            logoutConfirm.showMessage(this, getResources().getString(R.string.signout_info), "logout");

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onUpdateAvatar() {
        updateAvatar();
    }

    @Override
    public void onProfileEditClicked() {
        launchProfileEditFragment();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.toolbar_title && currentScreen != BaseFragment.FragmentResumed.FRAGMENT_HOME) {
            //if chat fragment was current open keyboard might be opened. There is one case of
            // low quality images when closing the keyboard and then opening HomeFragment
            /*ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CHAT);
            boolean wasChat = chatFragment != null;*/
            onScreenExit();
            OtherUtils.hideKeyboard(this);
            OtherUtils.clearFragmentsBackstack(getSupportFragmentManager());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = HomeFragment.newInstance(MainFragmentManagerActivity.this);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frameContent, fragment, FRAGMENT_HOME);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }, 100);
        }
    }

    @Override
    public void onResponseLogoutRequest() {
        HomeFragment.needCallRest = true;
//        Log.d("continuecheck", "Clearing preferences from MainFragmentManagerActivity.onResponseLogoutRequest");
        userPreferences.clear();
        /*usersDb.dropTable();
        galleryDB.dropTable();
        userGroupsDb.dropTable();*/
        Intent intent = new Intent(LOGOUT_BROADCAST);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) notificationManager.cancelAll();
        DatabaseUtils.dropAllTables();
        this.finishAffinity();
    }

    @Override
    public void onFailureLogoutRequest(Object error) {
        genericError = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        genericError.showMessage(this, ErrorHandler.getErrorByCode(this, error), "");
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        if (type.equals("renewTokenError")) {
//            Log.d("continuecheck", "Clearing preferences from MainFragmentManagerActivity");
            userPreferences.clear();
            goToLoginAfterLogout();
        }
        else if (alertMessage.equals(genericError)) {
            alertMessage.alert.cancel();
        } else if (alertMessage.equals(logoutConfirm)) {
            logout();
            goToLoginAfterLogout();
        }
    }

    @Override
    public void onCancelAlertMessage() {
        if (logoutConfirm != null) {
            logoutConfirm.dismissSafely();
        }
    }



    public void logout() {
        String token = userPreferences.getAccessToken();
        LogoutRequest logoutRequest = new LogoutRequest(this, token);
        logoutRequest.addOnOnResponse(this);
        logoutRequest.doRequest();
    }

    @Override
    public void onUserAvatarClicked() {
        launchProfileFragment();
    }

    @Override
    public void onGalleryClicked() {
        launchGalleryFragment(false);
    }

    @Override
    public void onNotificationsClicked() {
        launchAlertsFragment();
    }

    @Override
    public void onCalendarClicked() {
        launchCalendarFragment();
    }

    @Override
    public void onContactsClicked(int which) {
        Log.d("shre","onContactsClicked(int which) {");
        launchContactsFragment(which);
    }


    public void launchAlertsFragment() {
        NotificationsFragment fragment = NotificationsFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_NOTIFICATIONS);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchProfileFragment() {
        ProfileFragment fragment = ProfileFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_PROFILE);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchAboutFragment() {
        AboutFragment fragment = AboutFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_ABOUT);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchProfileEditFragment() {
        ProfileEditFragment fragment = ProfileEditFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_PROFILE);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchCalendarFragment() {
        CalendarFragment fragment = CalendarFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_CALENDAR);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchGalleryFragment(boolean inSelectMode) {
        /*if (inSelectMode) {
            getSupportFxragmentManager().popBackStackImmediate();
        }*/
        GalleryFragment fragment = GalleryFragment.newInstance(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_GALLERY);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void launchContactsFragment(int which) {
        int filter;
        switch (which) {
            case BUTTON_ALL_CONTACTS:
                filter = ContactsPresenterContract.FILTER_ALL_CONTACTS;
                break;
            case BUTTON_FAMILY:
                filter = ContactsPresenterContract.FILTER_FAMILY;
                break;
            case BUTTON_GROUPS:
                filter = ContactsPresenterContract.FILTER_GROUPS;
                break;
            default:
                filter = ContactsPresenterContract.FILTER_NOT_INIT;
        }
        Log.d("shre","launchContactsFragment(int which):"+filter);
        ContactsFragment fragment = ContactsFragment.newInstance(this, filter, null, null, false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_CONTACTS);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onGalleryItemPicked(GalleryContentRealm galleryContentRealm, int index, String filterKind) {
        indexPhoto = index;
        String path = galleryContentRealm.getPath();
        loadContent(path, indexPhoto, filterKind);
    }

    @Override
    public void onGalleryDetailItemPicked(GalleryContentRealm galleryContentRealm, int index, String filterKind) {
        indexPhoto = index;
        String path = galleryContentRealm.getPath();
        if (path.equals("")) {

        } else {
            loadContentAugmented(path, indexPhoto, filterKind);
        }
    }


    @Override
    public void onGalleryShareButtonClicked(ArrayList<Integer> idContentList, ArrayList<String> paths,
                                            ArrayList<String> metadata) {
        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_share_contacts_gallery ));

        Log.d("shre","launchContactsFragment:"+ContactsPresenter.FILTER_ALL_CONTACTS);
        ContactsFragment fragment = ContactsFragment.newInstance(this,
                ContactsPresenter.FILTER_ALL_CONTACTS, idContentList, null, false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_CONTACTS);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onMeetingInviteButtonClicked(List<Integer> alreadySelectedContactIds) {
        if (selectedContactIds == null) {
            selectedContactIds = new ArrayList<>();
        }
        selectedContactIds.clear();
        selectedContactIds.addAll(alreadySelectedContactIds);
        ContactsFragment fragment = ContactsFragment.newInstance(this,
                ContactsPresenter.FILTER_FAMILY, null, new ArrayList<>(selectedContactIds), true);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_CONTACTS);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onContactsSelected(List<Integer> contactIds) {
        Log.d("slcon","activity onContactsSelected:"+contactIds);
        this.selectedContactIds = contactIds;
        CalendarNewDateFragment calendarNewFragment = (CalendarNewDateFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CALENDAR_NEW_DATE);
        if (calendarNewFragment != null) {
            calendarNewFragment.deleteCircleNotPresent(contactIds);
        }
    }

    // TODO: 6/18/19 This method should be refactored, for multiple reasons. Do not refactor until thoroughly understood the reason of its existence
    public ArrayList<Integer> getAndDeleteSelectedContacts() {
        Log.d("slcon","activity getAndDeleteSelectedContacts:"+selectedContactIds);
        if (selectedContactIds == null) return new ArrayList<>();
        ArrayList<Integer> res = new ArrayList<>();
        res.addAll(selectedContactIds);
        selectedContactIds = new ArrayList<>();
        return res;
    }

    public void onNotificationProcessed(@NonNull Bundle bundle) {
        String type = bundle.getString("type");
        bundle.putInt(BaseFragment.CHANGES_TYPE, BaseFragment.CHANGES_OTHER_NOTIFICATION);
        //notify home to update counters
        Log.d("notfch","notif processed");
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_HOME);
        if (homeFragment != null) {
            Log.d("notfch","notif processed, home not null");
            Bundle homeBundle = new Bundle();
            homeBundle.putInt(BaseFragment.CHANGES_TYPE, BaseFragment.CHANGES_ANY_NOTIFICATION);
            homeBundle.putString("type", type);
            homeFragment.notifyChanges(homeBundle);
        }
        switch (type) {
            case "NEW_MESSAGE":
            case "NEW_CHAT_MESSAGE":
                Log.d("notman3","main notifProcessed, new message");
                ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CHAT);
                if (chatFragment != null) {
                    bundle.putInt(BaseFragment.CHANGES_TYPE, BaseFragment.CHANGES_NEW_MESSAGE);
                    chatFragment.notifyChanges(bundle);
                }
                ContactsFragment contactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONTACTS);
                if (contactsFragment != null) {
                    contactsFragment.notifyChanges(bundle);
                }
                break;
            case "USER_UPDATED":
            case "USER_LINKED":
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
            case "ADDED_TO_GROUP":
            case "GROUP_UPDATED":
            case "REMOVED_FROM_GROUP":
            case "MISSED_CALL":
                homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_HOME);
                if (homeFragment != null) {
                    homeFragment.notifyChanges(bundle);
                }
                contactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONTACTS);
                if (contactsFragment != null) {
                    contactsFragment.notifyChanges(bundle);
                }
                if (type.equals("USER_UPDATED")) {
                    GalleryFragment galleryFragment = (GalleryFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_GALLERY);
                    if (galleryFragment != null) {
                        galleryFragment.notifyChanges(bundle);
                    }
                    ContentDetailFragment contentDetailFragment = (ContentDetailFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONTENT_DETAIL);
                    if (contentDetailFragment != null) {
                        contentDetailFragment.notifyChanges(bundle);
                    }
                }
                if (type.equals("USER_UPDATED") || type.equals("GROUP_UPDATED")
                        || type.equals("REMOVED_FROM_GROUP")) {
                    chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CHAT);
                    if (chatFragment != null) {
                        chatFragment.notifyChanges(bundle);
                    }
                }
                break;
            case "NEW_USER_GROUP":
            case "REMOVED_USER_GROUP":
                break;
            case "MEETING_INVITATION_DELETED_EVENT":
            case "MEETING_INVITATION_EVENT":
            case "MEETING_CHANGED_EVENT":
            case "MEETING_INVITATION_ADDED_EVENT":
            case "MEETING_DELETED_EVENT":
            case NotificationsDb.MEETING_REMINDER_NOTIFICATION_TYPE:
                alertsManager.restartMeetingRunnable();
                break;

        }

    }

    @Override
    public void onMeetingCreatedOrUpdated() {
        alertsManager.restartMeetingRunnable();
    }

//    @Override
//    public void onGalleryItemLongPicked(GalleryContent galleryContent, int index) {
//
//    }

    public void loadContent(String path, int index, String filterKind) {
        ContentDetailFragment fragment = ContentDetailFragment.newInstance(this, path, index, filterKind);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment,
                FRAGMENT_CONTENT_DETAIL);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    public void loadContentAugmented(String path, int index, String filterKind) {

        Intent intent = new Intent(this, ContentDetailAugmentedActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        bundle.putString("filterKind", filterKind);
        bundle.putInt("index", index);

        intent.putExtras(bundle);
        startActivity(intent);

        /*
        ContentDetailAugmentedFragment fragment = new ContentDetailAugmentedFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment.newInstance(this, path, index, filterKind),
                FRAGMENT_CONTENT_DETAIL);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        */
    }




    @Override
    public void onAddContactPressed() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, AddContactFragment.newInstance(), FRAGMENT_ADD_CONTACT);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }
    @Override
    public void onContactSelected(String idUserSender, boolean isGroupChat, boolean isDynamizer) {
        this.idUserSender = idUserSender;
        this.isGroupChat = isGroupChat;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, ChatFragment.newInstance(this, idUserSender,
                isGroupChat, isDynamizer), FRAGMENT_CHAT);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCreateDate(long date) {
        openDateEditor(CalendarNewDateFragment.IS_CREATING, date);
    }

    @Override
    public void onEditDate(int id) {
        openDateEditor(id, -1);
    }

    @Override
    public void onViewDateDetail(int id) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, CalendarDateDetailFragment.newInstance(this,
                id), FRAGMENT_CALENDAR_DATE_DETAIL);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    private void openDateEditor(int id, long date) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, CalendarNewDateFragment.newInstance(this,
                id, date), FRAGMENT_CALENDAR_NEW_DATE);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onFragmentResumed(int which) {
        currentScreen = which;
        navigationView.getMenu().getItem(which).setChecked(true);
    }

    @Override
    public void onRenewTokenFailed() {

    }

    @Override
    public void onGalleryShareButtonClicked(int idContent, String path, String metadata) {
        ArrayList<Integer> idContentArray = new ArrayList<Integer>();
        idContentArray.add(new Integer(idContent));
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> metadataList = new ArrayList<>();
        paths.add(path);
        metadataList.add(metadata);
        onGalleryShareButtonClicked(idContentArray, paths, metadataList);
    }


    @Override
    public void onSetUserMessagesAsWatched(ArrayList<Long> ids) {
        if (ids.size() > 0) {
            UserPreferences userPreferences = new UserPreferences();
            UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
            ChatMessageRest messageRest = userMessageDb.findMessage(ids.get(0));
            for (long id : ids) {
                userMessageDb.setMessageWatched(id);
                SetMessageWatchedRequest setMessageWatchedRequest = new SetMessageWatchedRequest(
                        this, id);
                setMessageWatchedRequest.doRequest(userPreferences.getAccessToken());
            }
            UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
            usersDb.setMessagesInfo(messageRest.getIdUserFrom(), 0,
                    new NotificationsDb(MyApplication.getAppContext())
                            .getNumberUnreadMissedCallNotifications(Integer.parseInt(
                                    String.valueOf(messageRest.getIdUserFrom()))),
                    Math.max(userMessageDb.getLastMessage(new UserPreferences().getUserID(),
                            messageRest.getIdUserFrom()),
                            new NotificationsDb(MyApplication.getAppContext())
                                    .getLastMissedCallTime(messageRest.getIdUserFrom())));
            Log.d("unrd","MainFragment, user:"+messageRest.getIdUserFrom()+" totalNum:"
                    +new UserMessageDb(MyApplication.getAppContext())
                    .getTotalNumberMessages(userPreferences.getUserID(), messageRest.getIdUserFrom()));
        }
    }

    @Override
    public void onSetGroupMessagesAsWatched(String chatId) {
        Log.d("grpwatched","onSetGroupMessagesAsWatched");
        PutGroupLastAccessRequest putGroupLastAccessRequest = new PutGroupLastAccessRequest(
                this, chatId);
        putGroupLastAccessRequest.doRequest(userPreferences.getAccessToken());

        GroupMessageDb groupMessageDb = new GroupMessageDb(this);
        groupMessageDb.setGroupMessageListWatchedTrue(Integer.parseInt(chatId));

        OtherUtils.updateGroupOrDynChatInfo(Integer.parseInt(chatId));
    }

    @Override
    public void onGroupTitleClicked(int chatId) {
        GroupDetailFragment fragment = GroupDetailFragment.newInstance(this, chatId);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_GROUP_DETAIL);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void openGroupChat(int groupId) {
        Dynamizer dynamizer = userGroupsDb.findDynamizerFromChatIdUnmanaged(groupId);
        if (dynamizer == null) {
            GroupRealm group = userGroupsDb.getGroupFromIdChatUnmanaged(groupId);
            if (group != null && group.isShouldShow()) {
                onContactSelected(String.valueOf(groupId), true, false);
            } else {
                launchAlertsFragment();
            }
        } else {
            if (dynamizer.isShouldShow()) {
                onContactSelected(String.valueOf(groupId), false, true);
            } else {
                launchAlertsFragment();
            }
        }
        onSetNotificationsAsWatched();
    }

    @Override
    public void openUserChat(int userId) {
        //check if user is still a contact
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        if (usersDb.userCircleORCircleUserAreNOTnull(userId)) {
            onContactSelected(String.valueOf(userId), false, false);
        } else {
            launchAlertsFragment();
        }
        onSetNotificationsAsWatched();
    }

    @Override
    public void openContacts() {
        Log.d("shre","openContacts");
        launchContactsFragment(0);
        onSetNotificationsAsWatched();
    }

    @Override
    public void openContactsGroups() {
        launchContactsFragment(BUTTON_GROUPS);
        onSetNotificationsAsWatched();
    }

    @Override
    public void openMeeting(int meetingId) {
        MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        MeetingRealm meetingRealm = meetingsDb.findMeeting(meetingId);
        if (meetingRealm != null && meetingRealm.isShouldShow()) {
            onViewDateDetail(meetingId);
        } else {
            launchAlertsFragment();
        }
        onSetNotificationsAsWatched();
    }

    @Override
    public void openCalendarDay(long date) {
        CalendarFragment fragment = CalendarFragment.newInstance(this, date);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_CALENDAR);
        ft.addToBackStack(null);
        ft.commit();
        onSetNotificationsAsWatched();
    }

    @Override
    public void openAddToCircles(String code) {
        AddContactFragment fragment = AddContactFragment.newInstance(code);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContent, fragment, FRAGMENT_ADD_CONTACT);
        ft.addToBackStack(null);
        ft.commit();
        onSetNotificationsAsWatched();
    }

    @Override
    public void onSetNotificationsAsWatched() {
        NotificationsDb notificationsDb = new NotificationsDb(MyApplication.getAppContext());
        ArrayList<NotificationRest> notificationRests = notificationsDb.getUnreadMissedCallNotifications();
        ArrayList<Integer> userIds = new ArrayList<>();
        for (NotificationRest notificationRest : notificationRests) {
            if (!userIds.contains(notificationRest.getIdUser())) userIds.add(notificationRest.getIdUser());
        }
        notificationsDb.markAllAsRead();
        //refresh unread counter for users that had missed call unread notifications
        for (int userId : userIds) {
            new UsersDb(MyApplication.getAppContext()).setMessagesInfo(userId,
                    new UserMessageDb(MyApplication.getAppContext())
                            .getNumberUnreadMessagesReceived(new UserPreferences().getUserID(),
                                    userId),
                    new NotificationsDb(MyApplication.getAppContext())
                            .getNumberUnreadMissedCallNotifications(userId),
                    Math.max(new UserMessageDb(MyApplication.getAppContext())
                                    .getLastMessage(new UserPreferences().getUserID(), userId),
                            new NotificationsDb(MyApplication.getAppContext()).getLastMissedCallTime(userId)));
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("pronoti","onNewIntent");
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            processNotificationClicked(extras);
        }
    }

    private void processNotificationClicked(Bundle extras) {
        String type = extras.getString("type");
        Log.d("pronoti","processNotificationClicked, type: "+type);
        switch (type) {
            case "MEETING_INVITATION_EVENT":
            case "MEETING_CHANGED_EVENT":
            case "MEETING_ACCEPTED_EVENT":
            case "MEETING_REJECTED_EVENT":
                openMeeting(extras.getInt("idMeeting"));
                break;
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                openCalendarDay(extras.getLong("meetingDate"));
                break;
            case "ADDED_TO_GROUP":
            case "NEW_CHAT_MESSAGE":
                Log.d("pronoti","processNotificationClicked, open group: "+extras.getInt("idGroup"));
                openGroupChat(extras.getInt("chatId"));
                break;
            case "REMOVED_FROM_GROUP":
                openContactsGroups();
                break;
            case "USER_LINKED":
            case "NEW_MESSAGE":
            case NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE:
                Log.d("pronoti","processNotificationClicked, chat idUser: "+extras.getInt("idUser"));
                openUserChat(extras.getInt("idUser"));
                break;
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                openContacts();
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                openAddToCircles(extras.getString("code"));
                break;

        }

    }

    private RateMeMaybe rmm;
    private void askForRating() {

        try {
            this.rmm = new RateMeMaybe(this);
            this.rmm.setIcon(0);

            this.rmm.setServiceUrl(Environment.getRateMeUrl());
            //rmm.forceShow();
            this.rmm.setAdditionalListener(new RateMeMaybe.OnRMMUserChoiceListener() {
                @Override
                public void handlePositive() {
                    Log.d("CV", "+");
                }

                @Override
                public void handleNeutral() {
                    Log.d("CV", "0");
                }

                @Override
                public void handleNegative() {
                    Log.d("CV", "-");
                }

                @Override
                public void handleError() {
                    Log.d("CV", "err");
                }
            });
            this.rmm.run();
            //this.rmm.forceShow();
        } catch (IllegalStateException ignored) {

            Log.e("RateMeMaybe", "Unable to perform action while saveInstanceState is performed");
        }


    }

    private void versionControl() {


        /*String jsonUrl = Environment.getVersionControlUrl();

        String language = new UserPreferences().getUserLanguage().equals(UserRegister.ESP) ? "es" : "ca";
        AlertMessageService.showMessageDialog(this, jsonUrl, language, new AlertMessageService.AlertDialogListener() {
            @Override
            public void onFailure(Exception e) {
                System.out.print("bp1");
                LoginActivity.versionControlIsPresent = false;
            }

            @Override
            public void onSuccess(boolean b) {
                System.out.print("bp1");
            }

            @Override
            public void onAlertDialogDismissed() {
                System.out.print("bp1");
                LoginActivity.versionControlIsPresent = false;
            }
        });*/
    }

    @Override
    public void onUserProfileChanged() {
        final TextView usernameTV = navigationView.getHeaderView(0).findViewById(R.id.header_textview);
        usernameTV.setText(userPreferences.getName());
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Log.d("lng", "updateConfiguration 4");

        recreate();
    }

    private void onScreenExit(){
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CHAT);
        if (chatFragment != null && chatFragment.isVisible()) {
            chatFragment.onExitScreen();
        } else if (chatFragment == null) {
            NotificationsFragment notificationsFragment = (NotificationsFragment)
                    getSupportFragmentManager().findFragmentByTag(FRAGMENT_NOTIFICATIONS);
            if (notificationsFragment != null && notificationsFragment.isVisible()) {
                notificationsFragment.onBackPressed();
            }
        }
    }

    @Override
    public void continueToApp(boolean b) {

        UserPreferences userPreferences = new UserPreferences();
        userPreferences.continueToApp(b);
        if (b){
            this.onRestart();
        }
        Log.d("versionControlAlert", "continueToAppMainFragmentManager: " + String.valueOf(b));
    }

}
