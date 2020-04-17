package cat.bcn.vincles.mobile.UI.ValidateActivity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import cat.bcn.vincles.mobile.Client.Business.Firebase.FirebaseInstanceIDListener;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetAuthenticatedUserDataRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.LoginRequest;
import cat.bcn.vincles.mobile.Client.Requests.RegisterUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.UpdateUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.ValidateNewUserRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ValidateActivity extends BaseActivity implements View.OnClickListener, ValidateNewUserRequest.OnResponse, AlertMessage.AlertMessageInterface,  BaseRequest.RenewTokenFailed,
        LoginRequest.OnResponse, GetAuthenticatedUserDataRequest.OnResponse, GetUserPhotoRequest.OnResponse, RegisterUserRequest.OnResponse, UpdateUserRequest.OnResponse {

    EditText code;
    AlertMessage alertErrorMessage, alertInfoMessage;
    String user, password;
    UserRegister userRegister;

    GetAuthenticatedUserDataRequest getAuthenticatedUserDataRequest;
    UserPreferences userPreferences;

    Button validateButton;
    TextView sendAgain;

    AlertMessage alertMessage;
    AlertNonDismissable alertNonDismissable;

    boolean saveCredentials;

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_validate_user));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate);

        user = getIntent().getExtras().getString("user");
        password = getIntent().getExtras().getString("password");
        String name = getIntent().getExtras().getString("name");
        String username = getIntent().getExtras().getString("username");
        String email = getIntent().getExtras().getString("email");
        String lastname = getIntent().getExtras().getString("lastname");
        long birthdate = getIntent().getExtras().getLong("birthdate");
        String phone = getIntent().getExtras().getString("phone");
        String gender = getIntent().getExtras().getString("gender");
        boolean liveInBarcelona = getIntent().getExtras().getBoolean("liveInBarcelona");
        String photoMimeType = getIntent().getExtras().getString("photoMimeType");
        saveCredentials = getIntent().getExtras().getBoolean("saveCredentials");
        userRegister = new UserRegister(user, password, name, lastname, birthdate, phone, gender,
                liveInBarcelona, "", photoMimeType);
        Log.d("vld","user register name:"+userRegister.getName());
        Log.d("vld","user register birth:"+userRegister.getBirthdate());
        Log.d("vld","user register phone:"+userRegister.getPhone());
        Log.d("vld","user register bcn:"+userRegister.getLiveInBarcelona());
        userPreferences = new UserPreferences(this);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        validateButton = findViewById(R.id.validate);
        sendAgain = findViewById(R.id.send_again);
        validateButton.setOnClickListener(this);
        sendAgain.setOnClickListener(this);

        code = findViewById(R.id.code);
        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidData(s.toString())) {
                    code.setError(null);
                    validateButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    code.setCompoundDrawables(null,null,null,null);
                } else {
                    validateButton.setBackgroundColor(getResources().getColor(R.color.redNew));
                    setErrorWithoutText();
                    code.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (!isValidData(code.getText().toString())) {
            setErrorWithoutText();
            validateButton.setBackgroundColor(getResources().getColor(R.color.redNew));
        }

        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
    }

    private void setErrorWithoutText() {
        Drawable dr = getResources().getDrawable(android.R.drawable.stat_notify_error);
        Drawable wrapDrawable = DrawableCompat.wrap(dr);
        DrawableCompat.setTint(wrapDrawable, getResources().getColor(R.color.colorPrimary));
        wrapDrawable.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        code.setCompoundDrawables(null,null,wrapDrawable,null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.validate:
                String codeString = code.getText().toString();
                if (isValidData(codeString)) {
                    alertNonDismissable.showMessage(this);
                    Log.d("vld","validate code string:"+codeString);
                    if (codeString.length() == 5) {
                        codeString = codeString.substring(1);
                    }
                    ValidateNewUserRequest validateNewUserRequest = new ValidateNewUserRequest(user, codeString);
                    validateNewUserRequest.addOnOnResponse(this);
                    validateNewUserRequest.doRequest();
                } else {
                    code.setCompoundDrawables(null,null,null,null);
                    this.code.setError(getString(R.string.validate_error));
                }
                break;
            case R.id.send_again:
                alertNonDismissable.showMessage(this);
                RegisterUserRequest registerUserRequest = new RegisterUserRequest(userRegister);
                registerUserRequest.addOnOnResponse(this);
                registerUserRequest.doRequest();
                break;
        }

    }

    public boolean isValidData(String code) {
        return code.length() > 3 && code.length() < 6;
    }


    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        if (alertMessage.equals(alertErrorMessage)) {
            alertErrorMessage.alert.cancel();
        } else if (alertMessage.equals(alertInfoMessage)) {
            alertInfoMessage.alert.cancel();
        }
    }

    @Override
    public void onResponseValidateNewUserRequest(JsonObject responseBody) {
        Log.d("vld","onResponseValidateNewUserRequest");
        LoginRequest loginRequest = new LoginRequest(user, password);
        loginRequest.addOnOnResponse(this);
        loginRequest.doRequest();
    }

    @Override
    public void onResponseUpdateUserRequest(JSONObject userRegister) {
        Log.d("vld","onResponseUpdateUserRequest");
        getAuthenticatedUserDataRequest = new GetAuthenticatedUserDataRequest();
        getAuthenticatedUserDataRequest.addOnOnResponse(this);
        getAuthenticatedUserDataRequest.doRequest(new UserPreferences().getAccessToken());
        alertNonDismissable.changeMessage(getResources().getString(R.string.login_loading_user_info));
    }

    @Override
    public void onFailureUpdateUserRequest(Object error) {
        Log.d("vld","onFailureUpdateUserRequest:"+error);

    }

    @Override
    public void onFailureValidateNewUserRequest(Object error) {
        Log.d("vld","onFailureValidateNewUserRequest:"+error);
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(this, error);
        alertErrorMessage.showMessage(this,errorMsg,"");
    }

    @Override
    public void onResponseLoginRequest(TokenFromLogin tokenFromLogin) {
        Log.d("lgn","onResponseLoginRequest");
        userPreferences.setScope(tokenFromLogin.getScope());
        userPreferences.setTokenType(tokenFromLogin.getTokenType());
        userPreferences.setExpiresIn(tokenFromLogin.getExpiresIn());
        userPreferences.setRefreshToken(tokenFromLogin.getRefreshToken());
        userPreferences.setAccessToken(tokenFromLogin.getAccessToken());

        FirebaseInstanceIDListener.forceRefreshToken(MyApplication.getAppContext());

        UserRegister userChange = new UserRegister(userRegister.getAlias(), userRegister.getName(),
                userRegister.getLastname(), userRegister.getBirthdate(), userRegister.getEmail(),
                userRegister.getPhone(), userRegister.getGender(), userRegister.getLiveInBarcelona(),
                userPreferences.getRegisterPicture(), userRegister.getPhotoMimeType());
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(this, userChange);
        updateUserRequest.addOnOnResponse(this);
        updateUserRequest.doRequest(userPreferences.getAccessToken());


    }

    @Override
    public void onFailureLoginRequest(Object error) {
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        showError(error);
    }

    @Override
    public void onResponseGetAuthenticatedUserDataRequest(GetUser userRegister) {
        userPreferences.setUserID(userRegister.getId());
        userPreferences.setIdInstallation(userRegister.getIdInstallation());
        userPreferences.setIdCircle(userRegister.getIdCircle());
        userPreferences.setIdLibrary(userRegister.getIdLibrary());
        userPreferences.setIdCalendar(userRegister.getIdCalendar());
        userPreferences.setAlias(userRegister.getAlias());
        userPreferences.setUsername(userRegister.getUsername());
        userPreferences.setName(userRegister.getName());
        userPreferences.setLastName(userRegister.getLastname());
        userPreferences.setEmail(userRegister.getEmail());
        userPreferences.setBirthdate(userRegister.getBirthdate());
        userPreferences.setPhone(userRegister.getPhone());
        userPreferences.setGender(userRegister.getGender());
        userPreferences.setLivesInBarcelona(userRegister.getLiveInBarcelona());
        userPreferences.setIsUserSenior(userRegister.getIdCircle() != -1);
        userPreferences.setUserAvatar(userPreferences.getRegisterPicture());
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }

       GetUser getUser =  userPreferences.getUser();

        saveUser(userRegister);
        String userID = String.valueOf(userPreferences.getUserID());
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(null, userID);
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(userPreferences.getAccessToken());

    }

    private void saveUser(GetUser userRegister) {
        UsersDb usersDb = new UsersDb(this);
        usersDb.saveGetUserIfNotExists(userRegister);
    }

    @Override
    public void onFailureGetAuthenticatedUserDataRequest(Object error) {
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        showError(error);
    }


    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        userPreferences.setUserAvatar(photo.getPath());
        if (saveCredentials) {
            OtherUtils.saveAccount(user, password, AccountManager.get(this));
        } else {
            OtherUtils.deleteAccountIfExisting(AccountManager.get(this));
        }
        startMainFragmentManagerActivity();
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        startMainFragmentManagerActivity();
    }

    public void startMainFragmentManagerActivity() {
        Intent intent = new Intent(this,MainFragmentManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void showError(Object error) {
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(this, error);
        alertMessage.showMessage(this,errorMsg,"");
    }

    @Override
    public void onResponseRegisterUserRequest(UserRegister userRegister) {
        Log.d("vld","onResponseRegisterUserRequest");
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
        String errorMsg = getString(R.string.validate_resend_email_ok);
        alertErrorMessage.showMessage(this,errorMsg,"");
    }

    @Override
    public void onFailureRegisterUserRequest(Object error) {
        Log.d("vld","failure register, error:"+error.toString());
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = getString(R.string.validate_resend_email_error);
        alertErrorMessage.showMessage(this,errorMsg,"");
    }

    @Override
    public void onRenewTokenFailed() {
        AlertMessage alertMessage = new AlertMessage(this, getResources().getString(R.string.close_session));
        alertMessage.showMessage(this, getResources().getString(R.string.error_default), "renewTokenError");
    }
}
