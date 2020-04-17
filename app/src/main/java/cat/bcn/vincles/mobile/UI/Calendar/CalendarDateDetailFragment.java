package cat.bcn.vincles.mobile.UI.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Chats.ChatFragment;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsAdapter;
import cat.bcn.vincles.mobile.UI.Gallery.GridSpacingItemDecoration;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class CalendarDateDetailFragment extends BaseFragment implements
        CalendarDateDetailFragmentView, View.OnClickListener, AlertMessage.AlertMessageInterface,
        AlertMessage.CancelMessageInterface, ContactsAdapter.ContactsAdapterListener {

    public static final int IS_CREATING = -1;
    private static final String DATE_PICKER_TAG = "DatePicker";
    private static final String TIME_PICKER_TAG = "TimePicker";
    private static final String PRESENTER_FRAGMENT_TAG = "presenter_date_detail_fragment_tag";

    long startDate;
    int lengthDate = 60;

    View rootView;
    ProgressBar progressBar;
    TextView description, dateTV, timeTV;
    ImageView contactsIcon;
    TextView hostName;
    OnFragmentInteractionListener interactionListener;
    CalendarDateDetailPresenterContract presenter;
    int meetingId = -1;

    private  RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private ContactsAdapter contactsAdapter;



    public CalendarDateDetailFragment(){}

    public static CalendarDateDetailFragment newInstance(FragmentResumed listener, int meetingId) {
        CalendarDateDetailFragment fragment = new CalendarDateDetailFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CALENDAR);
        Bundle arguments = new Bundle();
        arguments.putInt("meetingId", meetingId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_calendar_meeting_detail));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) meetingId = getArguments().getInt("meetingId");


        CalendarDateDetailPresenter presenterFragment = (CalendarDateDetailPresenter)
                getFragmentManager().findFragmentByTag(PRESENTER_FRAGMENT_TAG);
        if (presenterFragment != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(presenterFragment).commit();
            presenterFragment = null;
        }
        if (presenterFragment == null) {
            presenterFragment = CalendarDateDetailPresenter.newInstance((BaseRequest.RenewTokenFailed)
                            getActivity(), this, savedInstanceState);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(presenterFragment, PRESENTER_FRAGMENT_TAG).commit();
        } else {
            presenterFragment.setExternalVars((BaseRequest.RenewTokenFailed)
                    getActivity(), this, savedInstanceState);
        }
        presenter = presenterFragment;

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calendar_detail, container, false);
        progressBar = rootView.findViewById(R.id.progressbar);
        description = rootView.findViewById(R.id.description);
        contactsIcon = rootView.findViewById(R.id.contactsIcon);
        hostName = rootView.findViewById(R.id.host_name);
        dateTV = rootView.findViewById(R.id.day);
        timeTV = rootView.findViewById(R.id.hour);
        rootView.findViewById(R.id.back).setOnClickListener(this);

        initRecyclerView();

        presenter.loadMeeting(meetingId);
        presenter.onCreateView();

        return rootView;
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recyclerView);

        int numberOfColumns = getResources().getInteger(R.integer.contacts_number_of_columns);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatFragment.OnFragmentInteractionListener) {
            interactionListener = (CalendarDateDetailFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void showError(Object error) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        alertMessage.setCancelMessageInterface(this);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

    }

    @Override
    public void notifyContactChange() {
        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void showMeeting(MeetingRealm meeting, List<Contact> contacts, String hostName,
                            String hostPath) {
        description.setText(meeting.getDescription());
        startDate = meeting.getDate();
        lengthDate = meeting.getDuration();

        //fill time
        Locale current = getResources().getConfiguration().locale;
        String date = DateUtils.getMeetingDetailDate(startDate, current);
        String dateCapitalized = date.substring(0,1).toUpperCase() + date.substring(1);
        dateTV.setText(dateCapitalized);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startDate));
        String hour = DateUtils.getTimeFromHourAndMinute(
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(calendar.get(Calendar.MINUTE))) +" a ";
        calendar.setTime(new Date(startDate+lengthDate*60*1000));
        hour = hour + DateUtils.getTimeFromHourAndMinute(
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                String.valueOf(calendar.get(Calendar.MINUTE)));
        timeTV.setText(hour);

        //fill host
        this.hostName.setText(hostName == null ?
                getResources().getString(R.string.chat_username_you): hostName);
        fillHostImage(hostPath);

        //fill contact list
        contactsAdapter = new ContactsAdapter(getContext(), contacts, this);
        contactsAdapter.setShowNotificationsNumber(false);
        contactsAdapter.setDeleteVisibility(false);
        contactsAdapter.setContactSelectionEnabled(false);
        recyclerView.setAdapter(contactsAdapter);

    }

    @Override
    public void updateHostImage(String path) {
        fillHostImage(path);
    }

    private void fillHostImage(final String path) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (path == null || path.length() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        contactsIcon.setVisibility(View.GONE);
                    } else {
                        final String pathModified = path.replace("file://","");
                        progressBar.setVisibility(View.GONE);
                        contactsIcon.setVisibility(View.VISIBLE);
                        ImageUtils.setImageToImageView(pathModified.equals("placeholder") ?
                                contactsIcon.getContext().getResources().getDrawable(R.drawable.user)
                                : new File(pathModified), contactsIcon, contactsIcon.getContext(), false);

                        /*
                        Glide.with(contactsIcon.getContext())
                                .load(pathModified.equals("placeholder") ?
                                        contactsIcon.getContext().getResources().getDrawable(R.drawable.user)
                                        : new File(pathModified))
                                .apply(RequestOptions.overrideOf(200, 200))
                                .into(contactsIcon);
                                */
                    }
                }
            });
        }
    }

    private int getLengthPosition(int valueToFind) {
        int[] values = getResources().getIntArray(R.array.meeting_duration_values);
        int i = 0;
        for (int value : values) {
            if (value == valueToFind) return i;
            i++;
        }
        return -1;
    }


    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.dismissSafely();
        presenter.stopedShowingErrorDialog();
    }

    @Override
    public void onCancelAlertMessage() {
        presenter.stopedShowingErrorDialog();
    }

    @Override
    public void needContactPicturePath(int contactId, int contactType) {
        presenter.loadContactPicture(contactId);
    }

    @Override
    public void deleteCircle(int idUserToUnlink, String contactName) {
    }

    @Override
    public void clickedCircle(String idUserSender, boolean isGroupChat, boolean isDynamizer) {
    }

    @Override
    public void updateShareTitle() {

    }


    public interface OnFragmentInteractionListener {
    }


}
