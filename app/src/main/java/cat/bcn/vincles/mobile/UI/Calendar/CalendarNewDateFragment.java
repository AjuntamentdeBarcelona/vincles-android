package cat.bcn.vincles.mobile.UI.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Chats.ChatFragment;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsAdapter;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.Gallery.GridSpacingItemDecoration;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class CalendarNewDateFragment extends BaseFragment implements CalendarNewDateFragmentView,
        View.OnClickListener, DatePickerDialogFragment.DatePicked, TimePickerDialogFragment.TimePicked, DurationPickerDialogFragment.DurationPicked, AlertMessage.AlertMessageInterface, AlertMessage.CancelMessageInterface, ContactsAdapter.ContactsAdapterListener {

    public static final int IS_CREATING = -1;
    private static final String DATE_PICKER_TAG = "DatePicker";
    private static final String TIME_PICKER_TAG = "TimePicker";
    private static final String PRESENTER_FRAGMENT_TAG = "presenter_new_date_fragment_tag";

    boolean isEditing;
    long startDate;
    int lengthDate = 60;

    View rootView;
    EditText description;
    View inviteButton, saveButton;
    TextView dateTV, lengthTV;
    OnFragmentInteractionListener interactionListener;
    CalendarNewDatePresenterContract presenter;
    int meetingId = -1;

    private  RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private ContactsAdapter contactsAdapter;

    AlertNonDismissable alertNonDismissable;

    public CalendarNewDateFragment(){}

    public static CalendarNewDateFragment newInstance(FragmentResumed listener, int meetingId,
                                                      long startDate) {
        CalendarNewDateFragment fragment = new CalendarNewDateFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CALENDAR);
        Bundle arguments = new Bundle();
        arguments.putInt("meetingId", meetingId);
        arguments.putLong("startDate", startDate);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            meetingId = getArguments().getInt("meetingId");
            startDate = getArguments().getLong("startDate");
        }
        isEditing = meetingId != IS_CREATING;

        if (!isEditing && startDate < System.currentTimeMillis()) {
            startDate = System.currentTimeMillis() + 2 * 60 * 1000;
        }

        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);


        CalendarNewDatePresenter presenterFragment = (CalendarNewDatePresenter) getFragmentManager().findFragmentByTag(PRESENTER_FRAGMENT_TAG);
        if (presenterFragment != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(presenterFragment).commit();
            presenterFragment = null;
        }
        if (presenterFragment == null) {
            presenterFragment = CalendarNewDatePresenter.newInstance((BaseRequest.RenewTokenFailed)
                            getActivity(), this, savedInstanceState, isEditing);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(presenterFragment, PRESENTER_FRAGMENT_TAG).commit();
        } else {
            presenterFragment.setExternalVars((BaseRequest.RenewTokenFailed)
                    getActivity(), this, savedInstanceState);
        }
        presenter = presenterFragment;

    }

    @Override
    public void onStop() {
        super.onStop();
        OtherUtils.hideKeyboard(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_calendar_new_meeting));

        if (presenter != null) {
            ArrayList<Integer> contacts = ((MainFragmentManagerActivity)getActivity())
                    .getAndDeleteSelectedContacts();
            if (contacts != null && contacts.size() > 0) presenter.addContacts(contacts);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        outState.putLong("startDate", startDate);
        outState.putInt("lengthDate", lengthDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calendar_new_date, container, false);
        description = rootView.findViewById(R.id.description);
        inviteButton = rootView.findViewById(R.id.invite_button_layout);
        saveButton = rootView.findViewById(R.id.create_meeting);

        ViewGroup startDateView = rootView.findViewById(R.id.start_date);
        ViewGroup lengthDate = rootView.findViewById(R.id.length_date);
        dateTV = startDateView.findViewById(R.id.start_date_value);
        lengthTV = lengthDate.findViewById(R.id.length_date_value);
        rootView.findViewById(R.id.back).setOnClickListener(this);
        startDateView.setOnClickListener(this);
        lengthDate.setOnClickListener(this);
        inviteButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        if (startDate == -1) startDate = System.currentTimeMillis();
        if (savedInstanceState != null) {
            startDate = savedInstanceState.getLong("startDate");
            this.lengthDate = savedInstanceState.getInt("lengthDate");
        }
        setDateText();
        setLengthText();

        initRecyclerView();

        if (isEditing) {
            ((TextView)saveButton.findViewById(R.id.create_meeting_tv)).setText(
                    R.string.calendar_edit_date_save);
            presenter.loadMeeting(meetingId);
        }

        presenter.onCreateView();
        Log.d("slcon","on create view");

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
            interactionListener = (CalendarNewDateFragment.OnFragmentInteractionListener) context;
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
            case R.id.start_date:
                DatePickerDialogFragment dateDialog = new DatePickerDialogFragment();
                dateDialog.setInitialDate(startDate);
                dateDialog.setListener(this);
                dateDialog.show(getActivity().getSupportFragmentManager(), DATE_PICKER_TAG);
                break;
            case R.id.length_date:
                DurationPickerDialogFragment durationDialog = new DurationPickerDialogFragment();
                durationDialog.setWhich(getLengthPosition(lengthDate));
                durationDialog.setListener(this);
                durationDialog.show(getActivity().getSupportFragmentManager(), DATE_PICKER_TAG);
                break;
            case R.id.invite_button_layout:
                if (interactionListener != null) {
                    // Pass the ids of the selected contacts, if any, to the listener
                    interactionListener.onMeetingInviteButtonClicked(presenter == null ? Collections.<Integer> emptyList() : presenter.getContactIds());
                }
                break;
            case R.id.create_meeting:
                presenter.onSaveClicked(description.getText().toString(), startDate, lengthDate);
                break;


        }
    }

    @Override
    public void goBack() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void showWaitDialog() {
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
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
    public void showEmptyTitleError() {
        description.setError(getResources().getString(R.string.calendar_meeting_title_missing));
    }

    @Override
    public void notifyContactChange() {
        if (contactsAdapter != null) contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMeetingCreatedOrUpdated() {
        if (interactionListener != null) interactionListener.onMeetingCreatedOrUpdated();
    }

    @Override
    public void showMeeting(MeetingRealm meeting, List<Contact> contacts) {
        description.setText(meeting.getDescription());
        description.setSelection(description.getText().length());

        startDate = meeting.getDate();
        setDateText();

        lengthDate = meeting.getDuration();
        setLengthText();

        showContacts(contacts);

    }

    @Override
    public void showContacts(List<Contact> contacts) {
        contactsAdapter = new ContactsAdapter(getContext(), contacts, this);
        contactsAdapter.setShowNotificationsNumber(false);
        contactsAdapter.setDeleteVisibility(true);
        contactsAdapter.setContactSelectionEnabled(false);
        recyclerView.setAdapter(contactsAdapter);

    }

    private void setLengthText() {
        switch (lengthDate) {
            case 30:
                lengthTV.setText(R.string.calendar_meeting_duration_30);
                break;
            case 60:
                lengthTV.setText(R.string.calendar_meeting_duration_60);
                break;
            case 90:
                lengthTV.setText(R.string.calendar_meeting_duration_90);
                break;
            case 120:
                lengthTV.setText(R.string.calendar_meeting_duration_120);
                break;
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
    public void onDatePicked(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startDate));
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        startDate = calendar.getTimeInMillis();
        setDateText();

        TimePickerDialogFragment timeDialog = new TimePickerDialogFragment();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (startDate < System.currentTimeMillis() + 2 * 60 * 1000) minute = minute + 2;
        timeDialog.setHourMinute(hour, minute);
        timeDialog.setListener(this);
        timeDialog.show(getActivity().getSupportFragmentManager(), TIME_PICKER_TAG);
    }

    private void setDateText() {
        Locale current = getResources().getConfiguration().locale;
        dateTV.setText(DateUtils.getNewMeetingDate(startDate, current));
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startDate));
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        Calendar currentCalendar = Calendar.getInstance();
        if (calendar.compareTo(currentCalendar) < 0) {
            //time is past
            TimePickerDialogFragment timeDialog = new TimePickerDialogFragment();
            timeDialog.setListener(this);
            timeDialog.show(getActivity().getSupportFragmentManager(), TIME_PICKER_TAG);
            int hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
            minute = currentCalendar.get(Calendar.MINUTE)+2;
            timeDialog.setHourMinute(hour, minute);
            Toast.makeText(getContext(),getString(R.string.calendar_hour_is_past),
                    Toast.LENGTH_SHORT).show();

        } else { //time is ok
            startDate = calendar.getTimeInMillis();
            setDateText();
        }
    }

    @Override
    public void onDurationPicked(int which) {
        lengthDate = getResources().getIntArray(R.array.meeting_duration_values)[which];
        setLengthText();
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
        presenter.removeContact(idUserToUnlink);
        contactsAdapter.removeContact(idUserToUnlink);
    }

    /**
     * Given a list of contactId, apply {@link #deleteCircle(int, String) deleteCircle} to all of those
     * contacts from {@link ContactsAdapter#getContactList()} that are not present in the given
     * {@code contactIds} param. The result is that the {@link ContactsAdapter#getContactList()} will
     * only contain the contacts whose contactId are contained in the {@code contactIds} param.
     * @param contactIds List of contactIds which we want to keep in {@link ContactsAdapter#getContactList()}
     */
    public void deleteCircleNotPresent(List<Integer> contactIds) {
        if (contactsAdapter != null && contactsAdapter.getContactList() != null) {
            List<Contact> contactsInAdapter = new ArrayList<>(contactsAdapter.getContactList());
            for (Contact contact : contactsInAdapter) {
                if (!contactIds.contains(contact.getId())) {
                    deleteCircle(contact.getId(), contact.getName() + " " + contact.getLastname());
                }
            }
        }
    }

    @Override
    public void clickedCircle(String idUserSender, boolean isGroupChat, boolean isDynamizer) {

    }

    @Override
    public void updateShareTitle() {

    }


    public interface OnFragmentInteractionListener {
        void onMeetingInviteButtonClicked(List<Integer> alreadySelectedContactIds);
        void onMeetingCreatedOrUpdated();
    }


}
