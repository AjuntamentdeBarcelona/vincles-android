package cat.bcn.vincles.mobile.UI.Register;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Business.ValidateFields;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.RegisterUserRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertPickOrTakePhoto;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.ValidateActivity.ValidateActivity;
import cat.bcn.vincles.mobile.Utils.DatePickerFragment;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static cat.bcn.vincles.mobile.Utils.OtherUtils.ALERT_TYPE_PERMISSIONS;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.MY_PERMISSIONS_REQUEST_CAMERA_PHOTO;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_IMAGE_CAPTURE;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, DatePickerFragment.DatePickerFragmentInterface, RadioGroup.OnCheckedChangeListener, RegisterUserRequest.OnResponse, AlertMessage.AlertMessageInterface, AlertPickOrTakePhoto.AlertPickOrTakePhotoInterface, AlertPickOrTakePhoto.AlertPickOrTakePhotoClosed {

    private static final int SELECT_FILE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 0;
    private static final int REGISTER_PERMISSION_REQUEST = 1;
    Locale myLocale;

    String language;
    RadioButton languageCat;
    RadioButton languageCast;
    Button registerBtn;
    EditText nameET, lastNameET, emailET, passwordET, repeatPasswordET, phoneET;
    TextView datePickedET;
    RadioGroup languageRadioGrp, genderRadioGrp, livesInBarcelonaRadioGrp;
    ImageView userAvatarIV;
    public long birthDate = Long.MAX_VALUE;
    String gender = UserRegister.MALE;
    AlertMessage alertErrorMessage, alertInfoMessage;
    AlertPickOrTakePhoto alertPickOrTakePhoto;
    Uri selectedImageUri = null;
    UserPreferences userPreferences;
    String avatar64;
    String photoMimeType;
    TextView title;
    UserRegister userRegister;
    ScrollView scrollView;
    private boolean pickPhotoDialogShown;
    private String date;
    private boolean livesInBarcelona;
    private boolean isImageErrorDialog = false;

    AlertNonDismissable alertNonDismissable;

    String avatarStoredPath = "";

    boolean ignoreLanguageChange = false;


    @Override
    protected void onPause() {
        super.onPause();
        if (languageRadioGrp != null) {
            languageRadioGrp.setOnCheckedChangeListener(null);
            genderRadioGrp.setOnCheckedChangeListener(null);
            livesInBarcelonaRadioGrp.setOnCheckedChangeListener(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_register));

        if (languageRadioGrp != null) {
            languageRadioGrp.setOnCheckedChangeListener(this);
            genderRadioGrp.setOnCheckedChangeListener(this);
            livesInBarcelonaRadioGrp.setOnCheckedChangeListener(this);
            setKeyBoardVisibilityListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.userPreferences = new UserPreferences(MyApplication.getAppContext());
        if(savedInstanceState != null ) isImageErrorDialog = savedInstanceState.getBoolean("isImageErrorDialog");
        if (getResources().getConfiguration().locale.getLanguage().contains("es") &&
                userPreferences.getUserLanguage().equals(UserRegister.CAT)
                || (getResources().getConfiguration().locale.getLanguage().contains("ca") &&
                userPreferences.getUserLanguage().equals(UserRegister.ESP))) {
            Log.d("lng","Register activity, language KOOOOO");
            setLocale(userPreferences.getUserLanguage().equals(UserRegister.CAT) ? "ca" : "es");
            recreate();
        } else {

            Log.d("lng","Register activity, String:"+getString(R.string.calendar_see_today));

            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            this.userPreferences = new UserPreferences(this);

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            scrollView = findViewById(R.id.scrollView);
            nameET = findViewById(R.id.name);
            lastNameET = findViewById(R.id.lastName);
            emailET = findViewById(R.id.email);
            passwordET = findViewById(R.id.password);
            repeatPasswordET = findViewById(R.id.repeatPassword);
            phoneET = findViewById(R.id.phone);
            phoneET.setImeOptions(EditorInfo.IME_ACTION_DONE);

            datePickedET = findViewById(R.id.timePickedTV);

            registerBtn = findViewById(R.id.registerBtn);
            languageRadioGrp = findViewById(R.id.language);

            genderRadioGrp = findViewById(R.id.gender);
            livesInBarcelonaRadioGrp = findViewById(R.id.liveInBarcelona);
            userAvatarIV = findViewById(R.id.userAvatar);

            languageCat = findViewById(R.id.catalan);
            languageCast = findViewById(R.id.spanish);

            String lang = new UserPreferences().getUserLanguage();
            if (lang.equals(UserRegister.LANGUAGE_NOT_SET)) {
                Locale current = getResources().getConfiguration().locale;
                lang = current.getLanguage();
            } else if (lang.equals(UserRegister.ESP)) {
                lang = "es";
            } else {
                lang = "cat";
            }

            if(lang.contains("es")) {
                languageRadioGrp.check(R.id.spanish);
            } else {
                languageRadioGrp.check(R.id.catalan);
            }

            //datePickedET.setOnClickListener(this);
            findViewById(R.id.dateClickListener).setOnClickListener(this);

            datePickedET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    datePickedET.setError(null);
                }
            });

            registerBtn.setOnClickListener(this);
            userAvatarIV.setOnClickListener(this);

            alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data),true);
            if (savedInstanceState != null) {
                pickPhotoDialogShown = savedInstanceState.getBoolean("pickPhotoDialogShown", false);
                avatarStoredPath = savedInstanceState.getString("avatarStoredPath", "");
                if (savedInstanceState.getString("name") != null) {
                    nameET.setText(savedInstanceState.getString("name"));
                }
                if (savedInstanceState.getString("lastname") != null) {
                    lastNameET.setText(savedInstanceState.getString("lastname"));
                }
                if (savedInstanceState.getString("email") != null) {
                    emailET.setText(savedInstanceState.getString("email"));
                }
                if (savedInstanceState.getString("phone") != null) {
                    phoneET.setText(savedInstanceState.getString("phone"));
                }
                if (savedInstanceState.getString("birthdatetext") != null) {
                    datePickedET.setText(savedInstanceState.getString("birthdatetext"));
                }
                birthDate = savedInstanceState.getLong("birthdate");
                if (savedInstanceState.getString("gender") != null && savedInstanceState.getString("gender").equals(UserRegister.FEMALE)) {
                    genderRadioGrp.check(R.id.famale);
                }

                livesInBarcelonaRadioGrp.check(savedInstanceState.getBoolean("liveInBarcelona") ? R.id.yes : R.id.no);

            }
            if (alertPickOrTakePhoto == null) {
                alertPickOrTakePhoto = new AlertPickOrTakePhoto(this, this);
                alertPickOrTakePhoto.setAlertPickOrTakePhotoClosed(this);
            }
            if (pickPhotoDialogShown) {

                if (!"".equals(avatarStoredPath)) {
                    alertPickOrTakePhoto.showMessage();
                    alertPickOrTakePhoto.showAcceptOrCancelBtns();
                    alertPickOrTakePhoto.setImagePath(avatarStoredPath);
                }
            } else {
                if (avatarStoredPath != null && !"".equals(avatarStoredPath)) {
                    ImageUtils.setImageToImageView(new File(avatarStoredPath), userAvatarIV, userAvatarIV.getContext(), false);

                    /*
                    Glide.with(userAvatarIV.getContext())
                            .load(new File(avatarStoredPath))
                            .into(userAvatarIV);
                            */
                }
            }
            if(isShowingImageErrorDialog()) {
                showImageErrorDialog();
            }

        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nameET != null) {
            outState.putString("avatarStoredPath", avatarStoredPath);
            outState.putBoolean("pickPhotoDialogShown", pickPhotoDialogShown);
            outState.putString("avatarStoredPath", avatarStoredPath);
            outState.putString("name", nameET.getText() != null ? nameET.getText().toString() : "");
            outState.putString("lastname", lastNameET.getText() != null ? lastNameET.getText().toString() : "");
            outState.putString("email", emailET.getText() != null ? emailET.getText().toString() : "");
            outState.putString("phone", phoneET.getText() != null ? phoneET.getText().toString() : "");
            outState.putString("birthdatetext", date != null ? date : "");
            outState.putLong("birthdate", birthDate);
            outState.putString("gender", gender != null ? gender : "");
            outState.putBoolean("liveInBarcelona", livesInBarcelona);
            outState.putString("photoMimeType", photoMimeType != null ? photoMimeType : "");
            outState.putBoolean("isImageErrorDialog", isShowingImageErrorDialog());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dateClickListener) {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "timePicker");
            newFragment.addDatePickerFragmentInterface(this);
        } else if (view.getId() == R.id.registerBtn) {
            checkForLoginPermissionsAndRegisterOrAsk();
        } else if (view.getId() == R.id.userAvatar) {
            alertPickOrTakePhoto = new AlertPickOrTakePhoto(this, this);
            alertPickOrTakePhoto.setAlertPickOrTakePhotoClosed(this);
            alertPickOrTakePhoto.showMessage();
        }
    }

    private void checkForLoginPermissionsAndRegisterOrAsk() {
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
            doRegister();
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {

                    //showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
                    doRegister();

            }
            else{

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE},
                        REGISTER_PERMISSION_REQUEST);
            }

        }
    }

    private void showSettingsAlert(String message) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
        alertMessage.showMessage(this,message, ALERT_TYPE_PERMISSIONS);
    }

    public void showErrorPermissions() {
        AlertMessage alertPermissions = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
                startAppSettings();
            }
        },getResources().getString(R.string.error));
        alertPermissions.showMessage(this,getString(R.string.required_permissions), ALERT_TYPE_PERMISSIONS);
    }

    public void showErrorPermissionsSettings() {
        AlertMessage alertPermissions = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
                startAppSettings();
            }
        },getResources().getString(R.string.error));
        alertPermissions.showMessage(this,getString(R.string.required_permissions_settings), "");
    }


    private void setKeyBoardVisibilityListener() {
        KeyboardVisibilityEvent.setEventListener(this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if(isOpen) {
                            scrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.scrollTo(scrollView.getScrollX(),
                                            (int) (scrollView.getScrollY()
                                                    + getResources().getDimension(R.dimen.register_padding_size)));
                                }
                            });
                        }
                    }
                });
    }


    private void doRegister() {
        int languageSelected = languageRadioGrp.getCheckedRadioButtonId();
        language = languageSelected == R.id.catalan ? UserRegister.CAT : UserRegister.ESP;

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String repeatPassword = repeatPasswordET.getText().toString();
        String name = nameET.getText().toString();
        String lastName = lastNameET.getText().toString();
        String phone = phoneET.getText().toString();
        int genderSelected = genderRadioGrp.getCheckedRadioButtonId();
        gender = genderSelected == R.id.male ? UserRegister.MALE : UserRegister.FEMALE;
        int livesInBarcelonaSelected = livesInBarcelonaRadioGrp.getCheckedRadioButtonId();
        livesInBarcelona = livesInBarcelonaSelected == R.id.yes ? true : false;

        photoMimeType = ImageUtils.getMimeType(avatarStoredPath) == null ? "image/jpeg" : ImageUtils.getMimeType(avatarStoredPath);

        Log.d("rgph", "before is valid, avatar64::"+avatar64);
        if (isValidData(birthDate, email,password,repeatPassword,name,lastName,phone)) {
            alertNonDismissable.showMessage(this);
            userRegister = new UserRegister(email, password, name, lastName, birthDate, phone, gender, livesInBarcelona, avatar64, photoMimeType);
            RegisterUserRequest registerUserRequest = new RegisterUserRequest(userRegister);
            registerUserRequest.addOnOnResponse(this);
            registerUserRequest.doRequest();
        }
    }

    @Override
    public void onDataSet(String date, long epochtime) {
        Log.d("epochtime", String.valueOf(epochtime));
        datePickedET.setText(date);
        this.date = date;
        birthDate = epochtime;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        Log.d("lng","Register on language checked change, checkedCat:"
                +(languageRadioGrp.getCheckedRadioButtonId() == R.id.catalan) + " ignoreLanguageChange?"+ignoreLanguageChange);

        if(radioGroup==languageRadioGrp && !ignoreLanguageChange){

            if(languageRadioGrp.getCheckedRadioButtonId() == R.id.catalan) {
                userPreferences.setUserLanguage(UserRegister.CAT);
                setLocale("ca");
            } else {
                userPreferences.setUserLanguage(UserRegister.ESP);
                setLocale("es");
            }
        } else if (ignoreLanguageChange) {
            ignoreLanguageChange = false;
        }
    }


    @Override
    public void onResponseRegisterUserRequest(UserRegister userRegister) {
        UserPreferences userPreferences = new UserPreferences(this);
        userPreferences.setUserLanguage(language);

        Intent intent = new Intent(this, ValidateActivity.class);
        intent.setFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TASK | */Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", emailET.getText().toString());
        intent.putExtra("password", passwordET.getText().toString());
        intent.putExtra("alias", this.userRegister.getAlias());
        intent.putExtra("name", this.userRegister.getName());
        intent.putExtra("username", this.userRegister.getUsername());
        intent.putExtra("email", this.userRegister.getEmail());
        intent.putExtra("lastname", this.userRegister.getLastname());
        intent.putExtra("birthdate", this.userRegister.getBirthdate());
        intent.putExtra("phone", this.userRegister.getPhone());
        intent.putExtra("gender", this.userRegister.getGender());
        intent.putExtra("liveInBarcelona", this.userRegister.getLiveInBarcelona());
        intent.putExtra("photoMimeType", this.userRegister.getPhotoMimeType());
        intent.putExtra("saveCredentials", ((CheckBox)findViewById(R.id.save_pwd_checkbox))
                .isChecked());
        userPreferences.setRegisterPicture(ImageUtils.getImage64(avatarStoredPath));
        alertNonDismissable.dismissSafely();
        startActivity(intent);
    }

    @Override
    public void onFailureRegisterUserRequest(Object message) {
        if (alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String error = ErrorHandler.getErrorByCode(this, message);
        alertErrorMessage.showMessage(this,error, "");
    }

    public boolean isValidData (Long birthDate, String email, String password, String repeatPassword, String name, String lastName, String phone) {
        boolean isValid = true;
        ValidateFields validateFields = new ValidateFields();
        if (!validateFields.isValididName(name)) {
            nameET.setError(getString(R.string.not_valid_name));
            isValid = false;
        } else if (!validateFields.isValididLastName(lastName)) {
            lastNameET.setError(getString(R.string.not_valid_last_name));
            isValid = false;
        } else if (!validateFields.isValididEmail(email)) {
            emailET.setError(getString(R.string.not_valid_email));
            isValid = false;
        } else if (!validateFields.isPasswordMaching(password,repeatPassword)) {
            passwordET.setError(getString(R.string.error_passwords_repeat));
            isValid = false;
        } else if (!validateFields.isValidPhone(phone) ) {
            phoneET.setError(getString(R.string.error_1113));
            isValid = false;
        } else if (!validateFields.isEmptyBirthDate(birthDate)) {
            //datePickedET.requestFocus();
            datePickedET.setError(getString(R.string.not_valid_date));
            isValid = false;
        } else if (!validateFields.isValidBirthDate(birthDate)) {
            datePickedET.requestFocus();
            datePickedET.setError(getString(R.string.error_1114));
            isValid = false;
        } else if (avatarStoredPath == null || "".equals(avatarStoredPath)) {
            alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
            alertErrorMessage.showMessage(this,getResources().getString(R.string.required_photo), "");
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        switch (type){
            case ALERT_TYPE_PERMISSIONS:
                alertMessage.alert.cancel();
                startAppSettings();
                break;
            default:
                alertMessage.alert.cancel();
        }
        if (alertMessage.equals(alertErrorMessage)) {
            alertErrorMessage.alert.cancel();
        } else if (alertMessage.equals(alertInfoMessage)) {
            alertInfoMessage.alert.cancel();
            finish();
        }
    }

    private void startAppSettings() {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        startActivity(i);
    }

    @Override
    public void onTakePhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        doTakePhotoAction();
    }

    private void doTakePhotoAction() {
        //Disable rotation while taking the photo
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        avatarStoredPath = OtherUtils.sendPhotoIntent(this, true);
    }

    private void doPickPhotoAction() {
        SharedPreferences sp = this.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("SHOWING_CAMERA", true);
        editor.commit();

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    public void onPickPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doPickPhotoAction();
        } else {
            boolean ask = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SharedPreferences sp = this.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                if(!sp.getBoolean("READ_EXTERNAL_STORAGE", false)){
                    ask = true;
                }
                else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ask = false;
                    }
                }
            }

            if(ask){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL);
                SharedPreferences sp = this.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("READ_EXTERNAL_STORAGE", true);
                editor.commit();
            }
            else {
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doPickPhotoAction();
            }
            else{
                showErrorPermissions();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                doTakePhotoAction();
            }
            else{
                showErrorPermissions();
            }
        }
        if (requestCode == REGISTER_PERMISSION_REQUEST) {
            doRegister();
            /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
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
                checkForLoginPermissionsAndRegisterOrAsk();
            }
            else{
                showErrorPermissions();
            }*/
        }
    }

    @Override
    public void onAcceptPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        userPreferences.setUserAvatar(avatarStoredPath);
        //userAvatarIV.setImageURI(selectedImageUri);
        if(avatarStoredPath != null && !"".equals(avatarStoredPath)) {
            ImageUtils.setImageToImageView(new File(avatarStoredPath), userAvatarIV, userAvatarIV.getContext(), false);
        }
        pickPhotoDialogShown = false;
        alertPickOrTakePhoto.close();
    }

    @Override
    public void onCancelPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.hideAccpetOrCancelBtns();
        alertPickOrTakePhoto.resetImage();
        File file = new File(avatarStoredPath);
        if (file.exists()) {
            file.delete();
        }
        avatarStoredPath = "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                pickPhotoDialogShown = true;
                alertPickOrTakePhoto.showAcceptOrCancelBtns();
                try {
                    avatarStoredPath = ImageUtils.decodeFile(avatarStoredPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                alertPickOrTakePhoto.setImagePath(avatarStoredPath);
                photoMimeType = ImageUtils.getMimeType(avatarStoredPath);
                //Enable rotation again
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        } else if (requestCode == SELECT_FILE) {
            if (resultCode == RESULT_OK) {
                boolean showResultDialog = true;
                pickPhotoDialogShown = true;
                selectedImageUri = data.getData();
                try {
                    avatarStoredPath = ImageUtils.decodeFile(ImageUtils.getRealPathFromURI(selectedImageUri,
                            this));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showResultDialog = false;
                    showImageErrorDialog();
                }
                Log.d("rgph", "Select photo  ourpath:"+avatarStoredPath);
                if(showResultDialog) {
                    alertPickOrTakePhoto.showAcceptOrCancelBtns();
                    alertPickOrTakePhoto.setImagePath(avatarStoredPath);
                    ContentResolver cR = getContentResolver();
                    photoMimeType = cR.getType(selectedImageUri);
               }

            }
        }
    }

    public void setLocale(String lang) {
        Log.d("lng","Register set locale, lang:" + lang);
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Log.d("lng", "updateConfiguration 3");

        recreateActivityCompat(this);
    }

    @SuppressLint("NewApi")
    public static final void recreateActivityCompat(final Activity a) {

        a.recreate();

    }

    @Override
    public void onClosed() {
        pickPhotoDialogShown = false;
        avatarStoredPath = "";
    }

    public boolean isShowingImageErrorDialog() {
        return isImageErrorDialog;
    }

    public void showImageErrorDialog() {
        isImageErrorDialog = true;
        AlertMessage alertMessage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.dismissSafely();
                isImageErrorDialog = false;
            }
        }, AlertMessage.TITTLE_ERROR);
        alertMessage.setDismissMessageInterface(new AlertMessage.DismissMessageInterface() {
            @Override
            public void onDismissAlertMessage() {
                isImageErrorDialog = false;
            }
        });
        String errorMsg = getString(R.string.error_opening_image);
        alertMessage.showMessage(this,errorMsg, "");
    }
}
