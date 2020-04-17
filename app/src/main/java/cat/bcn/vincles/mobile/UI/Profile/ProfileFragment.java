package cat.bcn.vincles.mobile.UI.Profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.UpdateUserRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertPickOrTakePhoto;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.CompoundButtonListener;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.app.Activity.RESULT_OK;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_IMAGE_CAPTURE;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, AlertPickOrTakePhoto.AlertPickOrTakePhotoInterface, UpdateUserRequest.OnResponse, AlertMessage.AlertMessageInterface, RadioGroup.OnCheckedChangeListener, AlertPickOrTakePhoto.AlertPickOrTakePhotoClosed {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 0;
    private static final int CALENDAR_PERMISSION_REQUEST = 1;

    private static final int SELECT_FILE = 2;
    private static final String ALERT_TYPE_PERMISSIONS = "SETTINGS_PERMISSIONS";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 3;
    private static final int MY_PERMISSIONS_REQUEST_DOWNLOAD= 4;
    private static final int MY_PERMISSIONS_REQUEST_GALLERY = 5;

    private OnFragmentInteractionListener mListener;
    ImageView userAvatarView;
    ImageButton editData;
    TextView dataTV;
    Uri selectedImageUri = null;
    String pathAvatar;
    AlertPickOrTakePhoto alertPickOrTakePhoto;
    UserPreferences userPreferences;
    AlertMessage alertMessage;
    AlertNonDismissable alertNonDismissable;
    AlertNonDismissable alertWaitCalendarSync;
    RadioGroup languageRadioGroup, textSizeRadioGroup, autodownloadRadioGroup,
            syncCalendarRadioGroup, copyPhotosGalleryRadioGroup;
    RadioButton languageCat, languageCast;

    String pathToPhotoTaken;
    boolean repeatingCalendarPermissionsRequest = false;

    boolean alertPhotoShowing;

    boolean disableSyncClick = false;
    private boolean isImageErrorDialog = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(FragmentResumed listener) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONFIGURATION);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("alertPhotoShowing", alertPhotoShowing);
        outState.putBoolean("isImageErrorDialog", isShowingImageErrorDialog());
        outState.putString("pathAvatar", pathAvatar);
        outState.putString("pathToPhotoTaken", pathToPhotoTaken);

    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_config));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(getContext());
        if(savedInstanceState != null ){
            isImageErrorDialog = savedInstanceState.getBoolean("isImageErrorDialog");
            pathToPhotoTaken = savedInstanceState.getString("pathToPhotoTaken");

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        editData = v.findViewById(R.id.personal_data_button);
        userAvatarView = v.findViewById(R.id.userAvatar);
        dataTV = v.findViewById(R.id.personal_data);
        languageRadioGroup = v.findViewById(R.id.language);
        autodownloadRadioGroup = v.findViewById(R.id.auto_download);
        copyPhotosGalleryRadioGroup = v.findViewById(R.id.copy_photos_gallery);
        syncCalendarRadioGroup = v.findViewById(R.id.sync_calendar);
        textSizeRadioGroup = v.findViewById(R.id.text_size);
        languageCat = v.findViewById(R.id.catalan);
        languageCast = v.findViewById(R.id.spanish);
        View backButton = v.findViewById(R.id.back);

        alertPickOrTakePhoto = new AlertPickOrTakePhoto(getActivity(), this);
        alertPickOrTakePhoto.setAlertPickOrTakePhotoClosed(this);

        alertNonDismissable = new AlertNonDismissable(getResources().getString(
                R.string.login_sending_data),true);
        alertWaitCalendarSync = new AlertNonDismissable(getResources().getString(
                R.string.processing),true);

        if (backButton != null) backButton.setOnClickListener(this);
        editData.setOnClickListener(this);
        userAvatarView.setOnClickListener(this);

        String email = userPreferences.getEmail() == null ? "" : userPreferences.getEmail();
        dataTV.setText(userPreferences.getName() + "\n" +
                userPreferences.getLastName() + "\n" +
                userPreferences.getUsername() + "\n" +
                email + "\n" +
                userPreferences.getPhone() +
                (userPreferences.livesInBarcelona() ?
                        "\n" + getResources().getString(R.string.configuration_lives_barcelona) :
                ""));


        String language = userPreferences.getUserLanguage();
        Configuration conf = getResources().getConfiguration();
        if (userPreferences.getUserLanguage().equals(UserRegister.ESP) ||
                (language.equals(UserRegister.LANGUAGE_NOT_SET) && getLocale(conf).contains("es"))) {
            languageRadioGroup.check(R.id.spanish);
        } else {
            languageRadioGroup.check(R.id.catalan);
        }
        languageRadioGroup.setOnCheckedChangeListener(this);

        if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_SMALL) {
            textSizeRadioGroup.check(R.id.small);
        } else if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_BIG) {
            textSizeRadioGroup.check(R.id.big);
        } else {
            textSizeRadioGroup.check(R.id.medium);
        }
        textSizeRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsAutodownload()) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                autodownloadRadioGroup.check(R.id.auto_download_yes);
                userPreferences.setIsAutodownload(true);
            }
            else{
                autodownloadRadioGroup.check(R.id.auto_download_no);
                userPreferences.setIsAutodownload(false);
            }
        } else {
            autodownloadRadioGroup.check(R.id.auto_download_no);
        }
        autodownloadRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsCopyPhotos()) {
            copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_yes);
        } else {
            copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_no);
        }
        copyPhotosGalleryRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsSyncCalendars()) {
            syncCalendarRadioGroup.check(R.id.sync_calendar_yes);
        } else {
            syncCalendarRadioGroup.check(R.id.sync_calendar_no);
        }
        syncCalendarRadioGroup.setOnCheckedChangeListener(calendarRadioGroupListener);
        calendarRadioGroupListener.enable();

        pathAvatar = userPreferences.getUserAvatar();
        if (!pathAvatar.equals("")) {
            Uri avatarUri = Uri.parse(pathAvatar);
            userAvatarView.setImageURI(avatarUri);
        }

        if (savedInstanceState != null) {
            alertPhotoShowing = savedInstanceState.getBoolean("alertPhotoShowing", alertPhotoShowing);
            pathToPhotoTaken  = savedInstanceState.getString("pathToPhotoTaken", "");
        }
        if (alertPhotoShowing) {
            alertPickOrTakePhoto.showMessage();
            alertPhotoShowing = true;
            if (!pathAvatar.equals("")) {
                Uri myUri = Uri.parse(pathAvatar);
                alertPickOrTakePhoto.setImage(myUri);
            }
        }
        if(isShowingImageErrorDialog()) {
            showImageErrorDialog();
        }

        return v;
    }

    CompoundButtonListener calendarRadioGroupListener = new CompoundButtonListener() {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            super.onCheckedChanged(radioGroup, checkedId);
            Log.d("onSyncCalendarChanged", "listener isEnabled: " + String.valueOf(isEnabled()));

            onSyncCalendarChanged(checkedId == R.id.sync_calendar_yes);
        }
    };

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private String getLocale(Configuration conf) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return conf.getLocales().get(0).toString();
        } else {
            return conf.locale.toString();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userAvatar:

                alertPickOrTakePhoto.showMessage();
                alertPhotoShowing = true;
                if (!pathAvatar.equals("")) {
                    Uri myUri = Uri.parse(pathAvatar);
                    alertPickOrTakePhoto.setImage(myUri);
                }
                break;
            case R.id.personal_data_button:
                if (mListener != null) mListener.onProfileEditClicked();
                break;
            case R.id.back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    private void sendPhoto() {
        String alias = userPreferences.getAlias();
        String email = userPreferences.getEmail();
        String name = userPreferences.getName();
        String lastname = userPreferences.getLastName();
        long birthdate = userPreferences.getBirthdate();
        String phone = userPreferences.getPhone();
        String gender = userPreferences.getGender();
        boolean livesInBarcelona = userPreferences.livesInBarcelona();

        String photo = ImageUtils.getImage64(pathAvatar);
        String photoMimeType = ImageUtils.getMimeType(pathAvatar);

        String accessToken = userPreferences.getAccessToken();
        UserRegister userRegister = new UserRegister( alias, name, lastname, birthdate, email,
                phone, gender, livesInBarcelona, photo, photoMimeType);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest((BaseRequest.RenewTokenFailed) getActivity(), userRegister);
        updateUserRequest.addOnOnResponse(this);
        updateUserRequest.doRequest(accessToken);
    }

    @Override
    public void onTakePhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {


        if(OtherUtils.isLowMemory()){
            showErrorMessage("2804");
            return;
        }

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            makePhoto();
        }
        else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_camera_photo));
            }else{
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
            }

        }

    }

    private void makePhoto() {
        LoginActivity.screenOrientation = -1;

       alertPickOrTakePhoto.close();
       pathToPhotoTaken = OtherUtils.sendPhotoIntent(this, false);
    }

    private void showSettingsAlert(String message) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
        alertMessage.showMessage(getActivity(),message, ALERT_TYPE_PERMISSIONS);
    }

    public void showErrorMessage(Object error) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }

    @Override
    public void onPickPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.close();
        LoginActivity.screenOrientation = -1;

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doPickPhotoAction();
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doPickPhotoAction();
        } else if (requestCode == CALENDAR_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doCalendarSync();
        } else if (requestCode == CALENDAR_PERMISSION_REQUEST) {
            programmaticallyChangeCalendarRadioGroup(R.id.sync_calendar_no);
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            makePhoto();
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_DOWNLOAD && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            userPreferences.setIsAutodownload(true);
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_DOWNLOAD && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED){
            userPreferences.setIsAutodownload(false);
            isAutoDownloadManualDenied = true;
            autodownloadRadioGroup.check(R.id.auto_download_no);
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_GALLERY && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            userPreferences.setIsCopyPhotos(true);
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_GALLERY && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults.length > 1 && grantResults[1] != PackageManager.PERMISSION_GRANTED){
            userPreferences.setIsCopyPhotos(false);
            isAutoCopyManualDenied = true;
            copyPhotosGalleryRadioGroup.check(R.id.auto_download_no);
        }
    }

    private boolean isAutoDownloadManualDenied = false;
    private boolean isAutoCopyManualDenied = false;

    private void programmaticallyChangeCalendarRadioGroup(int sync_calendar_state) {
        calendarRadioGroupListener.disable();
        syncCalendarRadioGroup.check(sync_calendar_state);
        calendarRadioGroupListener.enable();
    }

    private void doPickPhotoAction() {
        SharedPreferences sp = getActivity().getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("SHOWING_CAMERA", true);
        editor.commit();

        Intent intent = new Intent(
        Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    public void onAcceptPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.close();
        alertPhotoShowing = false;
        sendPhoto();
    }

    @Override
    public void onCancelPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.hideAccpetOrCancelBtns();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                //Log.d("photoTaken", String.format("pathToPhotoTaken %s", pathToPhotoTaken));
                Bitmap resized = ImageUtils.decodeFileWithRotation(pathToPhotoTaken);

                if (resized == null) {
                    showImageErrorDialog();
                } else {
                    selectedImageUri = ImageUtils.getImageUri(getContext(), resized);
                    //Log.d("photoTaken", String.format("selectedImageUri %s", selectedImageUri.toString()));
                    pathAvatar = selectedImageUri.getPath();
                    //Log.d("photoTaken", String.format("pathAvatar %s", pathAvatar));
                    alertPhotoShowing = false;
                    alertPickOrTakePhoto.close();
                    alertNonDismissable.showMessage(getActivity());
                    sendPhoto();
                }
            }

        } else if (requestCode == SELECT_FILE) {
            if (resultCode == RESULT_OK) {
                selectedImageUri = data.getData();

                // Let's read picked image path using content resolver
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor cursor = Objects.requireNonNull(getActivity()).getContentResolver().query(selectedImageUri, filePath, null, null, null);
                String imagePath;
                if (cursor != null) {
                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    //Log.d("photoTaken", String.format("imagePath %s", imagePath));

                    if(imagePath == null){
                        showImageErrorDialog();
                    }else{
                        cursor.close();

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                        //Log.d("photoTaken", String.format("selectedImageUri %s", selectedImageUri.toString()));
                        Bitmap resized = ImageUtils.decodeFileWithRotation(imagePath, options);

                        if (resized == null) {
                            showImageErrorDialog();
                        } else {
                            selectedImageUri = ImageUtils.getImageUri(getContext(), resized);
                            pathAvatar = selectedImageUri.getPath();
                            alertPhotoShowing = false;
                            alertPickOrTakePhoto.close();
                            alertNonDismissable.showMessage(getActivity());
                            sendPhoto();
                        }
                    }
                }
                else{
                    showImageErrorDialog();
                }

                alertPhotoShowing = false;
                alertPickOrTakePhoto.close();

            }
        }
    }

    @Override
    public void onResponseUpdateUserRequest(JSONObject userRegister) {
        userPreferences.setUserAvatar(pathAvatar);

        if (mListener != null) mListener.onUpdateAvatar();
        userAvatarView.setImageURI(selectedImageUri);
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void onFailureUpdateUserRequest(Object error) {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
        showError(error);
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        switch (type){
            case ALERT_TYPE_PERMISSIONS:
                alertMessage.alert.cancel();
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivity(i);
                break;
            default:
                alertMessage.alert.cancel();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == languageRadioGroup) {
            if(languageRadioGroup.getCheckedRadioButtonId() == R.id.catalan){
                userPreferences.setUserLanguage(UserRegister.CAT);
                setLocale("ca");
            } else {
                userPreferences.setUserLanguage(UserRegister.ESP);
                setLocale("es");
            }
        } else if (group == autodownloadRadioGroup) {
            if (checkedId == R.id.auto_download_yes){
                setAutodownloadToTrue();
            }
            else{
                userPreferences.setIsAutodownload(false);
            }
        } else if (group == syncCalendarRadioGroup) {
            Log.d("syncCalendarRadioGroup","onCheckedChanged: " + String.valueOf(checkedId == R.id.sync_calendar_yes));

            onSyncCalendarChanged(checkedId == R.id.sync_calendar_yes);
        } else if (group == textSizeRadioGroup) {
            if (textSizeRadioGroup.getCheckedRadioButtonId() == R.id.small) {
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_SMALL);
            } else if (textSizeRadioGroup.getCheckedRadioButtonId() == R.id.big) {
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_BIG);
            } else {
                Log.d("fntsz","size medium");
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_MEDIUM);
            }
            Log.d("fntsz","recreate act");
            if (getActivity() != null) getActivity().recreate();
        } else if (group == copyPhotosGalleryRadioGroup) {
            if (checkedId == R.id.copy_photos_gallery_yes){
                setIsCopyFotosTrue();
            }
            else{
                copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_no);
                userPreferences.setIsCopyPhotos(false);

            }
        }
    }

    private void setIsCopyFotosTrue() {
        if (isAutoCopyManualDenied){
            isAutoCopyManualDenied = false;
            return;
        }
        if (getActivity() == null)return;
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_yes);
            userPreferences.setIsCopyPhotos(true);
        }
        else{
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
                copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_no);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_GALLERY);
            }
        }
    }

    private void setAutodownloadToTrue() {
        if (isAutoDownloadManualDenied){
            isAutoDownloadManualDenied = false;
            return;
        }
        if (getActivity() == null)return;
        if (!userPreferences.getIsCopyPhotos()){
            userPreferences.setIsAutodownload(true);
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            autodownloadRadioGroup.check(R.id.auto_download_yes);
            userPreferences.setIsAutodownload(true);
        }
        else{
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
                autodownloadRadioGroup.check(R.id.auto_download_no);
                copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_no);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_DOWNLOAD);
            }
        }
    }

    private void onSyncCalendarChanged(boolean sync) {
       if (calendarRadioGroupListener.isEnabled()) {
           if (sync){
               if (CalendarSyncManager.hasCalendarPermissions(getContext())) {
                   userPreferences.setIsSyncCalendars(true);
                   doCalendarSync();
               }
               else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)
                       && !shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)
                       && CalendarSyncManager.permissionsDenied(getContext())){
                   showSettingsAlert(getResources().getString(R.string.should_accept_permissions_calendar));
                   this.calendarRadioGroupListener.disable();
                   this.syncCalendarRadioGroup.check(R.id.sync_calendar_no);
                   this.calendarRadioGroupListener.enable();
               }
               else{
                   //shouldrequest
                   requestPermissions( new String[]{Manifest.permission.WRITE_CALENDAR,
                                   Manifest.permission.READ_CALENDAR},
                           CALENDAR_PERMISSION_REQUEST);
               }
           }
           else{
               userPreferences.setIsSyncCalendars(false);
               startCalendarSyncAlert();
               CalendarSyncManager calendarSyncManager = new CalendarSyncManager();
               calendarSyncManager.deleteCalendar();
           }
        } else {
            calendarRadioGroupListener.disable();
            //syncCalendarRadioGroup.check(sync ? R.id.sync_calendar_no : R.id.sync_calendar_yes);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    syncCalendarRadioGroup.setOnCheckedChangeListener(ProfileFragment.this);
                }
            }, 1200);
        }


    }

    private void startCalendarSyncAlert() {
        alertWaitCalendarSync.showMessage(getActivity());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

             //   syncCalendarRadioGroup.setOnCheckedChangeListener(ProfileFragment.this);
                if (alertWaitCalendarSync != null) alertWaitCalendarSync.dismissSafely();
            }
        }, 1200);
    }

    private void doCalendarSync() {
        startCalendarSyncAlert();
        new Thread(new Runnable() {
            @Override
            public void run() {
                userPreferences.setIsSyncCalendars(true);
                CalendarSyncManager calendarSyncManager = new CalendarSyncManager();
                long calendarId = calendarSyncManager.addCalendar();
                userPreferences.setCalendarId(calendarId);
                calendarSyncManager.addAllMeetings(calendarId, new MeetingsDb(
                        MyApplication.getAppContext()).findAllShownMeetings());
            }
        }).start();

    }

    public void setLocale(final String lang) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Locale locale = new Locale(lang);
                Resources res = getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = res.getConfiguration();
                conf.locale = locale;
                res.updateConfiguration(conf, dm);

                Log.d("lng",getResources().getConfiguration().locale.getLanguage());

                if (getActivity() != null){
                    Log.d("recreate", "recreate");
                    getActivity().recreate();
                }
            }
        });


    }

    @Override
    public void onClosed() {
        alertPhotoShowing = false;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void onUpdateAvatar();
        void onProfileEditClicked();
    }

    public void showError(Object error) {
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");
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
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }


}
