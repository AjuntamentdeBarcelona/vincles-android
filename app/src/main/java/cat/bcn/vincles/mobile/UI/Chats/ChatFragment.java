package cat.bcn.vincles.mobile.UI.Chats;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertRetry;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivity;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivityView;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Compound.ActionCompoundView;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.Constants;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import cat.bcn.vincles.mobile.Utils.RequestsUtils;
import io.realm.Realm;

import static android.app.Activity.RESULT_OK;
import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_PHOTO;
import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_VIDEO;

public class ChatFragment extends BaseFragment implements ChatFragmentView, View.OnClickListener, ChatAdapter.ChatAdapterListener, AlertRetry.AlertSaveImageInGalleryInterface, AlertMessage.AlertMessageInterface {

    private static final int NORMAL_BOTTOM_BAR = 0;
    private static final int TEXT_BOTTOM_BAR = 1;
    private static final int AUDIO_BOTTOM_BAR = 2;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO = 0;
    private static final int MY_PERMISSIONS_REQUEST_AUDIO_RECORDING = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 3;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_CALL = 4;

    public static final String SHARE_MEDIA_IDS = "share_media_ids";
    public static final String SHARE_MEDIA_PATHS = "share_media_paths";
    public static final String SHARE_MEDIA_METADATAS = "share_media_metadatas";

    public static final String AUDIO_RECORDER_FRAGMENT_TAG = "audio_recorder_fragment_tag";
    public static final String REPOSITORY_FRAGMENT_TAG = "repository_fragment_tag";

    private static final int REQUEST_IMAGE_OR_VIDEO = 754;
    private static final String ALERT_TYPE_PERMISSIONS = "SETTINGS_PERMISSIONS";


    ChatPresenter presenter;
    View rootView;
    ViewGroup bottomBar;
    EditText messageET;
    ActionCompoundView actionCompoundView;
    ChatAdapter chatAdapter;
    private SparseArray<Contact> users;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private String idChat;
    private boolean isGroupChat;
    private boolean isDynamizer;
    private boolean isImageErrorDialog = false;
    private String newMediaFile;

    AlertRetry alertRetry;
    AlertNonDismissable alertNonDismissable;
    private OnFragmentInteractionListener mListener;

    Bundle savedInstanceState;

    ProgressBar audioProgressbar;
    TextView audioTime;

    private ImageView avatar;
    private TextView title;

    private int bottomBarState;
    public Realm realm;
    private ActionCompoundView callButton = null;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(FragmentResumed listener, String idUserSender,  boolean isGroupChat, boolean isDynamizer) {
        ChatFragment fragment = new ChatFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONTACTS);
        Bundle arguments = new Bundle();
        if (isDynamizer) isGroupChat = true;
        arguments.putString("idChat", idUserSender);
        arguments.putBoolean("isGroupChat", isGroupChat);
        arguments.putBoolean("isDynamizer", isDynamizer);
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();

        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        if (getArguments() != null){
            idChat = getArguments().getString("idChat");
            isGroupChat = getArguments().getBoolean("isGroupChat");
            isDynamizer = getArguments().getBoolean("isDynamizer");
        }


        presenter = new ChatPresenter((BaseRequest.RenewTokenFailed) getActivity(),this, savedInstanceState, idChat, new UserPreferences(getContext()), isGroupChat, isDynamizer, realm);
        if(savedInstanceState != null ){
            isImageErrorDialog = savedInstanceState.getBoolean("isImageErrorDialog");
        }


        this.savedInstanceState = savedInstanceState;

        ChatRepository chatRepository;
        if(getFragmentManager()==null){
            return;
        }
        Fragment repo = getFragmentManager().findFragmentByTag(REPOSITORY_FRAGMENT_TAG);
        if (repo instanceof ChatRepository) {
            chatRepository = (ChatRepository) repo;
        } else {
            chatRepository = null;
            getFragmentManager().beginTransaction().remove(repo);
        }

        if (chatRepository != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(chatRepository);
            chatRepository = null;
        }
        if (chatRepository == null) {
            chatRepository = ChatRepository.newInstance(presenter,
                    (BaseRequest.RenewTokenFailed) getActivity(), new UserPreferences().getUserID(),
                    Integer.parseInt(idChat), isGroupChat, isDynamizer, realm);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(chatRepository, REPOSITORY_FRAGMENT_TAG).commit();
        } else {
            chatRepository.setListeners(presenter, (BaseRequest.RenewTokenFailed) getActivity());
        }
        presenter.setChatRepository(chatRepository);

        ChatAudioRecorderFragment audioRecorderFragment = (ChatAudioRecorderFragment)getFragmentManager().findFragmentByTag(AUDIO_RECORDER_FRAGMENT_TAG);
        if (audioRecorderFragment == null) {
            audioRecorderFragment = new ChatAudioRecorderFragment();
            audioRecorderFragment.setPresenterAudio(presenter, this);
            audioRecorderFragment.setRetainInstance(true);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(audioRecorderFragment, AUDIO_RECORDER_FRAGMENT_TAG).commitAllowingStateLoss();
            presenter.setAudioRecorderFragment(audioRecorderFragment);
        } else {
            audioRecorderFragment.setPresenterAudio(presenter, this);
            presenter.setAudioRecorderFragment(audioRecorderFragment);
        }
    }

    @Override
    public void onDetach() {
        Log.d("onDetach", "onDestroy");

        super.onDetach();
        stopAudiosListening();
    }

  @Override
    public void onDestroy() {
        Log.d("onDestroy", "onDestroy");
      if (alertNonDismissable != null) {
          alertNonDismissable.dismissSafely();
      }
      super.onDestroy();

      if (realm!=null)realm.close();

      stopAudiosListening();
    }

    void stopAudiosListening(){
        try{
            if (this.chatAdapter != null){
                this.chatAdapter.stopPlayingAudio();
            }
        }catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        Log.d("onStop", "onStop");

        super.onStop();



        if( getActivity() == null){
            return;
        }
        if (ChatAudioRecorderFragment.RECORDING_AUDIO) {
            this.presenter.onClickCancelMessage();
        }

        OtherUtils.hideKeyboard(getActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        outState.putBoolean("isImageErrorDialog", isShowingImageErrorDialog());
        if (chatAdapter != null) chatAdapter.onSaveInstanceState(outState);
        outState.putString("newMediaFile", this.newMediaFile);

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        avatar = rootView.findViewById(R.id.avatar);
        title = rootView.findViewById(R.id.chat_title);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRecyclerView();

        bottomBar = view.findViewById(R.id.bottom_bar);
        actionCompoundView = view.findViewById(R.id.action);
        if (actionCompoundView != null) actionCompoundView.setOnClickListener(this);
        View backButton = view.findViewById(R.id.back);
        if (backButton != null) backButton.setOnClickListener(this);
        if (isGroupChat) {
            view.findViewById(R.id.avatar).setOnClickListener(this);
            view.findViewById(R.id.chat_title).setOnClickListener(this);
        }
        if(isShowingImageErrorDialog()) {
            showImageErrorDialog();
        }
        presenter.onCreateView();
        presenter.loadData();

        if (savedInstanceState != null){
            this.newMediaFile = savedInstanceState.getString("newMediaFile");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(callButton!=null)
        callButton.setEnabled(true);
        OtherUtils.sendAnalyticsView(getActivity(), getResources().getString(R.string.tracking_chat));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        String aux = String.valueOf(new UserPreferences().getUserID());
        Log.d("test ===>", "test"+ aux);


        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager)mLayoutManager).setReverseLayout(true);
        //((LinearLayoutManager)mLayoutManager).setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (chatAdapter != null && newState == RecyclerView.SCROLL_STATE_IDLE
                        && ((LinearLayoutManager) mLayoutManager)
                        .findLastCompletelyVisibleItemPosition() == chatAdapter.getItemCount()-1) {
                    presenter.onScrolledToTop();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO && grantResults.length > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            if (!OtherUtils.checkIfMicrophoneIsBusy()){
                AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
                alertMessage.showMessage(getActivity(),getResources().getString(R.string.error_audio_busy_for_video), "");
                return;
            }

            OtherUtils.sendVideoIntent(this);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_AUDIO_RECORDING && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onClickAudio();
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onClickFileShare();
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            newMediaFile = OtherUtils.sendPhotoIntent(this, false);
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_CAMERA_CALL && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
            actionCompoundView.performClick();
        }

    }

    @Override
    public boolean isLanguageCatalan() {
        if (getActivity() == null || !isAdded()){
            return false;
        }
        Locale current = getResources().getConfiguration().locale;
        return current.getLanguage().contains("ca");
    }

    @Override
    public void showMessages(final List<ChatElement> elementsList, final SparseArray<Contact> users) {
        if (getActivity() != null) {
            this.users = users;

        /*    if (chatAdapter == null || recyclerView.getAdapter() == null) {
                Log.d("qwer","chatfrag users size:"+((users==null) ? "0" : users.size()));
                chatAdapter = new ChatAdapter(getContext(), elementsList,
                        ChatFragment.this, users, savedInstanceState, idChat, isGroupChat);
                recyclerView.setAdapter(chatAdapter);
            } else {
                if (!compareUsersSparseArray(ChatFragment.this.users, users)) {
                    chatAdapter.setUsers(users);
                }
                Log.d("NOTIFY","showMessages");

                chatAdapter.notifyDataSetChanged();
            }*/


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("NOTIFYY","showMessages");

                    if (chatAdapter == null || recyclerView.getAdapter() == null) {
                        Log.d("qwer","chatfrag users size:"+((users==null) ? "0" : users.size()));
                        chatAdapter = new ChatAdapter(getContext(), elementsList,
                                ChatFragment.this, users, savedInstanceState, idChat, isGroupChat);
                        recyclerView.setAdapter(chatAdapter);
                    } else {
                        if (!compareUsersSparseArray(ChatFragment.this.users, users)) {
                            chatAdapter.setUsers(users);
                        }
                        Log.d("NOTIFY","showMessages");

                        chatAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void updateUsers(SparseArray<Contact> users) {
        if(users == null || chatAdapter == null){
            Log.d("NOTIFYY","chatAdapter nULL");

            return;
        }
        if (recyclerView.getAdapter() == null){

            Log.d("NOTIFYY","recyclerView.getAdapter() nULL");

            recyclerView.setAdapter(chatAdapter);
        }
        chatAdapter.setUsers(users);
        chatAdapter.notifyDataSetChanged();
        Log.d("NOTIFY","updateUsers");

    }

    private boolean compareUsersSparseArray(final SparseArray<Contact> users1, final SparseArray<Contact> users2) {
        for (int i = 0; i < users1.size(); i++) {
            Contact user1 = users1.get(users1.keyAt(i));
            Contact user2 = users2.get(user1.getId());
            if (!compareUsers(user1, user2)) return false;
        }
        return true;
    }

    private boolean compareUsers(Contact c1, Contact c2) {
        return c1.getName().equals(c2.getName()) && c1.getLastname().equals(c2.getLastname())
                && c1.getIdContentPhoto() == c2.getIdContentPhoto();
    }

    @Override
    public void reloadMessagesAdapter() {
        if (getActivity() != null && chatAdapter != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("NOTIFYY","reloadMessagesAdapter");

                    chatAdapter.notifyDataSetChanged();
                }
            });
        }
        else{
            Log.d("NOTIFYY","reloadMessagesAdapter chatAdapter nULL");

        }
    }

    @Override
    public void setChatInfo(String name, String photo) {

        if (name != null) {
            if (title == null)return;
            title.setText(name);
        }

        if (photo != null && photo.length()>0 && !photo.equals("placeholder")) {
           // ImageView avatar = rootView.findViewById(R.id.avatar);
            if (avatar == null)return;
            ImageUtils.setImageToImageView(photo, avatar, avatar.getContext(), true);

        }
    }

    @Override
    public void setChatDynamizer(int id, String photo) {
        if (photo != null && photo.length()>0 && !photo.equals("placeholder")) {
            actionCompoundView.setImagePath(photo);
        }
    }

    @Override
    public void showLoadingMessages() {

    }

    @Override
    public void hideLoadingMessages() {

    }

    @Override
    public void showWaitDialog() {
        if(getActivity() == null){return;}
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public boolean isShowingImageErrorDialog() {
        return isImageErrorDialog;
    }

    @Override
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

    @Override
    public boolean isShowingWaitDialog() {
        return alertNonDismissable != null && alertNonDismissable.alert != null && alertNonDismissable.alert.isShowing();
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void showRetryDialog() {
        alertRetry = new AlertRetry(getActivity(), this);
        alertRetry.showMessage(getString(R.string.chat_send_message_error));
    }

    @Override
    public void hideSendAgainDialog() {
        if (alertRetry != null) alertRetry.dismissSafely();
    }

    @Override
    public void onRetryAccept(AlertRetry alertRetry) {
        presenter.retrySendMessage();
    }

    @Override
    public void onRetryCancel(AlertRetry alertRetry) {
        presenter.cancelRetrySendMessage();
    }

    @Override
    public void showBottomBar() {
        bottomBarState = NORMAL_BOTTOM_BAR;

        OtherUtils.hideKeyboard(getActivity());
        bottomBar.removeAllViews();
        try {
          LayoutInflater inflater = LayoutInflater.from(getContext());
          inflater.inflate(R.layout.fragment_chat_bottom_bar, bottomBar);
        } catch (Exception e) {
          System.out.println("Error " + e.getMessage());
        }

        View textButton = bottomBar.findViewById(R.id.text);
        View cameraButton = bottomBar.findViewById(R.id.camera);
        View videoButton = bottomBar.findViewById(R.id.video);
        View audioButton = bottomBar.findViewById(R.id.audio);
        View fileButton = bottomBar.findViewById(R.id.file);

        if (textButton != null) textButton.setOnClickListener(this);
        if (cameraButton != null) cameraButton.setOnClickListener(this);
        if (videoButton != null) videoButton.setOnClickListener(this);
        if (audioButton != null) audioButton.setOnClickListener(this);
        if (fileButton != null) fileButton.setOnClickListener(this);
    }

    private int bottomBarHeight = 0;

    @Override
    public void showWritingBottomBar() {

        bottomBarState = TEXT_BOTTOM_BAR;
        bottomBar.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.fragment_chat_write_bottom_bar, bottomBar);

        View cancelButton = bottomBar.findViewById(R.id.cancel);
        messageET = bottomBar.findViewById(R.id.message_et);

        this.bottomBarHeight = bottomBar.getLayoutParams().height;
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bottomBar.setMinimumHeight(bottomBarHeight);

                if (messageET.getLineCount() > 5) {
                    bottomBar.getLayoutParams().height = bottomBarHeight+(4*(int)messageET.getTextSize());
                }else{
                    bottomBar.getLayoutParams().height = bottomBarHeight+((messageET.getLineCount()-1)*(int)messageET.getTextSize());
                }
                Log.d("LineCount", String.valueOf(bottomBar.getLayoutParams().height));

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        View sendButton = bottomBar.findViewById(R.id.send);
        messageET.requestFocus();
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(messageET, InputMethodManager.SHOW_IMPLICIT);
        }

        if (cancelButton != null) cancelButton.setOnClickListener(this);
        if (sendButton != null) sendButton.setOnClickListener(this);
    }

    @Override
    public void showAudioBottomBar() {
        if (getContext()==null)return;
        bottomBarState = AUDIO_BOTTOM_BAR;

        bottomBar.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.fragment_chat_audio_bottom_bar, bottomBar);

        View cancelButton = bottomBar.findViewById(R.id.cancel);
        View sendButton = bottomBar.findViewById(R.id.send_audio);
        audioProgressbar = bottomBar.findViewById(R.id.progressbar);
        audioTime = bottomBar.findViewById(R.id.time);
        audioProgressbar.setMax(Constants.AUDIO_RECORD_MAX_TIME);
        if (cancelButton != null) {cancelButton.setOnClickListener(this);}
        if (sendButton != null) {sendButton.setOnClickListener(this);}
    }

    @Override
    public void setAudioProgress(int progress, String time) {
        if (audioProgressbar != null) {
            audioProgressbar.setProgress(progress);
        }
        if (audioTime != null) {
            audioTime.setText(time);
        }
    }

    @Override
    public void setAction(Drawable drawable) {
        if (isGroupChat) {
            if (isDynamizer) actionCompoundView.setVisibility(View.GONE);
            else {
                actionCompoundView.setText(getResources().getString(R.string.chat_button_dinamizer));
                if (drawable == null) {
                    actionCompoundView.setImageDrawable(getResources().getDrawable(R.drawable.user));
                } else {
                    actionCompoundView.setImageDrawable(drawable);
                }
            }
        } else {
            actionCompoundView.setText(getString(R.string.chat_button_call));
        }
    }

    public void onExitScreen() {
        Log.d("groupwtch","onBackPressed");
        if (mListener != null)
            if (isGroupChat) mListener.onSetGroupMessagesAsWatched(idChat);
            else mListener.onSetUserMessagesAsWatched(presenter.getUnwatchedReceivedMessages());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                OtherUtils.hideKeyboard(getActivity());
                onExitScreen();
                Objects.requireNonNull(getFragmentManager()).popBackStack();
                break;
            case R.id.text:
                presenter.onClickText();
                break;
            case R.id.camera:
                if(OtherUtils.isLowMemory()){
                    showErrorMessage("2804");
                    return;
                }
                LoginActivity.screenOrientation = -1;

                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    newMediaFile = OtherUtils.sendPhotoIntent(this, false);
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        showSettingsAlert(getResources().getString(R.string.should_accept_permissions_camera_photo));
                    }else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
                    }

                }
                break;
            case R.id.video:
                if(OtherUtils.isLowMemory()){
                    showErrorMessage("2804");
                    return;
                }


                LoginActivity.screenOrientation = -1;

                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (!OtherUtils.checkIfMicrophoneIsBusy()){
                        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
                        alertMessage.showMessage(getActivity(),getResources().getString(R.string.error_audio_busy_for_video), "");
                        break;
                    }else{
                        OtherUtils.sendVideoIntent(this);
                    }

                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) && !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
                        showSettingsAlert(getResources().getString(R.string.should_accept_permissions_camera_video));
                    }else{
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO);
                    }

                }
                break;
            case R.id.audio:
                if(OtherUtils.isLowMemory()){
                    showErrorMessage("2804");
                    return;
                }
                if (chatAdapter!=null){
                    chatAdapter.stopPlayingAudio();
                }

                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    presenter.onClickAudio();
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
                        showSettingsAlert(getResources().getString(R.string.should_accept_permissions_microphone));
                    }
                    else{
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_AUDIO_RECORDING);
                    }

                }
                break;
            case R.id.file:
                LoginActivity.screenOrientation = -1;
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    presenter.onClickFileShare();
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                        showSettingsAlert(getResources().getString(R.string.should_accept_permissions_gallery));
                    }else{
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE);
                    }

                }
                break;
            case R.id.cancel:
                bottomBar.setMinimumHeight(this.bottomBarHeight);
                bottomBar.getLayoutParams().height = this.bottomBarHeight;
                presenter.onClickCancelMessage();
                break;
            case R.id.send:
                bottomBar.setMinimumHeight(this.bottomBarHeight);
                bottomBar.getLayoutParams().height = this.bottomBarHeight;
                presenter.onClickSendMessage(String.valueOf(messageET.getText()));
                break;
            case R.id.send_audio:
                presenter.onClickSendAudio();
                break;
            case R.id.action:

                if (isGroupChat) {
                    int chatId = presenter.getDynamizerChatId();
                    if (mListener != null && chatId != -1) {
                        mListener.onContactSelected(String.valueOf(chatId), false, true);
                    }
                } else {
                    callButton = (ActionCompoundView) v;
                    callButton.setEnabled(false);
                    if (ChatAudioRecorderFragment.RECORDING_AUDIO) {
                        this.presenter.onClickCancelMessage();
                    }
                    GetUser otherUser = presenter.getOtherUserInfoIfNotGroup();
                    Log.d("otherUSer", String.valueOf(otherUser.getIdCircle()));
                    Integer circleId = otherUser.getIdCircle();
                    Log.d("otherUSer", String.valueOf(circleId));
                    boolean userIsVincles = false;
                    if(circleId != null && circleId != -1){
                        userIsVincles = true;
                    }
                    if (otherUser != null) {
                        RequestsUtils.getInstance().cancelGalleryCalls();
                        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                            if (!OtherUtils.checkIfMicrophoneIsBusy()){
                                AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
                                alertMessage.showMessage(getActivity(),getResources().getString(R.string.error_audio_busy_for_video), "");
                                break;
                            }else{
                                Intent intent = new Intent(getContext(), CallsActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putInt(CallsActivity.EXTRAS_CALL_MODE, CallsActivityView.MAKING_CALL);
                                bundle.putInt(CallsActivity.EXTRAS_USER_ID, otherUser.getId());
                                bundle.putString(CallsActivity.EXTRAS_USER_NAME, otherUser.getName());
                                bundle.putString(CallsActivity.EXTRAS_USER_LASTNAME, otherUser.getLastname());
                                bundle.putBoolean(CallsActivity.EXTRAS_USER_IS_VINCLES, userIsVincles);
                                bundle.putString(CallsActivity.EXTRAS_USER_AVATAR_PATH, otherUser.getPhoto());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                        else {
                            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                                    && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
                                showSettingsAlert(getResources().getString(R.string.should_accept_permissions_phone_call));
                            }else{
                                requestPermissions(new String[]{
                                                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                                        MY_PERMISSIONS_REQUEST_CAMERA_CALL);
                            }
                        }

                    }
                }
                break;
            case R.id.avatar:
            case R.id.chat_title:
                if (isGroupChat) {
                    if(!isDynamizer) {
                        mListener.onGroupTitleClicked(Integer.parseInt(idChat));
                    }
                }
                break;

        }



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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("vidimg","activity result");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OtherUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            onSaveMedia(MEDIA_PHOTO);

        } else if (requestCode == OtherUtils.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri _uri = data.getData();
            if (_uri != null && "content".equals(_uri.getScheme())) {
                Cursor cursor = Objects.requireNonNull(getActivity()).getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                Objects.requireNonNull(cursor).moveToFirst();
                newMediaFile = Uri.parse(cursor.getString(0)).getPath();
                cursor.close();
            } else {
                newMediaFile = Objects.requireNonNull(_uri).getPath();
            }

            //check if it is not too big
            /*
            if (OtherUtils.isFileTooBigForServer(newMediaFile)) {
                Toast.makeText(getContext(), getResources().getString(R.string.file_too_big), Toast.LENGTH_LONG).show();
                return;
            }
*/
            onSaveMedia(MEDIA_VIDEO);
            //picture = data.getData();

        } else if (requestCode == REQUEST_IMAGE_OR_VIDEO && resultCode == RESULT_OK) {
            Log.d("vidimg","activity result ok img or vid");
            Uri uri = data.getData();

            // Retrieve the mime type of the URI
            String mimeType = ImageUtils.getMimeType(uri, getActivity());
            String path;
            try {
                path = ImageUtils.decodeFile(ImageUtils.getRealPathFromURI(uri,
                        Objects.requireNonNull(getActivity())));

                presenter.onSendSystemFile(path, mimeType);
            } catch (IOException e) {
                Log.d("vidimg", "activity result catch:" + e);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showImageErrorDialog();
            }
        }
    }

    private void onSaveMedia(int type) {
        presenter.onSaveMediaFile(newMediaFile, type);
    }


    @Override
    public void onChatElementMediaClicked(String path, String mimeType) {
        if (!mimeType.toLowerCase().contains("audio")) {
            Intent intent = new Intent(getContext(), ZoomContentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("filePath", path);
            bundle.putString("mimeType", mimeType);
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    @Override
    public void putNotificationOnWatched(ChatMessage chatMessage) {

        presenter.putNotificationOnWatched(getActivity(), chatMessage);
    }

    @Override
    protected void processPendingChanges(Bundle bundle){
        Log.d("notman2","chat processPendingChanges");
        int changesType = bundle.getInt(BaseFragment.CHANGES_TYPE);
        if (changesType == BaseFragment.CHANGES_OTHER_NOTIFICATION) {
          String type = bundle.getString("type");
            if (type != null) {
                switch (type) {
                  case "USER_UPDATED":
                  case "USER_LINKED":
                  case "USER_UNLINKED":
                  case "USER_LEFT_CIRCLE":
                  case "ADDED_TO_GROUP":
                  case "GROUP_UPDATED":
                  case "REMOVED_FROM_GROUP":
                  case "MISSED_CALL":
                    //loadContactPicture(userPreferences.getUserID());
                    Log.d("BUNDLE====>", String.valueOf(bundle));
                    ChatRepository chatRepository;
                    chatRepository = ChatRepository.newInstance(presenter,
                      (BaseRequest.RenewTokenFailed) getActivity(), new UserPreferences().getUserID(),
                      Integer.parseInt(idChat), isGroupChat, isDynamizer, realm);
                    FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
                    ft.add(chatRepository, REPOSITORY_FRAGMENT_TAG).commit();
                    break;
                }
            }

        } else if (changesType == BaseFragment.CHANGES_SHARE_MEDIA) {
            ArrayList<Integer> ids = bundle.getIntegerArrayList(SHARE_MEDIA_IDS);
            ArrayList<String> paths = bundle.getStringArrayList(SHARE_MEDIA_PATHS);
            ArrayList<String> metadatas = bundle.getStringArrayList(SHARE_MEDIA_METADATAS);
            presenter.sendFileMessage(OtherUtils.convertIntegers(ids), paths, metadatas, false);
        } else if (changesType == BaseFragment.CHANGES_NEW_MESSAGE
                || changesType == BaseFragment.CHANGES_NEW_GROUP_MESSAGE) {
            Log.d("notman2","chat processPendingChanges new message id:"+bundle.getLong("chatId")+", idChat:"+idChat);
            if (bundle.getLong("chatId") == Long.parseLong(idChat)) {
                Log.d("notman2","chat processPendingChanges new message reload messages");
                presenter.onNewMessageReceived(bundle.getLong("messageId"));
            }
        }
        pendingChangeProcessed();
    }



    @Override
    public void launchPickVideoOrPhoto() {
        SharedPreferences sp = getActivity().getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("SHOWING_CAMERA", true);
        editor.commit();

        Intent intent = new Intent(Intent.ACTION_PICK);
        // There are some devices with apps that might not handle more than one type for the ACTION_PICK
        // intent, and that will only care for the first one.
        intent.setType("image/jpeg video/mp4");
        startActivityForResult(intent, REQUEST_IMAGE_OR_VIDEO);
    }

    public void onLogout() {
        if (presenter != null) presenter.onLogout();
    }

    /**
     *
     * Methods for showing help guide
     *
     */
    @Override
    protected boolean shouldShowMenu() {
        return true;
    }

    @Override
    protected String getTextForPage(int page) {
        switch (bottomBarState) {
            case NORMAL_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_text);
                    case 1:
                        return getString(R.string.context_help_chat_photo);
                    case 2:
                        return getString(R.string.context_help_chat_video);
                    case 3:
                        return getString(R.string.context_help_chat_audio);
                    case 4:
                        return getString(R.string.context_help_chat_gallery);
                    case 5:
                        if ( this.isGroupChat ){
                            return getString(R.string.context_help_chat_profile);
                        }else{
                            return getString(R.string.context_help_chat_call);
                        }
                    default:
                        break;
                }
            case TEXT_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_send_text);
                    default:
                        break;
                }
            case AUDIO_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_record);
                    default:
                        break;
                }
        }
        return null;
    }

    @Override
    protected View getViewForPage(int page) {

        switch (bottomBarState) {
            case NORMAL_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.text);
                    case 1:
                        return rootView.findViewById(R.id.camera);
                    case 2:
                        return rootView.findViewById(R.id.video);
                    case 3:
                        return rootView.findViewById(R.id.audio);
                    case 4:
                        return rootView.findViewById(R.id.file);
                    case 5:
                        return rootView.findViewById(R.id.action);
                    default:
                        break;
                }
            case TEXT_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.bottom_bar);
                    default:
                        break;
                }
            case AUDIO_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.bottom_bar);
                    default:
                        break;
                }
        }
        return null;
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

    public interface OnFragmentInteractionListener {
        void onSetUserMessagesAsWatched(ArrayList<Long> ids);
        void onSetGroupMessagesAsWatched(String idChat);
        void onGroupTitleClicked(int chatId);
        void onContactSelected(String idUserSender, boolean isGroupChat, boolean isDynamizer);
    }
}
