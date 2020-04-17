package cat.bcn.vincles.mobile.UI.Gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daasuu.bl.BubbleLayout;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentsRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertRetry;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_IMAGE_CAPTURE;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_VIDEO_CAPTURE;


public class GalleryFragment extends BaseFragment implements GalleryView, View.OnClickListener, AlertMessage.AlertMessageInterface,
        GalleryAdapter.OnItemClicked, AlertConfirmOrCancel.AlertConfirmOrCancelInterface, AlertRetry.AlertSaveImageInGalleryInterface,
        GalleryAdapter.OnBottomReachedListener, GalleryAdapter.OnCheckPermissionsCallback, GetGalleryContentsRequest.OnResponse {


    private static final int STATE_DEFAULT = 0;
    private static final int STATE_ASKING_DELETE = 1;
    private static final int STATE_DELETING = 2;
    private static final int STATE_SHOWING_RESULT_OK = 3;
    private static final int STATE_SHOWING_RESULT_PARTIALLY_OK = 4;
    private static final int STATE_SHOWING_RESULT_NOT_OK = 5;


    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO = 0;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 3;
    private static final int CALL_PERMISSIONS_REQUEST = 0;
    private static final String ALERT_TYPE_PERMISSIONS = "SETTINGS_PERMISSIONS";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_GALLERY = 4;

    private OnFragmentInteractionListener mListener;

    AlertMessage alertMessage;
    private GalleryAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    GalleryDb galleryDb;
    UsersDb usersDb;
    View v;
    ViewGroup bottomBar;
    UserPreferences userPreferences;
    AlertRetry alertRetry;
    PopupWindow mPopupWindow, mPopupWindowShare;
    boolean shouldShowFilter = false, shouldShowAction = false;
    View filterBtn;
    GallerypPresenter gallerypPresenter;
    View popWindowMenu, popWindowShare;
    Object picture;
    boolean savingIsPhoto, isSavingFile = false, isPictureUri = false;
    AlertNonDismissable alertNonDismissable;
    int state = STATE_DEFAULT;
    public View shareButton;
    View cancelButton;
    View cameraButton;
    View videoButton;
    View deleteButton;
    String pathPhotoTaken;
    private RecyclerView recyclerView;
    private boolean hasZeroFiles;
    private boolean sharedMedia;
    private int index;
    private GalleryContentRealm galleryContentRealm;
    private int itemPositionToDownload = -1;
    Button selectAll;
    private boolean viewCreated = false;
    private boolean selectAllMode = false;
    TextView noContentLabel;
    AlertConfirmOrCancel alertCOrC;

    Realm realm;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance(FragmentResumed listener) {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_GALLERY);
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        ArrayList<Integer> itemsSelected = null;
        index = -1;
        boolean isInSelectionMode = false;
        boolean isInShareMode = false;
        boolean isInDeleteMode = false;
        boolean isSelectAllMode = false;

        if (savedInstanceState != null) {
            itemsSelected = savedInstanceState.getIntegerArrayList("itemsSelected");
            isInShareMode = savedInstanceState.getBoolean("isInShareMode", false);
            isInDeleteMode = savedInstanceState.getBoolean("isInDeleteMode", false);
            isInSelectionMode = savedInstanceState.getBoolean("isInSelectionMode", false);
            selectAllMode = savedInstanceState.getBoolean("isSelectAllMode", false);

            isSavingFile = savedInstanceState.getBoolean("isSavingFile");
            sharedMedia = savedInstanceState.getBoolean("sharedMedia");
            if (isSavingFile) {
                savingIsPhoto = savedInstanceState.getBoolean("savingIsPhoto");
                isPictureUri = savedInstanceState.getBoolean("isPictureUri");
                if (isPictureUri) picture = savedInstanceState.getParcelable("picture");
                else picture = savedInstanceState.getString("picture");
            }
        }


        loadContent(itemsSelected, savedInstanceState, isInSelectionMode, isInDeleteMode, isInShareMode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("pathPhotoTaken", this.pathPhotoTaken);
        outState.putIntegerArrayList("itemsSelected", gallerypPresenter.getItemsSelected());
        outState.putBoolean("isInSelectionMode", gallerypPresenter.isInSelectionMode());
        outState.putString("filterKind", gallerypPresenter.getFilterKind());
        outState.putString("actionKind", gallerypPresenter.getActionKind());
        outState.putBoolean("isSelectAllMode", selectAllMode);

        outState.putBoolean("showingFilter", mPopupWindow != null && mPopupWindow.isShowing());
        outState.putBoolean("showingActions", mPopupWindowShare != null && mPopupWindowShare.isShowing());

        outState.putBoolean("isSavingFile", isSavingFile);
        outState.putBoolean("sharedMedia", sharedMedia);
        if (isSavingFile) {
            outState.putBoolean("savingIsPhoto", savingIsPhoto);
            outState.putBoolean("isPictureUri", isPictureUri);
            if (isPictureUri) outState.putParcelable("picture", (Uri) picture);
            else outState.putString("picture", (String) picture);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_gallery));

        if (recyclerView != null) {
            showGalleryContent(gallerypPresenter.getFilteredMedia(gallerypPresenter.getFilterKind()));
            if (sharedMedia) {
                sharedMedia = false;
                gallerypPresenter.setInSelectionMode(false);
                adapter.emptyItemSelecteds();
                selectAllMode = false;
                selectAll.setText(R.string.gallery_select_all);
                onUpdateIsInSelectionMode();
            }
            if (selectAllMode) {
                selectAll.setText(R.string.gallery_deselect_all);
            } else {
                selectAll.setText(R.string.gallery_select_all);
            }
        }
    }

    public void loadContent(ArrayList<Integer> itemsSelected, Bundle savedInstanceState, boolean isInSelectionMode, boolean isInShareMode, boolean isInDeleteMode) {
        String filterKind = null;
        String actionKind = null;

        if (savedInstanceState != null) {
            filterKind = savedInstanceState.getString("filterKind");
            shouldShowFilter = savedInstanceState.getBoolean("showingFilter");

            actionKind = savedInstanceState.getString("actionKind");
            shouldShowAction = savedInstanceState.getBoolean("showingActions");


        }
        userPreferences = new UserPreferences(getContext());
        galleryDb = new GalleryDb(getContext());
        usersDb = new UsersDb(getContext());
        gallerypPresenter = new GallerypPresenter((BaseRequest.RenewTokenFailed) getActivity(), getContext(), this, userPreferences,
                galleryDb, usersDb, itemsSelected, filterKind, actionKind, isInSelectionMode, realm);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (alertNonDismissable != null && isAdded())
                alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
            savingIsPhoto = true;
            isPictureUri = false;
            picture = pathPhotoTaken;
            onSavePhoto();

        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
            Uri _uri = data.getData();
            if (_uri != null && "content".equals(_uri.getScheme())) {
                Cursor cursor = getActivity().getContentResolver().query(_uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                picture = Uri.parse(cursor.getString(0));
                cursor.close();
            } else {
                picture = _uri.getPath();
            }
            savingIsPhoto = false;
            onSaveVideo();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_gallery, container, false);
        View backBtn = v.findViewById(R.id.back);
        if (backBtn != null) backBtn.setOnClickListener(this);

        selectAll = v.findViewById(R.id.selectAll);
        noContentLabel = v.findViewById(R.id.noContentLabel);

        bottomBar = v.findViewById(R.id.bottom_bar);
        setupBottomBar(inflater);

        if (shouldShowFilter) {
            shouldShowFilter = false;
            //Posting delayed so that views have been drawn. Should do viewTree observer but this is
            //easier, and gives it "an animation look" as it shows the filter later
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFilterPopup();
                }
            }, 300);
        }

        if (shouldShowAction) {
            shouldShowAction = false;
            //Posting delayed so that views have been drawn. Should do viewTree observer but this is
            //easier, and gives it "an animation look" as it shows the filter later
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showActionsPopup();
                }
            }, 300);
        }

        TextView titleLine = v.findViewById(R.id.title);
        if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
            titleLine.setText(getResources().getString(R.string.my_files));
        } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
            titleLine.setText(getResources().getString(R.string.received_files));
        }

        if (isSavingFile) {
            showErrorSavingFile(null);
        }
        configureRecyclerView();
        if (savedInstanceState == null) {
            gallerypPresenter.filterMedia(gallerypPresenter.getFilterKind());
        } else {
            this.pathPhotoTaken = savedInstanceState.getString("pathPhotoTaken");
        }

        gallerypPresenter.onCreateView();

        viewCreated = true;

        if (selectAllMode) {
            selectAll.setText(R.string.gallery_deselect_all);
        } else {
            selectAll.setText(R.string.gallery_select_all);
        }

        return v;
    }

    public void onButtonPressed(Uri uri) {
    }

    private void setupBottomBar(LayoutInflater inflater) {
        bottomBar.removeAllViews();
        selectAll.setVisibility(View.GONE);
        if ((gallerypPresenter.isInSelectionMode())) {
            inflater.inflate(R.layout.fragment_contacts_select_bottom_bar, bottomBar);
        } else {
            inflater.inflate(R.layout.fragment_gallery_bottom_bar, bottomBar);
        }


        shareButton = bottomBar.findViewById(R.id.share);
        cancelButton = bottomBar.findViewById(R.id.cancel);
        cameraButton = bottomBar.findViewById(R.id.camera);
        videoButton = bottomBar.findViewById(R.id.video);
        deleteButton = bottomBar.findViewById(R.id.delete);
        if (deleteButton == null) {
            deleteButton = bottomBar.findViewById(R.id.delete);
        }
        if (cancelButton == null) {
            cancelButton = bottomBar.findViewById(R.id.cancel);
        }

        shareButton.setVisibility(View.VISIBLE);

        if (gallerypPresenter.getInDeleteMode()) {
            shareButton.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
        } else if (gallerypPresenter.getInShareMode()) {
            if (deleteButton != null) {
                deleteButton.setVisibility(View.GONE);
            }
        }
        TextView shareTextView = bottomBar.findViewById(R.id.share_tv);
        if (shareTextView != null) {
            if (gallerypPresenter.isInSelectionMode()) {
                shareTextView.setText(getResources().getString(R.string.gallery_button_share_selection));
            } else {
                shareTextView.setText(getResources().getString(R.string.gallery_button_share));
            }
        }

        filterBtn = bottomBar.findViewById(R.id.filter);

        if (cameraButton != null) cameraButton.setOnClickListener(this);
        if (videoButton != null) videoButton.setOnClickListener(this);
        if (shareButton != null) shareButton.setOnClickListener(this);
        if (cancelButton != null) cancelButton.setOnClickListener(this);
        if (deleteButton != null) deleteButton.setOnClickListener(this);
        if (filterBtn != null) filterBtn.setOnClickListener(this);
        if (selectAll != null) selectAll.setOnClickListener(this);


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
        if (alertNonDismissable != null)
            alertNonDismissable.dismissSafely();
        if (alertMessage != null)
            alertMessage.dismissSafely();
        if (alertRetry != null)
            alertRetry.dismissSafely();
        if (alertCOrC != null)
            alertCOrC.dismissSafely();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        if (recyclerView != null) recyclerView.setAdapter(null);
        if (realm != null) realm.close();
        if (mPopupWindowShare != null) {
            mPopupWindowShare.dismiss();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO && grantResults.length > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (!OtherUtils.checkIfMicrophoneIsBusy()) {
                    AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
                    alertMessage.showMessage(getActivity(), getResources().getString(R.string.error_audio_busy_for_video), "");
                    return;
                }
                OtherUtils.sendVideoIntent(this);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pathPhotoTaken = OtherUtils.sendPhotoIntent(this, false);
        }
        else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_GALLERY && grantResults.length > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                downloadItem();
        }
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.back) {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }

        } else if (view.getId() == R.id.camera) {

            if (OtherUtils.isLowMemory()) {
                showErrorMessage("2804");
                return;
            }

            LoginActivity.screenOrientation = -1;
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                pathPhotoTaken = OtherUtils.sendPhotoIntent(this, false);
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showSettingsAlert(getResources().getString(R.string.should_accept_permissions_camera_photo));
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
                }
            }

        } else if (view.getId() == R.id.video) {
            if (OtherUtils.isLowMemory()) {
                showErrorMessage("2804");
                return;
            }
            LoginActivity.screenOrientation = -1;
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {

                if (!OtherUtils.checkIfMicrophoneIsBusy()) {
                    AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
                    alertMessage.showMessage(getActivity(), getResources().getString(R.string.error_audio_busy_for_video), "");
                    return;
                }

                OtherUtils.sendVideoIntent(this);
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) && !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showSettingsAlert(getResources().getString(R.string.should_accept_permissions_camera_video));
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO);
                }
            }

        } else if (view.getId() == R.id.filter) {
            showFilterPopup();

        } else if (view.getId() == R.id.delete || view.getId() == R.id.delete) {
            if (gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() > 0) {
                showConfirmationRemoveContent();
            }
        } else if (view.getId() == R.id.cancel || view.getId() == R.id.cancel) {
            gallerypPresenter.resetItemsSelected();
            gallerypPresenter.setActionKind(null);

            selectAllMode = false;
            selectAll.setText(R.string.gallery_select_all);
            adapter.emptyItemSelecteds();
            adapter.notifyDataSetChanged();
            updateTitleSelectedLabel();
            checkNoContentLabel();

            shareButton.setVisibility(View.VISIBLE);
            gallerypPresenter.setInSelectionMode(false);
            gallerypPresenter.setInDeleteMode(false);
            gallerypPresenter.setInShareMode(false);
            onUpdateIsInSelectionMode();

            TextView titleLine = v.findViewById(R.id.title);
            if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
                titleLine.setText(getResources().getString(R.string.my_files));
            } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
                titleLine.setText(getResources().getString(R.string.received_files));
            } else {
                titleLine.setText(getResources().getString(R.string.all_files));
            }


        } else if (view.getId() == R.id.see_all_files) {
            checkPopMenuItem(0);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.all_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_ALL_FILES);
        } else if (view.getId() == R.id.see_my_files) {
            checkPopMenuItem(1);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.my_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_ALL_MY_FILES);
        } else if (view.getId() == R.id.see_recived_files) {
            checkPopMenuItem(2);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.received_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_RECIVED_FILES);
        } else if (view.getId() == R.id.filter_share) {
            gallerypPresenter.setActionKind(GallerypPresenter.ACTION_SHARE);
            mPopupWindowShare.dismiss();


            if (!gallerypPresenter.isInSelectionMode()) {
                gallerypPresenter.setInDeleteMode(false);
                gallerypPresenter.setInShareMode(true);
                gallerypPresenter.setInSelectionMode(true);

                updateTitleSelectedLabel();
                onUpdateIsInSelectionMode();
            }

        } else if (view.getId() == R.id.filter_delete) {
            gallerypPresenter.setActionKind(GallerypPresenter.ACTION_DELETE);

            mPopupWindowShare.dismiss();
            if (!gallerypPresenter.isInSelectionMode()) {
                gallerypPresenter.setInDeleteMode(true);
                gallerypPresenter.setInShareMode(false);
                gallerypPresenter.setInSelectionMode(true);

                updateTitleSelectedLabel();
                onUpdateIsInSelectionMode();
            }

        } else if (view.getId() == R.id.share) {

            if (!gallerypPresenter.isInSelectionMode()) {

                showActionsPopup();

            } else {

                if (gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() > 0) {
                    sharedMedia = true;
                    gallerypPresenter.onShareSelectionModeClicked();
                    gallerypPresenter.setInSelectionMode(false);
                    gallerypPresenter.setInShareMode(false);
                    gallerypPresenter.setInDeleteMode(false);

                }
            }

            // TODO AIXO HO HE COMENTAT
            /*
            if (!gallerypPresenter.isInSelectionMode()) {
                gallerypPresenter.setInSelectionMode(!gallerypPresenter.isInSelectionMode());
                updateTitleSelectedLabel();
                onUpdateIsInSelectionMode();
            } else {
                if (gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() > 0) {
                    sharedMedia = true;
                    gallerypPresenter.onShareSelectionModeClicked();
                }
            }
            */
        } else if (view.getId() == R.id.selectAll) {
            if (selectAllMode) {
                selectAllMode = false;
                selectAll.setText(R.string.gallery_select_all);
                adapter.emptyItemSelecteds();
                adapter.notifyDataSetChanged();
                onUpdateIsInSelectionMode();

            } else {
                selectAllMode = true;
                adapter.selectAllItems();
                adapter.notifyDataSetChanged();
                updateTitleSelectedLabel();

                selectAll.setText(R.string.gallery_deselect_all);
                onUpdateIsInSelectionMode();

            }
            checkNoContentLabel();

        }
    }

    private void checkNoContentLabel() {
        if (adapter.galleryContents.size() == 0) {
            noContentLabel.setVisibility(View.VISIBLE);
        } else {
            noContentLabel.setVisibility(View.GONE);
        }
    }

    private void showSettingsAlert(String message) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
        alertMessage.showMessage(getActivity(), message, ALERT_TYPE_PERMISSIONS);
    }

    @Override
    public void updateTitleSelectedLabel() {
        TextView titleLine = v.findViewById(R.id.title);
        if (gallerypPresenter.getInDeleteMode()) {
            titleLine.setText(getResources().getString(R.string.gallery_share_title));
        } else {
            titleLine.setText(getResources().getString(R.string.gallery_share_title) + " (" + String.valueOf(gallerypPresenter.getItemsSelected().size()) + "/" + String.valueOf(adapter.maxItemsSelected) + ")");
        }
    }

    @Override
    public void onShareContentSelectionMode(ArrayList<Integer> itemIDs, ArrayList<String> paths,
                                            ArrayList<String> metadata) {

        mListener.onGalleryShareButtonClicked(gallerypPresenter.getItemsSelected(), paths, metadata);
    }

    private void showFilterPopup() {
        if (popWindowMenu == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popWindowMenu = inflater.inflate(R.layout.popupwindow_filter_gallery, null);
            mPopupWindow = new PopupWindow(
                    popWindowMenu,
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);

            TextView seeAllFiles = popWindowMenu.findViewById(R.id.see_all_files);
            TextView seeMyFiles = popWindowMenu.findViewById(R.id.see_my_files);
            TextView seeRecivedFiles = popWindowMenu.findViewById(R.id.see_recived_files);

            seeAllFiles.setOnClickListener(this);
            seeMyFiles.setOnClickListener(this);
            seeRecivedFiles.setOnClickListener(this);

            if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
                checkPopMenuItem(1);
            } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
                checkPopMenuItem(2);
            } else {
                checkPopMenuItem(0);
            }
        }
        if (filterBtn != null) {
            popWindowMenu.measure(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindow.showAsDropDown(filterBtn, 0, -popWindowMenu.getMeasuredHeight() - filterBtn.getHeight());
            BubbleLayout bubbleLayout = ((BubbleLayout) popWindowMenu.findViewById(R.id.bubblelayout));
            bubbleLayout.setArrowPosition(filterBtn.getWidth() / 2 - bubbleLayout.getArrowWidth() / 2);
        }
    }

    private void showActionsPopup() {
        if (popWindowShare == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popWindowShare = inflater.inflate(R.layout.popupwindow_filter_actions, null);
            mPopupWindowShare = new PopupWindow(
                    popWindowShare,
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            mPopupWindowShare.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindowShare.setFocusable(true);
            mPopupWindowShare.setOutsideTouchable(true);

            TextView share = popWindowShare.findViewById(R.id.filter_share);
            TextView delete = popWindowShare.findViewById(R.id.filter_delete);

            share.setOnClickListener(this);
            delete.setOnClickListener(this);


        }
        if (shareButton != null) {
            popWindowShare.measure(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindowShare.showAsDropDown(shareButton, 0, -popWindowShare.getMeasuredHeight() - shareButton.getHeight());
            BubbleLayout bubbleLayout = ((BubbleLayout) popWindowShare.findViewById(R.id.bubblelayout));
            bubbleLayout.setArrowPosition(shareButton.getWidth() / 2 - bubbleLayout.getArrowWidth() / 2);
        }
    }


    public void showConfirmationRemoveContent() {
        state = STATE_ASKING_DELETE;

        if (gallerypPresenter.getItemsSelected().size() > 1) {
            new AlertConfirmOrCancel(getActivity(), this).showMessage(getString(R.string.remove_items_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        } else {
            new AlertConfirmOrCancel(getActivity(), this).showMessage(getString(R.string.remove_item_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        }

    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {


        if (selectAllMode == true) {
            alertConfirmOrCancel.alert.dismiss();
            if (isAdded() && alertNonDismissable != null) {
                alertNonDismissable.showMessage(getActivity(), getResources().getString(gallerypPresenter.getItemsSelected().size() > 1 ? R.string.deleting_files : R.string.deleting_file));


            }

            GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
            RealmResults<GalleryContentRealm> list = galleryDb.findAll(realm);
            if (list.size() > 0) {
                long InclusionTime = list.get(list.size() - 1).getInclusionTime();
                String accessToken = userPreferences.getAccessToken();
                GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(null, InclusionTime);
                getContentRequest.addOnOnResponse(this);
                getContentRequest.doRequest(accessToken);
            }

        } else {
            alertConfirmOrCancel.alert.dismiss();

            state = STATE_DELETING;
            if (isAdded() && alertNonDismissable != null) {
                alertNonDismissable.showMessage(getActivity(), getResources().getString(gallerypPresenter.getItemsSelected().size() > 1 ? R.string.deleting_files : R.string.deleting_file));


            }
            gallerypPresenter.deleteSelectedContent();

        }


    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        gallerypPresenter.resetItemsSelected();
        selectAllMode = false;
        selectAll.setText(R.string.gallery_select_all);
        adapter.emptyItemSelecteds();
        adapter.notifyDataSetChanged();
        checkNoContentLabel();

        updateTitleSelectedLabel();

        alertConfirmOrCancel.alert.dismiss();
        state = STATE_DEFAULT;
    }

    @Override
    public void onDeleteResults(int results) {

        if (alertNonDismissable != null && alertNonDismissable.alert != null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.dismissSafely();
        }
        if (!isAdded()) return;
        int messageID = 0;
        switch (results) {
            case GalleryView.DELETE_OK:
                state = STATE_DEFAULT;
                selectAllMode = false;
                selectAll.setText(R.string.gallery_select_all);
                adapter.emptyItemSelecteds();


                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        checkNoContentLabel();
                    }
                };

                Handler h = new Handler();
                h.postDelayed(r, 300);

                break;
            case GalleryView.DELETE_PARTIALLY_OK:
                state = STATE_SHOWING_RESULT_PARTIALLY_OK;
                messageID = R.string.deleting_files_partially_ok;
                break;
            case GalleryView.DELETE_NOT_OK:
                messageID = R.string.deleting_files_not_ok_retry;
                state = STATE_SHOWING_RESULT_NOT_OK;
                break;
        }
        alertNonDismissable.dismissSafely();
        if (messageID != 0) {
            if (isAdded()) {
                alertCOrC = new AlertConfirmOrCancel(getActivity(), this);
                alertCOrC.showMessage(getString(messageID), getResources().getString(R.string.delete), AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
            }
        }

        updateTitle();
        checkNoContentLabel();

    }

    @Override
    public void setAdapterItemsSelected(ArrayList<Integer> itemsSelected) {
        adapter.setItemsSelected(itemsSelected);
    }

    private void updateTitle() {
        TextView titleLine = v.findViewById(R.id.title);
        switch (gallerypPresenter.getFilterKind()) {
            case GallerypPresenter.FILTER_ALL_MY_FILES:
                titleLine.setText(getResources().getString(R.string.my_files));
                break;
            case GallerypPresenter.FILTER_RECIVED_FILES:
                titleLine.setText(getResources().getString(R.string.received_files));
                break;
            default:
                titleLine.setText(getResources().getString(R.string.all_files));
                break;
        }
    }


    private int getPopupTextViewID(int index) {
        switch (index) {
            case 0:
            default:
                return R.id.see_all_files;
            case 1:
                return R.id.see_my_files;
            case 2:
                return R.id.see_recived_files;
        }
    }

    private int getPopupImageViewID(int index) {
        switch (index) {
            case 0:
            default:
                return R.id.see_all_files_iv;
            case 1:
                return R.id.see_my_files_iv;
            case 2:
                return R.id.see_recived_files_iv;
        }
    }

    public void checkPopMenuItem(int index) {
        unCheckAllPopMenuItem();
        int textViewID = getPopupTextViewID(index);
        ((TextView) popWindowMenu.findViewById(textViewID))
                .setTextColor(getResources().getColor(R.color.colorPrimary));
        int imageViewID = getPopupImageViewID(index);
        popWindowMenu.findViewById(imageViewID).setVisibility(View.VISIBLE);

    }

    public void unCheckAllPopMenuItem() {
        for (int i = 0; i < 3; i++) {
            TextView textView = popWindowMenu.findViewById(getPopupTextViewID(i));
            textView.setTextColor(getResources().getColor(R.color.colorBlack));
            ImageView imageView = popWindowMenu.findViewById(getPopupImageViewID(i));
            imageView.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void showErrorMessage(Object error) {
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        if (isAdded())
            alertMessage.showMessage(getActivity(), errorMsg, "");
    }

    @Override
    public void savingFileOk() {
        isSavingFile = false;
    }

    @Override
    public void savingFilePictureIsUri(Uri uri) {
        isPictureUri = true;
        picture = uri;
    }

    @Override
    public void showErrorSavingFile(Object error) {
        isSavingFile = true;
        alertRetry = new AlertRetry(getActivity(), this);
        if (isAdded())
            alertRetry.showMessage(null);
    }

    @Override
    public void onRetryAccept(AlertRetry alertRetry) {
        if (alertRetry != null && alertRetry.alert != null
                && alertRetry.alert.isShowing()) {
            alertRetry.alert.dismiss();
        }
        alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
        if (savingIsPhoto) {
            onSavePhoto();
        } else {
            onSaveVideo();
        }
    }

    @Override
    public void onRetryCancel(AlertRetry alertRetry) {
        isSavingFile = false;
        if (alertRetry != null && alertRetry.alert != null
                && alertRetry.alert.isShowing()) {
            alertRetry.alert.dismiss();
        }
    }

    @Override
    public void closeAlertSavingImage() {
        if (alertNonDismissable != null && alertNonDismissable.alert != null
                && alertNonDismissable.alert.isShowing() && getActivity() != null && isAdded()) {
            alertNonDismissable.alert.dismiss();
        }
    }

    public void configureRecyclerView() {
        if (!isAdded()) return;

        recyclerView = v.findViewById(R.id.recyclerView);

        int numberOfColumns = getResources().getInteger(R.integer.gallery_number_of_columns);
        mLayoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        if (itemDecoration == null) {
            int spacing = getResources().getDimensionPixelSize(R.dimen.adapter_image_spacing);
            itemDecoration = new GridSpacingItemDecoration(numberOfColumns, spacing, false);
        }
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(mLayoutManager);

    }

    @Override
    public void showGalleryContent(final List<GalleryContentRealm> galleryContentList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter == null || recyclerView.getAdapter() == null || hasZeroFiles) {
                        adapter = new GalleryAdapter(getContext(), galleryContentList, gallerypPresenter.getItemsSelected(),
                                gallerypPresenter.isInSelectionMode(), gallerypPresenter.getInDeleteMode(), GalleryFragment.this);
                        adapter.addItemClickedListeners(GalleryFragment.this);
                        recyclerView.setAdapter(adapter);
                        hasZeroFiles = false;
                        if ((gallerypPresenter.isInSelectionMode())) {
                            updateTitleSelectedLabel();
                        }
                    } else {
                        if (galleryContentList == null || galleryContentList.size() == 0) {
                            adapter = new GalleryAdapter(getContext(), new ArrayList<GalleryContentRealm>(), gallerypPresenter.getItemsSelected(),
                                    gallerypPresenter.isInSelectionMode(), gallerypPresenter.getInDeleteMode(), GalleryFragment.this);
                            recyclerView.setAdapter(adapter);
                            hasZeroFiles = true;
                        } else {
                            adapter.setGalleryContents(galleryContentList);
                            adapter.notifyDataSetChanged();
                        }
                        if ((gallerypPresenter.isInSelectionMode())) {
                            updateTitleSelectedLabel();
                        }
                    }

                    setOnBottomReachedListener();
                    checkNoContentLabel();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            adapter.setGalleryContents(galleryContentList);
                            adapter.notifyDataSetChanged();
                            checkNoContentLabel();
                        }
                    };

                    Handler h = new Handler();
                    h.postDelayed(r, 300);
                }
            });
        }
    }

    private void setOnBottomReachedListener() {
        if (adapter != null)
            adapter.setOnBottomReachedListener(this);
    }

    @Override
    public void updateContents(RealmResults<GalleryContentRealm> contentPaths) {
        adapter.notifyDataSetChanged();
        v.findViewById(R.id.progressbar).setVisibility(View.GONE);
        checkNoContentLabel();


    }

    @Override
    public void onFileAdded() {
        if (viewCreated) adapter.notifyDataSetChanged();
        checkNoContentLabel();

    }

    @Override
    public boolean checkWriteExternalStoragePermission() {
        if (getActivity() == null) return false;
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void updateEnabledButtons(boolean enabled) {
        if (shareButton != null && deleteButton != null) {
            shareButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
            ImageView iv = shareButton.findViewById(R.id.share_iv);
            if (iv != null) iv.setEnabled(enabled);
            TextView tv = shareButton.findViewById(R.id.share_tv);
            if (tv != null) tv.setEnabled(enabled);
            iv = deleteButton.findViewById(R.id.delete_iv);
            if (iv != null) iv.setEnabled(enabled);
            tv = deleteButton.findViewById(R.id.delete_tv);
            if (tv != null) tv.setEnabled(enabled);
        }
    }

    @Override
    public void resetSelectMode() {
        gallerypPresenter.resetItemsSelected();
        selectAllMode = false;
        selectAll.setText(R.string.gallery_select_all);
        adapter.emptyItemSelecteds();
        adapter.notifyDataSetChanged();
        updateTitleSelectedLabel();
        checkNoContentLabel();
        gallerypPresenter.setActionKind(null);

        shareButton.setVisibility(View.VISIBLE);
        gallerypPresenter.setInSelectionMode(false);
        gallerypPresenter.setInDeleteMode(false);
        gallerypPresenter.setInShareMode(false);
        onUpdateIsInSelectionMode();
        updateTitle();
        TextView titleLine = v.findViewById(R.id.title);
        if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
            titleLine.setText(getResources().getString(R.string.my_files));
        } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
            titleLine.setText(getResources().getString(R.string.received_files));
        } else {
            titleLine.setText(getResources().getString(R.string.all_files));
        }
    }


    @Override
    public void onUpdateIsInSelectionMode() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        setupBottomBar(inflater);

        // TODO
        adapter.setInSelectedMode(gallerypPresenter.isInSelectionMode());
        if (gallerypPresenter.getInDeleteMode()) {
            adapter.setInDeleteMode(true);

        } else {
            adapter.setInDeleteMode(false);
        }

        if (gallerypPresenter.isInSelectionMode() && gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() == 0) {
            if (shareButton != null && deleteButton != null) {
                shareButton.setEnabled(false);
                deleteButton.setEnabled(false);
                ImageView iv = shareButton.findViewById(R.id.share_iv);
                if (iv != null) iv.setEnabled(false);
                TextView tv = shareButton.findViewById(R.id.share_tv);
                if (tv != null) tv.setEnabled(false);
                iv = deleteButton.findViewById(R.id.delete_iv);
                if (iv != null) iv.setEnabled(false);
                tv = deleteButton.findViewById(R.id.delete_tv);
                if (tv != null) tv.setEnabled(false);
            }
        } else {

            if (shareButton != null && deleteButton != null) {
                shareButton.setEnabled(true);
                deleteButton.setEnabled(true);
                ImageView iv = shareButton.findViewById(R.id.share_iv);
                if (iv != null) iv.setEnabled(true);
                TextView tv = shareButton.findViewById(R.id.share_tv);
                if (tv != null) tv.setEnabled(true);
                iv = deleteButton.findViewById(R.id.delete_iv);
                if (iv != null) iv.setEnabled(true);
                tv = deleteButton.findViewById(R.id.delete_tv);
                if (tv != null) tv.setEnabled(true);
            }
        }

    }


    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {

        switch (type) {
            case ALERT_TYPE_PERMISSIONS:
                alertMessage.alert.cancel();
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivity(i);
                break;
            default:
                alertMessage.alert.cancel();
        }


    }

    public void onViewItem(GalleryContentRealm galleryContentRealm, int index) {
        this.index = index;
        this.galleryContentRealm = galleryContentRealm;

        mListener.onGalleryItemPicked(galleryContentRealm, index, gallerypPresenter.getFilterKind());
    }

    @Override
    public void onSelectItem(GalleryContentRealm galleryContentRealm, int index) {
        selectAllMode = false;
        selectAll.setText(R.string.gallery_select_all);
        gallerypPresenter.itemSelected(galleryContentRealm.getId(), index);
        onUpdateIsInSelectionMode();
    }


    /**
     * Methods for showing help guide
     */
    @Override
    protected boolean shouldShowMenu() {
        return true;
    }

    @Override
    protected String getTextForPage(int page) {
        if (gallerypPresenter.isInSelectionMode()) {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_gallery_back);
                case 1:
                    return getString(R.string.context_help_gallery_delete);
                case 2:
                    return getString(R.string.context_help_gallery_share_contacts);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_gallery_filter);
                case 1:
                    return getString(R.string.context_help_gallery_share);
                case 2:
                    return getString(R.string.context_help_gallery_new_photo);
                case 3:
                    return getString(R.string.context_help_gallery_new_video);
                default:
                    return null;
            }
        }
    }

    @Override
    protected View getViewForPage(int page) {
        if (gallerypPresenter.isInSelectionMode()) {
            switch (page) {
                case 0:
                    return bottomBar.findViewById(R.id.cancel);
                case 1:
                    return bottomBar.findViewById(R.id.delete);
                case 2:
                    return bottomBar.findViewById(R.id.share);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return bottomBar.findViewById(R.id.filter);
                case 1:
                    return bottomBar.findViewById(R.id.share);
                case 2:
                    return bottomBar.findViewById(R.id.camera);
                case 3:
                    return bottomBar.findViewById(R.id.video);
                default:
                    return null;
            }
        }
    }

    @Override
    public void onBottomReached(List<GalleryContentRealm> galleryContentRealms) {
        if (galleryContentRealms.size() == 0) return;
        gallerypPresenter.getContent(galleryContentRealms.get(galleryContentRealms.size() - 1).getInclusionTime());
    }

    @Override
    public boolean onCheckPermissionsCallback(int position) {
        itemPositionToDownload = position;
        if (getActivity() == null) return false;
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CAMERA_GALLERY);
            }
        }
        return false;
    }

    private void downloadItem() {
        if (itemPositionToDownload == -1) return;

        recyclerView.findViewHolderForAdapterPosition(itemPositionToDownload).itemView.findViewById(R.id.video_hint).performClick();

    }

    @Override
    public void onResponseGetGalleryContentsRequest(ArrayList<GalleryContentRealm> galleryContents) {


        GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        boolean isLastContentInGallery = true;
        List<GalleryContentRealm> galleryContentList = galleryContents;
        if (galleryContentList.size() > 0) {
            int lasGalleryContentId = galleryContentList.get(galleryContentList.size() - 1).getId();
            isLastContentInGallery = galleryDb.existsContentById(lasGalleryContentId);

            for (GalleryContentRealm galleryContent : galleryContentList) {
                if (!galleryDb.existsContentById(galleryContent.getId())) {
                    galleryDb.insertContent(new GalleryContentRealm(galleryContent.getId(),
                            galleryContent.getIdContent(), galleryContent.getMimeType(),
                            galleryContent.getUserId(), galleryContent.getInclusionTime()));
                    //       usersDb.saveGetUser(galleryContent.getUserId(), true);
                }

            }
        }

        //Server returns results in groups of 10. Several requests need to be done
        if (!isLastContentInGallery) {

            long InclusionTime = galleryContentList.get(galleryContentList.size() - 1).getInclusionTime();
            String accessToken = userPreferences.getAccessToken();
            GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(null, InclusionTime);
            getContentRequest.addOnOnResponse(this);
            getContentRequest.doRequest(accessToken);
        } else { //Finally, all info obtained

            state = STATE_DELETING;
            if (isAdded() && alertNonDismissable != null && alertNonDismissable.alert != null && !alertNonDismissable.alert.isShowing()) {
                alertNonDismissable.showMessage(getActivity(), getResources().getString(gallerypPresenter.getItemsSelected().size() > 1 ? R.string.deleting_files : R.string.deleting_file));

            }


            adapter.selectAllItems();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    gallerypPresenter.deleteSelectedContent();
                }
            };

            Handler h = new Handler();
            h.postDelayed(r, 200);

        }
    }

    @Override
    public void onFailureGetGalleryContentsRequest(Object error) {

    }

    public interface OnFragmentInteractionListener {
        void onGalleryItemPicked(GalleryContentRealm galleryContent, int index, String filterKind);

        void onGalleryShareButtonClicked(ArrayList<Integer> idContentList, ArrayList<String> paths,
                                         ArrayList<String> metadata);
    }

    public void onSavePhoto() {
        gallerypPresenter.pushImageToAPI(picture, isPictureUri);
    }

    public void onSaveVideo() {
        Uri uri = Uri.parse(picture.toString());
        Log.d("uriTest", uri.toString());
        gallerypPresenter.pushVideoToAPI(uri);
    }
}
