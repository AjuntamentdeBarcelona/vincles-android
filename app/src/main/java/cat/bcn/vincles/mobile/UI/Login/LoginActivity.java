package cat.bcn.vincles.mobile.UI.Login;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.tempos21.versioncontrol.service.AlertMessageService;

import cat.bcn.vincles.mobile.Client.Db.DatabaseUtils;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Migration.MigrationDataDeleter;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.GetAuthenticatedUserDataRequest;
import cat.bcn.vincles.mobile.Client.Requests.LoginRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.RecoverPassword.RecoverPasswordActivity;
import cat.bcn.vincles.mobile.UI.Register.RegisterActivity;
import cat.bcn.vincles.mobile.UI.Splash.FragmentSplashScreen;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener,
        LoginRequest.OnResponse, AlertMessage.AlertMessageInterface, LoginDataDownloader.OnResult {

    public static final String DATA_DOWNLOADER_FRAGMENT = "data_downloader_fragment";
    private static final int LOGIN_PERMISSIONS_REQUEST = 0;

    public static int screenOrientation = -1;
    public static boolean versionControlIsPresent = false;


    Button registerUserBtn, loginUserBtn;
    CheckBox checkBox;
    EditText emailET, passwordET;
    TextView recoverPasswrodTV;
    AlertMessage alertMessage;
    GetAuthenticatedUserDataRequest getAuthenticatedUserDataRequest;
    UserPreferences userPreferences;
    AlertNonDismissable alertNonDismissable;
    String alertText;
    LoginDataDownloader loginDataDownloader;

    //in on register it can be changed. When coming back it may not update automatically
    //so keep track to update manually
    String language;

    boolean isDoingMigration = false;

    /**
     * Code to use the Account Authenticator, copied from AccountAuthenticatorActivity
     */
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    public void checkLoginFields () {
        if(!emailET.getText().toString().matches("") && !passwordET.getText().toString().matches("")){
            loginUserBtn.setEnabled(true);
            ViewCompat.setBackgroundTintList(loginUserBtn, null);
        } else {
            loginUserBtn.setEnabled(false);
            ViewCompat.setBackgroundTintList(loginUserBtn, ColorStateList.valueOf(getResources().getColor(R.color.redNew)));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("alertText", alertText);
        outState.putBoolean("isDoingMigration", isDoingMigration);
        LoginActivity.screenOrientation = this.getResources().getConfiguration().orientation;
        outState.putInt("screenOrientation",LoginActivity.screenOrientation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alertNonDismissable.dismissSafely();
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this, getResources().getString(R.string.tracking_login));

        Log.d("lng","login activity, onResume lang: "+getResources().getConfiguration().locale
                + " settingsLang:"+userPreferences.getUserLanguage());
        if (!language.equals(getResources().getConfiguration().locale.getLanguage())) {
            recreate();
        }
       /* if ((LoginActivity.screenOrientation == this.getResources().getConfiguration().orientation) && !LoginActivity.versionControlIsPresent) {
            this.versionControl();
            LoginActivity.versionControlIsPresent = true;
        }*/
        LoginActivity.screenOrientation = this.getResources().getConfiguration().orientation;
        OtherUtils.cancelProcessingNotifications(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Code to use the Account Authenticator, copied from AccountAuthenticatorActivity
         */
        new Environment(this.getApplicationContext());

        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }

        setContentView(R.layout.activity_login);

        if (savedInstanceState != null) {
            alertText = savedInstanceState.getString("alertText");
            isDoingMigration = savedInstanceState.getBoolean("isDoingMigration");
        }else{
            LoginActivity.screenOrientation = this.getResources().getConfiguration().orientation;
        }

        userPreferences = new UserPreferences(this);

        emailET = findViewById(R.id.email);
        passwordET = findViewById(R.id.password);
        recoverPasswrodTV = findViewById(R.id.recoverPassword);
        registerUserBtn = findViewById(R.id.registerUser);
        loginUserBtn = findViewById(R.id.login);
        checkBox = findViewById(R.id.save_pwd_checkbox);

        registerUserBtn.setOnClickListener(this);
        loginUserBtn.setOnClickListener(this);
        recoverPasswrodTV.setOnClickListener(this);

        emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLoginFields();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLoginFields();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        alertNonDismissable = new AlertNonDismissable("", true);
        if (alertText != null && alertText.length() > 0) {
            alertNonDismissable.showMessage(this, alertText);
        }


        loginDataDownloader = (LoginDataDownloader)
                getSupportFragmentManager().findFragmentByTag(DATA_DOWNLOADER_FRAGMENT);
        if (loginDataDownloader == null || savedInstanceState == null) {
            loginDataDownloader = new LoginDataDownloader();
            loginDataDownloader.initUserDataDownloader(userPreferences, this);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(loginDataDownloader, DATA_DOWNLOADER_FRAGMENT).commit();
        } else {
            loginDataDownloader.initUserDataDownloader(userPreferences, this);
        }

        Log.d("lng","login activity, onCreate lang: "+getResources().getConfiguration().locale
                + " settingsLang:"+userPreferences.getUserLanguage());
        language = getResources().getConfiguration().locale.getLanguage();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.getString("username");
            String pwd = extras.getString("password");
            if (username != null && username.length() > 0 && pwd != null && pwd.length() > 0) {
                isDoingMigration = true;
                LoginRequest loginRequest = new LoginRequest(username, pwd);
                loginRequest.addOnOnResponse(this);
                loginRequest.doRequest();
            }
        }

        if (isDoingMigration) {
            if (getSupportActionBar() != null) getSupportActionBar().hide();
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        }

        if (isDoingMigration) {
            FragmentSplashScreen fragmentSplashScreen = new FragmentSplashScreen();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.splash_frame, fragmentSplashScreen);
            transaction.commit();
        }


        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        if (accounts != null && accounts.length > 0) {
            emailET.setText(accounts[0].name);
            passwordET.setText(accountManager.getPassword(accounts[0]));
            checkBox.setChecked(true);
        }


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    OtherUtils.deleteAccountIfExisting(AccountManager.get(LoginActivity.this));
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.registerUser) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.login) {
            checkForLoginPermissionsAndLoginOrAsk();
        } else if (view.getId() == R.id.recoverPassword) {
            Intent intent = new Intent(this, RecoverPasswordActivity.class);
            startActivity(intent);
        }
    }

    private void checkForLoginPermissionsAndLoginOrAsk() {



        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                        || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_NETWORK_STATE)
                        == PackageManager.PERMISSION_GRANTED)) {
            OtherUtils.hideKeyboard(this);
            doLogin();
        } else {

            boolean ask = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SharedPreferences sp = this.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);

                Log.d("RECORD_AUDIO", String.valueOf(shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)));
                Log.d("CAMERA", String.valueOf(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)));

                if(!sp.getBoolean("RECORD_AUDIO", false) ||
                        !sp.getBoolean("CAMERA", false) ||
                        !sp.getBoolean("WRITE_EXTERNAL_STORAGE", false)
                        || !sp.getBoolean("READ_PHONE_STATE", false)){
                    ask = true;
                }
                else{
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ||
                            !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                            !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            ||
                            !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {

                        ask = false;
                    }
                }
            }

            if(ask){
                SharedPreferences sp = this.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("RECORD_AUDIO", true);
                editor.putBoolean("CAMERA", true);
                editor.putBoolean("WRITE_EXTERNAL_STORAGE", true);
                editor.putBoolean("READ_PHONE_STATE", true);
                editor.commit();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE},
                        LOGIN_PERMISSIONS_REQUEST);
            }
            else{
                doLogin();
               // showErrorPermissionsSettings();

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED &&
                (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                        || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_NETWORK_STATE)
                        == PackageManager.PERMISSION_GRANTED)) {
            checkForLoginPermissionsAndLoginOrAsk();
        }
        else{
            doLogin();
          //  showErrorPermissions();
        }
    }

    private void doLogin() {
        DatabaseUtils.dropAllTables();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        LoginRequest loginRequest = new LoginRequest(email, password);
        loginRequest.addOnOnResponse(this);
        loginRequest.doRequest();
        alertText = getResources().getString(R.string.login_sending_data);
        alertNonDismissable.showMessage(this, alertText);
    }

    @Override
    public void onResponseLoginRequest(TokenFromLogin tokenFromLogin) {
        Log.d("lgn","onResponseLoginRequest");
        if (isDoingMigration) {
            //deleteData data now that login succeeded
            MigrationDataDeleter.deleteData(this);

            userPreferences.setFirstTimeUserAccessApp(false);
        }

        userPreferences.setScope(tokenFromLogin.getScope());
        userPreferences.setTokenType(tokenFromLogin.getTokenType());
        userPreferences.setExpiresIn(tokenFromLogin.getExpiresIn());
        userPreferences.setRefreshToken(tokenFromLogin.getRefreshToken());
        userPreferences.setAccessToken(tokenFromLogin.getAccessToken());

        if (checkBox != null && checkBox.isChecked()) {
            OtherUtils.saveAccount(emailET.getText().toString(), passwordET.getText().toString(),
                       AccountManager.get(this));

        }

        loginDataDownloader.doRequests();

        if (!isDoingMigration) {
            alertText = getResources().getString(R.string.login_loading_user_info);
            alertNonDismissable.changeMessage(alertText);
        }
        Log.d("lgn","onResponseLoginRequest");
    }

    @Override
    public void onFailureLoginRequest(Object error) {
        Log.d("lgn","onFailureLoginRequest error:"+error);
        alertText = null;
        if (alertNonDismissable != null) alertNonDismissable.dismissSafely();
        MigrationDataDeleter.deleteData(this);
        userPreferences.setFirstTimeUserAccessApp(false);
        if (isDoingMigration) {
            isDoingMigration = false;
            findViewById(R.id.splash_frame).setVisibility(View.GONE);

            getSupportActionBar().show();
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        } else {
            showError(error);
        }
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    private void showError(Object error) {
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(this, error);
        alertMessage.showMessage(this,errorMsg,"");
    }

    public void showErrorPermissions() {
        AlertMessage alertPermissions = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
            }
        },getResources().getString(R.string.error));
        alertPermissions.showMessage(this,getString(R.string.required_permissions), "");
    }

    public void showErrorPermissionsSettings() {
        AlertMessage alertPermissions = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
            }
        },getResources().getString(R.string.error));
        alertPermissions.showMessage(this,getString(R.string.required_permissions_settings), "");
    }

    private void startMainFragmentManagerActivity() {
        Intent intent = new Intent(this,MainFragmentManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onError(Object error) {
        alertText = null;
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(loginDataDownloader).commit();
        loginDataDownloader = new LoginDataDownloader();
        loginDataDownloader.initUserDataDownloader(userPreferences, this);
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(loginDataDownloader, DATA_DOWNLOADER_FRAGMENT).commit();

        if (isDoingMigration) {
            isDoingMigration = false;
            findViewById(R.id.splash_frame).setVisibility(View.GONE);

            getSupportActionBar().show();
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        } else {
            showError(error);
        }

    }

    @Override
    public void onFinished() {
        alertText = null;
        if (alertNonDismissable.alert!=null) {
            alertNonDismissable.dismissSafely();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(loginDataDownloader).commitAllowingStateLoss();
        userPreferences.setLoginDataDownloaded(true);
        startMainFragmentManagerActivity();
    }


    /**
     *
     *
     * Code to use the Account Authenticator, copied from AccountAuthenticatorActivity
     *
     * It is recommended to extend that AccountAuthenticatorActivity, but then LoginActivity would
     * not extend AppCompatActivity nor BaseActivity.
     *
     *
     */

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }


    private void versionControl() {
        new Environment(getApplicationContext());
        String jsonUrl = Environment.getVersionControlUrl();

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
        });
    }

    public void dismissAlert(){
        Log.d("renewTokenFailed", "dissmiss alert");
        if (alertNonDismissable!=null)
            alertNonDismissable.dismissSafely();
    }
}
