package cat.bcn.vincles.mobile.UI.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import io.realm.RealmList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    int userId;
    Context context;
    List<MeetingRealm> elementsList;
    OnItemClicked clickListener;

    public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final int MEETING_OWN = 0;
        private static final int MEETING_PENDING = 1;
        private static final int MEETING_ACCEPTED = 2;

        ViewGroup root;
        TextView timeStart, timeEnd, host, description, guests;
        FrameLayout buttonsBar;
        ImageView background;

        int meetingType;
        int meetingId;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            root = (ViewGroup) itemView;
            timeStart = itemView.findViewById(R.id.hour_start);
            timeEnd = itemView.findViewById(R.id.hour_end);
            host = itemView.findViewById(R.id.meeting_host);
            description = itemView.findViewById(R.id.meeting_description);
            guests = itemView.findViewById(R.id.meeting_guests);
            buttonsBar = itemView.findViewById(R.id.meeting_buttons_bar);
            background = itemView.findViewById(R.id.background_iv);
        }

        public void fillInfo(MeetingRealm meetingRealm) {
            UserPreferences userPreferences = new UserPreferences();
            this.meetingId = meetingRealm.getId();

            root.setOnClickListener(this);

            long startTimeMillis = meetingRealm.getDate();
            long endTimeMillis = startTimeMillis + meetingRealm.getDuration()*60*1000;
            timeStart.setText(DateUtils.getFormattedHourMinutesFromMillis(context, startTimeMillis));
            timeEnd.setText(DateUtils.getFormattedHourMinutesFromMillis(context, endTimeMillis));
            GetUser hostUser = meetingRealm.getHost();
            host.setText(host.getContext().getResources().getString(
                    R.string.calendar_date_created_by, hostUser != null ? hostUser.getName() :
                            (meetingRealm.getHostId() == userPreferences.getUserID() ?
                            userPreferences.getName() : "")));
            description.setText(meetingRealm.getDescription());
            guests.setText(getListOfUsers(meetingRealm, guests.getResources()));

            if (meetingRealm.getHostId() == userId) {
                meetingType = MEETING_OWN;
            } else {
                RealmList<Integer> guestIds = meetingRealm.getGuestIDs();
                RealmList<String> guestStates = meetingRealm.getGuestStates();
                for (int i = 0; i < guestIds.size(); i++) {
                    if (guestIds.get(i) == userId) {
                        if (guestStates.get(i).equalsIgnoreCase(MeetingUserInfoRest.ACCEPTED)) {
                            meetingType = MEETING_ACCEPTED;
                        } else {
                            meetingType = MEETING_PENDING;
                        }
                        break;
                    }
                }
            }

            background.setVisibility(meetingType == MEETING_PENDING ? View.VISIBLE : View.GONE);
            if (meetingType == MEETING_OWN) {
                host.setText(host.getContext().getResources().getString(
                        R.string.calendar_date_created_by, host.getContext().getResources()
                                .getString(R.string.chat_username_you)));
            }
            setButtonsBar();
        }

        private void setButtonsBar() {
            buttonsBar.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (meetingType) {
                case MEETING_OWN:
                    ViewGroup bar = (ViewGroup) inflater.inflate(R.layout.meeting_own_buttons, buttonsBar);
                    bar.findViewById(R.id.cancel_own_layout).setOnClickListener(this);
                    bar.findViewById(R.id.edit_layout).setOnClickListener(this);
                    break;
                case MEETING_PENDING:
                    bar = (ViewGroup) inflater.inflate(R.layout.meeting_pending_buttons, buttonsBar);
                    bar.findViewById(R.id.reject_layout).setOnClickListener(this);
                    bar.findViewById(R.id.accept_layout).setOnClickListener(this);
                    break;
                case MEETING_ACCEPTED:
                    bar = (ViewGroup) inflater.inflate(R.layout.meeting_accepted_buttons, buttonsBar);
                    bar.findViewById(R.id.cancel_accepted_layout).setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            int whatButton = -1;
            switch (v.getId()) {
                case R.id.cancel_own_layout:
                    whatButton = OnItemClicked.CANCEL_OWN;
                    break;
                case R.id.edit_layout:
                    whatButton = OnItemClicked.EDIT;
                    break;
                case R.id.reject_layout:
                    whatButton = OnItemClicked.REJECT;
                    break;
                case R.id.accept_layout:
                    whatButton = OnItemClicked.ACCEPT;
                    break;
                case R.id.cancel_accepted_layout:
                    whatButton = OnItemClicked.CANCEL;
                    break;
            }
            if (clickListener != null && whatButton != -1) {
                clickListener.onItemButtonClicked(whatButton, meetingId);
            } else if (clickListener != null) {
                clickListener.onItemClicked(meetingId);
            }
        }
    }

    private String getListOfUsers(MeetingRealm meetingRealm, Resources resources) {
        List<GetUser> users = new ArrayList<>();
        if (meetingRealm.getHost() != null && meetingRealm.getHost().getId() != this.userId){
            users.add(meetingRealm.getHost());
        }
        users.addAll(meetingRealm.getGuests());

        if (users.size() == 0) return "";
        StringBuilder usersString = new StringBuilder();
        boolean putComma = false;
        for (GetUser user : users) {
            if (putComma) {
                usersString.append(", ");
            } else {
                putComma = true;
            }
            if (user!=null){
                if (this.userId == user.getId()) {
                    usersString.append(resources.getString(R.string.chat_username_you));
                } else {
                    usersString.append(user.getName());
                }
            }
        }

        if (usersString.length() > 0) return resources.getString(R.string.calendar_date_guests,
                (usersString + "."));
        return "";
    }

    public CalendarAdapter(Context context, List<MeetingRealm> elementsList,
                           OnItemClicked clickListener) {
        this.context = context;
        this.userId = new UserPreferences(context).getUserID();
        this.elementsList = elementsList;
        this.clickListener = clickListener;
    }


    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.meeting_element, parent, false);
        return new CalendarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        holder.fillInfo(elementsList.get(position));
    }

    @Override
    public int getItemCount() {
        return elementsList.size();
    }

    public interface OnItemClicked {
        public static final int CANCEL_OWN = 0;
        public static final int EDIT = 1;
        public static final int REJECT = 2;
        public static final int ACCEPT = 3;
        public static final int CANCEL = 4;

        void onItemButtonClicked(int whatButton, int meetingId);
        void onItemClicked(int meetingId);
    }

}
