package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import cat.bcn.vincles.mobile.Client.Business.NotificationsManager;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import io.realm.Realm;
import io.realm.RealmResults;

public class ContentDetailAugmentedFragment extends BaseFragment implements ContentDetailAugmentedView, AlertMessage.AlertMessageInterface, View.OnClickListener,
        AlertConfirmOrCancel.AlertConfirmOrCancelInterface, ViewPager.OnPageChangeListener {

    private OnFragmentInteractionnAugmentedListener mListener;
    UserPreferences userPreferences;
    GalleryDb galleryDb;
    String pathImage = "";
    int index = 0;
    ContentDetailAugmentedPresenter contentDetailPresenter;
    AlertMessage alertMessageOpeningImage;
    AlertConfirmOrCancel alertConfirmOrCancel;
    ContentDetailAugmentedPagerAdapter pagerAdapter;
    ViewPager viewPager;
    int currentPage;
    String filterKind;
    private RealmResults<GalleryContentRealm> galleryContentRealmRealmResults;
    boolean itemsAdded = false;
    ContentDetailMainPresenter contentDetailMainPresenter;
    Realm realm;

    public ContentDetailAugmentedFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentDetailAugmentedActivity act = (ContentDetailAugmentedActivity) getActivity();
        realm = Realm.getDefaultInstance();

       this.pathImage = act.filePath;
       this.index = act.index;
       this.filterKind = act.filterKind;

        galleryDb = new GalleryDb(getContext());

        userPreferences = new UserPreferences(getContext());
        contentDetailPresenter = new ContentDetailAugmentedPresenter(galleryDb, filterKind, realm, this);

        UsersDb usersDb = new UsersDb(getContext());
        contentDetailMainPresenter = new ContentDetailMainPresenter(this, galleryDb,usersDb,
                userPreferences, filterKind);
        galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
        pagerAdapter = new ContentDetailAugmentedPagerAdapter(getChildFragmentManager(),
                galleryContentRealmRealmResults);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = null;
        v = inflater.inflate(R.layout.fragment_content_detail_augmented, container, false);


        v.findViewById(R.id.back).setOnClickListener(this);

        viewPager = v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(index);
        currentPage = index;
        viewPager.addOnPageChangeListener(this);

        return v;

    }




    @Override
    public void showError(Object error) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

        if (alertConfirmOrCancel != null) alertConfirmOrCancel.alert.cancel();
    }


    @Override
    public void showErrorOpeningImage() {
        alertMessageOpeningImage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
            }
        },getResources().getString(R.string.error));
        alertMessageOpeningImage.showMessage(getActivity(),getString(R.string.error_opening_image), "");
    }

    @Override
    public void contentAdded() {
        pagerAdapter.notifyDataSetChanged();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteractionAugmented(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            if (getActivity()!=null)
            getActivity().finish();
        }
    }


    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {

    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        if (position == galleryContentRealmRealmResults.size()-1){
            contentDetailMainPresenter.getContent(galleryDb.getLastContentTime());
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


    public interface OnFragmentInteractionnAugmentedListener {
        void onFragmentInteractionAugmented(Uri uri);
    }

    public interface ContentDetailAugmentedListener {
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
            pagerAdapter.notifyDataSetChanged();
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
        Log.d("CotentDelAugmtedFrag", "onDestroy");
        super.onDestroy();
        if (viewPager!=null)viewPager.setAdapter(null);
        if (realm!=null)realm.close();

    }

}
