package cat.bcn.vincles.mobile.UI.ContentDetail;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import cat.bcn.vincles.mobile.Client.Business.NotificationsManager;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Compound.BackCompoundView;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ContentDetailFragment extends BaseFragment implements ContentDetailView, AlertMessage.AlertMessageInterface, View.OnClickListener,
        AlertConfirmOrCancel.AlertConfirmOrCancelInterface, ViewPager.OnPageChangeListener, ContentDetailPagerFragment.ViewRequest {

    private OnFragmentInteractionListener mListener;
    UserPreferences userPreferences;
    GalleryDb galleryDb;
    UsersDb usersDb;
    ImageView avatar;
    View deleteBtn;
    View shareButton;
    TextView ownerNameTv, dateTv, hourTv;
    String pathImage = "";
    int index = 0;
    ContentDetailPresenter contentDetailPresenter;
    AlertMessage alertMessageErrorRemovinContent;
    AlertMessage alertMessageOpeningImage;
    AlertConfirmOrCancel alertConfirmOrCancel;
    ContentDetailPagerAdapter pagerAdapter;
    ViewPager viewPager;
    Button nextButton, previousButton;
    TextView nextTV, previousTV;
    int currentPage;
    String filterKind;
    private String avatarPath;
    private RealmResults<GalleryContentRealm> galleryContentRealmRealmResults;
    private ContentDetailListener listener;
    private boolean sharedMedia;
    private boolean itemsAdded = false;
    private GalleryContentRealm galleryContentRealm;
    ContentDetailMainPresenter contentDetailMainPresenter;
    Realm realm;
    private FrameLayout bottomBar;
    private LinearLayout userInfo;
    private ImageView clockIcon;
    private View dateDivider;
    public boolean isFullScreen = false;
    private View v;
    private BackCompoundView backButton;
    public Boolean isLoadingMore = false;


    public ContentDetailFragment() {
        // Required empty public constructor
    }

    public static ContentDetailFragment newInstance(BaseFragment.FragmentResumed listener, String path, int index, String filterKind) {
        ContentDetailFragment fragment = new ContentDetailFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        args.putInt("index", index);
        args.putString("filterKind", filterKind);
        fragment.setArguments(args);
        fragment.setListener(listener, BaseFragment.FragmentResumed.FRAGMENT_GALLERY);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        if (savedInstanceState != null) {
            isFullScreen = savedInstanceState.getBoolean("isFullScreen");
        }
        this.pathImage = getArguments().getString("path", "");
        this.index = getArguments().getInt("index", 0);
        this.filterKind = getArguments().getString("filterKind");
        galleryDb = new GalleryDb(getContext());
        usersDb = new UsersDb(getContext());
        userPreferences = new UserPreferences(getContext());
        contentDetailPresenter = new ContentDetailPresenter((BaseRequest.RenewTokenFailed) getActivity(),this, galleryDb,usersDb,
                userPreferences, filterKind, realm);
        contentDetailMainPresenter = new ContentDetailMainPresenter((BaseRequest.RenewTokenFailed) getActivity(),this, galleryDb,usersDb,
                userPreferences, filterKind);
        galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
        pagerAdapter = new ContentDetailPagerAdapter(getChildFragmentManager(),
                galleryContentRealmRealmResults, this);

        galleryContentRealmRealmResults.addChangeListener(new RealmChangeListener<RealmResults<GalleryContentRealm>>() {
            @Override
            public void onChange(@NonNull RealmResults<GalleryContentRealm> galleryContentRealms) {
                contentAdded();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_content_detail, container, false);

        if (sharedMedia) {
            sharedMedia = false;
        }

        ownerNameTv = v.findViewById(R.id.chat_title);
        dateTv = v.findViewById(R.id.date);
        hourTv = v.findViewById(R.id.hour);
        deleteBtn = v.findViewById(R.id.delete);
        if (deleteBtn == null) {
            deleteBtn = v.findViewById(R.id.delete);
        }
        shareButton = v.findViewById(R.id.share);
        nextButton = v.findViewById(R.id.next);
        previousButton = v.findViewById(R.id.before);
        nextTV = v.findViewById(R.id.next_text);
        previousTV = v.findViewById(R.id.beforeText);

        bottomBar = v.findViewById(R.id.bottom_bar);
        userInfo = v.findViewById(R.id.userInfo);
        clockIcon = v.findViewById(R.id.clockIcon);
        dateDivider = v.findViewById(R.id.date_divider);

        deleteBtn.setOnClickListener(this);
        backButton = v.findViewById(R.id.back);

        v.findViewById(R.id.back).setOnClickListener(this);
        if (nextButton != null) nextButton.setOnClickListener(this);
        if (previousButton != null) previousButton.setOnClickListener(this);
        if (shareButton != null) shareButton.setOnClickListener(this);

        contentDetailPresenter.loadOwnerName(index);
        contentDetailPresenter.loadDate(getContext(), index);

        avatar = v.findViewById(R.id.avatar);
        if (avatar != null) {
            contentDetailPresenter.loadAvatar(index);
        }

        viewPager = v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(index);
        currentPage = index;
        viewPager.addOnPageChangeListener(this);
        setButtonsForPosition(index);

        if (isFullScreen)showAugmentedDetail();

        return v;
    }

    @Override
    protected void processPendingChanges(Bundle bundle){
        if (bundle.getInt(BaseFragment.CHANGES_TYPE) == BaseFragment.CHANGES_OTHER_NOTIFICATION) {
            String type = bundle.getString("type");
            switch (type) {
                case "USER_UPDATED":
                    contentDetailPresenter.onUserUpdated(bundle.getInt("idUser"));
                    break;
            }
        }
        pendingChangeProcessed();
    }


    @Override
    public void setOwnerName (String ownerName) {
        ownerNameTv.setText(ownerName);
        //In constraint layout 1.0.2 there is a bug with wrap when changing the text (size) of the
        //textview. This forces it to recalculate
        //  LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ownerNameTv.getLayoutParams();
        // params.width = 0;
        // ownerNameTv.setLayoutParams(params);
    }



    @Override
    public void setDate(int day, int month, int year, String formatedTime) {
        Locale current = getResources().getConfiguration().locale;
        String lang = current.getLanguage();
        String date = DateUtils.getFormatedDate(lang.equals("ca"),day,month,year);
        dateTv.setText(date);
        hourTv.setText(formatedTime);
    }

    @Override
    public void showAvatar (final String path) {
        avatarPath = path;
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageUtils.setImageToImageView(path.equals("placeholder") ?
                            getResources().getDrawable(R.drawable.user)
                            : new File(path), avatar, getContext(), false);
                }
            });
        }
    }

    @Override
    public void showError(Object error) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

        if (alertConfirmOrCancel != null) alertConfirmOrCancel.alert.cancel();
    }

    @Override
    public void showConfirmationRemoveContent() {
        alertConfirmOrCancel = new AlertConfirmOrCancel(getActivity(),this);
        alertConfirmOrCancel.showMessage(getString(R.string.remove_item_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
    }

    @Override
    public void removedContent() {
        if (!isAdded())return;
        if (alertConfirmOrCancel != null && alertConfirmOrCancel.alert != null
                && alertConfirmOrCancel.alert.isShowing()) {
            alertConfirmOrCancel.alert.dismiss();
        }

        if (getFragmentManager() != null){
            try {
                goBack();
            }catch (Exception e){
                Log.e("Exception", "Error");
            }
        }
    }

    @Override
    public void showErrorRemovingContent() {
        alertMessageErrorRemovinContent = new AlertMessage(this,AlertMessage.TITTLE_ERROR);
        alertMessageErrorRemovinContent.showMessage(getActivity(),getResources().getString(R.string.error_removing_content), "");
    }

    @Override
    public void showErrorOpeningImage() {
        alertMessageOpeningImage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                goBack();
            }
        },getResources().getString(R.string.error));
        alertMessageOpeningImage.showMessage(getActivity(),getString(R.string.error_opening_image), "");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContentDetailListener) {
            listener = (ContentDetailListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if (context instanceof ContentDetailFragment.OnFragmentInteractionListener) {
            mListener = (ContentDetailFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if (galleryContentRealmRealmResults != null){
            galleryContentRealmRealmResults.removeAllChangeListeners();
        }
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            if (!isFullScreen){
                goBack();
            }
            else{
                hideAugmentedDetail();
            }

        } else if (view.getId() == R.id.delete || view.getId() == R.id.delete) {
            showConfirmationRemoveContent();
        } else if (view.getId() == R.id.next) {
            viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
        } else if (view.getId() == R.id.before) {
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        } else if (view.getId() == R.id.share) {
            if (listener != null) {
                sharedMedia = true;
                GalleryContentRealm content = galleryContentRealmRealmResults.get(index);
                listener.onGalleryShareButtonClicked(content.getId(), content.getPath(),
                        content.getMimeType());
            }
        }
    }

    private void goBack(){
        if (getFragmentManager()!=null)
            getFragmentManager().popBackStack();
    }

    private void setButtonsForPosition(int position) {
        if (previousButton==null || nextButton==null || isFullScreen) return;
        if (position == 0) {
            previousButton.setVisibility(View.INVISIBLE);
            previousTV.setVisibility(View.INVISIBLE);
            if(pagerAdapter.getCount()>1) {
                nextButton.setVisibility(View.VISIBLE);
                nextTV.setVisibility(View.VISIBLE);
            } else{
                nextButton.setVisibility(View.INVISIBLE);
                nextTV.setVisibility(View.INVISIBLE);
            }
        } else if (position == pagerAdapter.getCount()-1) {
            nextButton.setVisibility(View.INVISIBLE);
            nextTV.setVisibility(View.INVISIBLE);
            previousButton.setVisibility(View.VISIBLE);
            previousTV.setVisibility(View.VISIBLE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
            previousTV.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            nextTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        if (alertConfirmOrCancel != null && alertConfirmOrCancel.alert != null
                && alertConfirmOrCancel.alert.isShowing()) {
            alertConfirmOrCancel.alert.dismiss();
        }
        contentDetailPresenter.deleteContent(viewPager.getCurrentItem());
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.alert.dismiss();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setButtonsForPosition(position);
        currentPage = position;
        contentDetailPresenter.updateUserID(currentPage);
        contentDetailPresenter.loadOwnerName(currentPage);
        contentDetailPresenter.loadDate(getContext(), currentPage);
        if (avatar != null) {
            contentDetailPresenter.loadAvatar(currentPage);
        }

        if (position == galleryContentRealmRealmResults.size()-1 && !isLoadingMore){
            contentDetailMainPresenter.getContent(galleryDb.getLastContentTime());
            isLoadingMore = true;
        }

    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (itemsAdded){
            pagerAdapter.notifyDataSetChanged();
            itemsAdded = false;
            viewPager.setCurrentItem(currentPage-1);
        }
    }

    @Override
    public void onViewRequest(int position) {
        if (!isFullScreen){
            showAugmentedDetail();
        }
        else{
            GalleryContentRealm galleryContent =  galleryContentRealmRealmResults.get(position);
            if (galleryContent==null)return;
            boolean isVideo = (galleryContent.getMimeType() != null && galleryContent.getMimeType().startsWith("video"));

            if(this.userInfo != null){
                this.userInfo.bringToFront();
            }
            if(this.backButton != null){
                this.backButton.bringToFront();
            }

            if (isVideo) {
                Intent intent = new Intent(getContext(), ZoomContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("filePath", galleryContent.getPath());
                bundle.putString("mimeType", galleryContent.getMimeType());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }


        /*this.index = position;
        this.galleryContentRealm = galleryContentRealmRealmResults.get(position);
        mListener.onGalleryDetailItemPicked(galleryContentRealm, index, filterKind);*/

    }

    private void showAugmentedDetail() {
        isFullScreen = true;
        if (getActivity()!=null){
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        AppCompatActivity acg = ((AppCompatActivity)getActivity());
        if (acg!=null){
            if (acg.getSupportActionBar()!=null)
                acg.getSupportActionBar().hide();
        }


        bottomBar.setVisibility(View.GONE);
        if (userInfo!=null)
            userInfo.setVisibility(View.GONE);
        if (avatar != null)
            avatar.setVisibility(View.GONE);
        if (dateDivider!=null)
            dateDivider.setVisibility(View.GONE);
        if (ownerNameTv!= null)
            ownerNameTv.setVisibility(View.GONE);
        dateTv.setVisibility(View.GONE);
        clockIcon.setVisibility(View.GONE);
        hourTv.setVisibility(View.GONE);
        if(nextButton!=null)
            nextButton.setVisibility(View.GONE);
        if(previousButton!=null)
            previousButton.setVisibility(View.GONE);
        if(nextTV!=null)
            nextTV.setVisibility(View.GONE);
        if(previousTV!=null)
            previousTV.setVisibility(View.GONE);

        ViewGroup.MarginLayoutParams backLp = (ViewGroup.MarginLayoutParams) backButton.getLayoutParams();
        backLp.setMargins((int)getResources().getDimension(R.dimen.gallery_padding_top_bottom),
                (int)getResources().getDimension(R.dimen.gallery_padding_top_bottom),
                (int)getResources().getDimension(R.dimen.gallery_padding_top_bottom),
                (int)getResources().getDimension(R.dimen.gallery_padding_top_bottom));
        backButton.setLayoutParams(backLp);


        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.setMargins(0,0,0,0);
        viewPager.setLayoutParams(lp);

        v.setPadding(0,0,0,0);

    }

    public void hideAugmentedDetail(){
        isFullScreen = false;
        if (getActivity()!=null){
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        AppCompatActivity acg = ((AppCompatActivity)getActivity());
        if (acg!=null){
            if (acg.getSupportActionBar()!=null)
                acg.getSupportActionBar().show();
        }
        bottomBar.setVisibility(View.VISIBLE);
        if (userInfo!=null)
            userInfo.setVisibility(View.VISIBLE);
        if (avatar != null)
            avatar.setVisibility(View.VISIBLE);
        if (dateDivider != null)
            dateDivider.setVisibility(View.VISIBLE);
        if (ownerNameTv!= null)
            ownerNameTv.setVisibility(View.VISIBLE);
        dateTv.setVisibility(View.VISIBLE);
        clockIcon.setVisibility(View.VISIBLE);
        hourTv.setVisibility(View.VISIBLE);
        if(nextButton!=null)
            nextButton.setVisibility(View.VISIBLE);
        if(previousButton!=null)
            previousButton.setVisibility(View.VISIBLE);
        if(nextTV!=null)
            nextTV.setVisibility(View.VISIBLE);
        if(previousTV!=null)
            previousTV.setVisibility(View.VISIBLE);


        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.setMargins(0,(int)getResources().getDimension(R.dimen.gallery_recycler_margin_top),0,(int)getResources().getDimension(R.dimen.gallery_padding_top_bottom));
        lp.width = 0;
        lp.height = 0;
        viewPager.setLayoutParams(lp);

        v.setPadding((int)getResources().getDimension(R.dimen.gallery_padding_sides),
                (int)getResources().getDimension(R.dimen.gallery_padding_top_bottom),
                (int)getResources().getDimension(R.dimen.gallery_padding_sides),
                (int)getResources().getDimension(R.dimen.gallery_padding_top_bottom));

        ViewGroup.MarginLayoutParams backLp = (ViewGroup.MarginLayoutParams) backButton.getLayoutParams();
        backLp.setMargins(0,0,0,0);
        backButton.setLayoutParams(backLp);

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void onGalleryDetailItemPicked(GalleryContentRealm galleryContent, int index, String filterKind);

    }


    @Override
    public String getAvatarPath() {
        return avatarPath;
    }

    @Override
    public void contentAdded() {
        if(!isAdded())return;
        galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void isLoadingMore(boolean isLoading) {
        isLoadingMore = isLoading;
    }


    public interface ContentDetailListener {
        void onGalleryShareButtonClicked(int idContent, String path, String metadata);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null){
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(notificationProcessedBroadcastReceiver,
                    new IntentFilter(NotificationsManager.NOTIFICATION_PROCESSED_BROADCAST));
        }

        if (pagerAdapter != null) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    pagerAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(notificationProcessedBroadcastReceiver);
        }

    }

    private BroadcastReceiver notificationProcessedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNotificationProcessed(intent);
        }
    };

    private void onNotificationProcessed(Intent intent) {
        if(intent.getExtras() != null){
            Bundle b = intent.getExtras().getBundle("bundle");
            if (b != null && b.getString("type" ) != null && Objects.equals(b.getString("type"), "CONTENT_ADDED_TO_GALLERY")){
                itemsAdded = true;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("ContentDetailFragment", "onDestroy");
        super.onDestroy();

        if (realm!=null)realm.close();

    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("isFullScreen", isFullScreen);
    }

}
