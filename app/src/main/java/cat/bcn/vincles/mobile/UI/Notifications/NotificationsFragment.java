package cat.bcn.vincles.mobile.UI.Notifications;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Model.NotificationAdapterModel;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Gallery.GridSpacingItemDecoration;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;

public class NotificationsFragment extends BaseFragment implements NotificationsFragmentView, View.OnClickListener, NotificationsAdapter.NotificationAdapterListener {

    private OnFragmentInteractionListener mListener;
    NotificationsPresenter presenter;
    View rootView;
    TextView noNotificationsTV;

    //list
    ArrayList<NotificationAdapterModel> notifications;
    SparseArray<Contact> users;
    SparseArray<GroupRealm> groups;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    NotificationsAdapter notificationsAdapter;
    private RecyclerView.ItemDecoration itemDecoration;
    Realm realm;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance(FragmentResumed listener) {
        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_NOTIFICATIONS);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.realm = Realm.getDefaultInstance();
        presenter = new NotificationsPresenter((BaseRequest.RenewTokenFailed) getActivity(),
                this, savedInstanceState,realm);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        if (notificationsAdapter != null) notificationsAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        presenter.deleteListeners();
        super.onDestroy();
        if (realm!=null)realm.close();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        noNotificationsTV = rootView.findViewById(R.id.no_notifications);

        View backButton = rootView.findViewById(R.id.back);
        if (backButton != null) backButton.setOnClickListener(this);

        notifications = presenter.getNotificationsList();
        initRecyclerView(savedInstanceState);

        noNotificationsTV.setVisibility(notifications.size() == 0 ? View.VISIBLE : View.GONE);

        return rootView;
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

    private void initRecyclerView(Bundle savedInstanceState) {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        if (itemDecoration == null) {
            int spacing = getResources().getDimensionPixelSize(R.dimen.adapter_image_spacing);
            itemDecoration = new GridSpacingItemDecoration(1, spacing, false);
        }
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        Locale current = getResources().getConfiguration().locale;
        users = presenter.getUsers();
        groups = presenter.getGroups();
        notificationsAdapter = new NotificationsAdapter(getContext(), notifications, users,
                groups, savedInstanceState, this);
        recyclerView.setAdapter(notificationsAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void updateList() {
        notificationsAdapter.notifyDataSetChanged();
        noNotificationsTV.setVisibility(notifications.size() == 0 ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onNotificationActionClicked(NotificationAdapterModel notification) {
        presenter.notificationActionClicked(notification);
    }

    @Override
    public void onNotificationDeleteClicked(int notificationId) {
        presenter.deleteNotification(notificationId);
    }

    @Override
    public void openGroupChat(int chatId) {
        mListener.openGroupChat(chatId);
    }

    @Override
    public void openUserChat(int userId) {
        mListener.openUserChat(userId);
    }

    @Override
    public void openContacts() {
        mListener.openContacts();
    }

    @Override
    public void openContactsGroups() {
        mListener.openContactsGroups();
    }

    @Override
    public void openMeeting(int meetingId) {
        mListener.openMeeting(meetingId);
    }

    @Override
    public void openCalendarDay(long date) {
        mListener.openCalendarDay(date);
    }

    @Override
    public void openAddToCircles(String code) {
        mListener.openAddToCircles(code);
    }

    public void onBackPressed() {
        if (mListener != null)
            mListener.onSetNotificationsAsWatched();
    }

    public interface OnFragmentInteractionListener {
        void openGroupChat(int chatId);
        void openUserChat(int userId);
        void openContacts();
        void openContactsGroups();
        void openMeeting(int meetingId);
        void openCalendarDay(long date);
        void openAddToCircles(String code);

        void onSetNotificationsAsWatched();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_notifications));

        presenter.onResume();
    }
}
