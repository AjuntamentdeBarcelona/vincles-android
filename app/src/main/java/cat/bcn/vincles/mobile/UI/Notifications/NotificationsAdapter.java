package cat.bcn.vincles.mobile.UI.Notifications;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacksGallery;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.NotificationAdapterModel;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Compound.DeleteNotificationCompoundView;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    public static final int USER_ME = -1;
    Dynamizer dynamizer;

    Locale locale;
    Context context;
    ArrayList<NotificationAdapterModel> elementsList;
    NotificationAdapterListener listener;
    private boolean deleteVisibility;
    SparseArray<Contact> users;
    SparseArray<GroupRealm> groups;
    MediaManager mediaManager;


    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        View root;
        ImageView avatar;
        DeleteNotificationCompoundView delete;
        TextView date, content;
        View actionButton;
        ImageView actionIcon;
        TextView actionText;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            avatar = itemView.findViewById(R.id.avatar);
            delete = itemView.findViewById(R.id.delete);
            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.content);
            actionButton = itemView.findViewById(R.id.action_button);
            actionIcon = actionButton.findViewById(R.id.action_icon);
            actionText = actionButton.findViewById(R.id.action_text);
        }


    }


    NotificationsAdapter(Context context, ArrayList<NotificationAdapterModel> elementsList,
                         SparseArray<Contact> users, SparseArray<GroupRealm> groups,
                         Bundle savedState, NotificationAdapterListener listener){
        this.context = context;
        locale = context.getResources().getConfiguration().locale;
        this.elementsList = elementsList;
        this.listener = listener;
        this.users = users;
        this.groups = groups;
        this.mediaManager = new MediaManager(context);
        loadSavedState(savedState);
    }

    public void onSaveInstanceState(Bundle outState) {

    }

    private void loadSavedState(Bundle state) {
        if (state != null) {
        }
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {
        final NotificationAdapterModel notification = elementsList.get(position);
        holder.date.setText(DateUtils.getNotificationFormatedTime(notification.getCreationTime(),
                locale));

        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNotificationActionClicked(notification);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNotificationDeleteClicked(notification.getId());
            }
        });

        holder.root.setBackground(holder.root.getContext().getResources().getDrawable(
                notification.isWatched() ? R.drawable.chat_alert_background_watched
                        : R.drawable.chat_alert_background
        ));

        holder.actionButton.setVisibility(notification.isShouldShowButton() ? View.VISIBLE : View.GONE);



        switch (notification.getType()) {

            case "USER_LINKED":
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(), Contact.TYPE_USER_CIRCLE );
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_new_contact, users.get(notification.getIdUser())
                                .getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_chat));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_send_message));
                break;

            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_no_longer_contact, users.get(notification.getIdUser())
                                .getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_contacts));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_contacts_button));
                break;

            case "NEW_MESSAGE":
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(),notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                int unreadMessages = users.get(notification.getIdUser())
                        .getNumberNotifications(); //number unread messages
                holder.content.setText(holder.content.getResources().getQuantityString(
                        R.plurals.notifications_messages_user_plurals, unreadMessages,
                        unreadMessages, users.get(notification.getIdUser()).getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_chat));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_send_message));
                break;

            case "MEETING_INVITATION_EVENT":
                Locale loc = holder.content.getContext().getResources().getConfiguration().locale;
                long meetingDate = notification.getMeetingDate();
                Log.d("dynpht","noti adapter, invitation photo:"+users.get(notification.getIdUser()).getPath());
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_new_date, users.get(notification.getIdUser())
                                .getName(), DateUtils.getMeetingDetailDate(meetingDate, loc),
                        DateUtils.getFormattedHourMinutesFromMillis(holder.content.getContext(),
                                meetingDate)));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_date));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case NotificationsDb.MEETING_REMINDER_NOTIFICATION_TYPE:
                UserPreferences userPreferences = new UserPreferences(
                        MyApplication.getAppContext());
                String photo = notification.getIdUser() == userPreferences.getUserID() ? userPreferences.getUserAvatar() : users.get(notification.getIdUser()).getPath();
                photo = (photo == null ? "" :  photo.replace("file://",""));
                setAvatar(holder.avatar, photo, notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(notification.getMeetingReminderText());
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_date));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case "MEETING_CHANGED_EVENT":
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_date_updated, users.get(notification.getIdUser())
                                .getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_date));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case "MEETING_ACCEPTED_EVENT":
                loc = holder.content.getContext().getResources().getConfiguration().locale;
                meetingDate = notification.getMeetingDate();
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_date_accepted, users.get(notification.getIdUser())
                                .getName(), DateUtils.getMeetingDetailDate(meetingDate, loc),
                        DateUtils.getFormattedHourMinutesFromMillis(holder.content.getContext(),
                                meetingDate)));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_date));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case "MEETING_REJECTED_EVENT":
                loc = holder.content.getContext().getResources().getConfiguration().locale;
                meetingDate = notification.getMeetingDate();
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_date_rejected, users.get(notification.getIdUser())
                                .getName(), DateUtils.getMeetingDetailDate(meetingDate, loc),
                        DateUtils.getFormattedHourMinutesFromMillis(holder.content.getContext(),
                                meetingDate)));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_date));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                loc = holder.content.getContext().getResources().getConfiguration().locale;
                meetingDate = notification.getMeetingDate();
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_date_revoked, users.get(notification.getIdUser())
                                .getName(), DateUtils.getMeetingDetailDate(meetingDate, loc),
                        DateUtils.getFormattedHourMinutesFromMillis(holder.content.getContext(),
                                meetingDate)));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_calendar));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_create_date));
                break;
            case "ADDED_TO_GROUP":
                setAvatar(holder.avatar, groups.get(notification.getIdUser()).getPhoto(), notification.getIdUser(),  Contact.TYPE_GROUP);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_new_group, groups.get(notification.getIdUser())
                                .getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_chat));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_send_message));
                break;
            case "NEW_CHAT_MESSAGE":
                String name;
                UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                dynamizer = userGroupsDb.findDynamizerFromChatIdUnmanaged(notification.getIdChat());

                if(dynamizer != null) {
                    setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                    unreadMessages = users.get(notification.getIdUser())
                            .getNumberNotifications(); //number unread messages
                    
                    holder.content.setText(holder.content.getResources().getQuantityString(
                            R.plurals.notifications_messages_user_plurals, unreadMessages,
                            unreadMessages, users.get(notification.getIdUser()).getName()));
                    holder.actionText.setText(holder.actionText.getResources().getString(
                            R.string.notifications_see_chat));
                    holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                            R.drawable.selector_send_message));

                } else {
                    if (groups.get(notification.getIdUser()) == null) {
                        setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                        unreadMessages = users.get(notification.getIdUser())
                                .getNumberNotifications(); //number unread messages

                        name = users.get(notification.getIdUser()).getName();
                    } else {
                        setAvatar(holder.avatar, groups.get(notification.getIdUser()).getPhoto(), notification.getIdUser(),  Contact.TYPE_GROUP);
                        unreadMessages = groups.get(notification.getIdUser())
                                .getNumberUnreadMessages(); //number unread messages
                        name = groups.get(notification.getIdUser()).getName();
                    }
                    holder.content.setText(holder.content.getResources().getQuantityString(
                            R.plurals.notifications_messages_group_plurals, unreadMessages,
                            unreadMessages, name));
                    holder.actionText.setText(holder.actionText.getResources().getString(
                            R.string.notifications_see_chat));
                    holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                            R.drawable.selector_send_message));
                }
                break;
            case "REMOVED_FROM_GROUP":
                setAvatar(holder.avatar, groups.get(notification.getIdUser()).getPhoto(), notification.getIdUser(), Contact.TYPE_GROUP);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_deleted_group, groups.get(notification.getIdUser())
                                .getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_groups));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_groups_button));
                break;

            case NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE:
                Contact user = users.get(notification.getIdUser());
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.notifications_missed_call, user.getName()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.notifications_see_chat));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_send_message));
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                Log.d("gdp","notif adapter, id::"+notification.getIdUser());
                user = users.get(notification.getIdUser());
                setAvatar(holder.avatar, users.get(notification.getIdUser()).getPath(), notification.getIdUser(),  Contact.TYPE_USER_CIRCLE);
                holder.content.setText(holder.content.getResources().getString(
                        R.string.group_detail_invite_notification, user.getName(), notification.getCode()));
                holder.actionText.setText(holder.actionText.getResources().getString(
                        R.string.add_contacts));
                holder.actionIcon.setImageDrawable(holder.actionIcon.getResources().getDrawable(
                        R.drawable.selector_add_contact_button));
                break;
        }


    }

    private void setAvatar(final ImageView imageView, String path, int idUser, int type) {
        if (path != null && path.length()>0 && !path.equals("placeholder")) {
            path = path.replace("file://", "");
        }


    //TODO
        this.mediaManager.downloadContactItem(imageView, 0, path, idUser, type, -1, false, new MediaCallbacksGallery() {
            @Override
            public void onSuccess(final HashMap<Integer, Object> response) {
                final String theFilePath = (String) response.get(1);

                Handler mainHandler = new Handler(context.getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (theFilePath == null || theFilePath.length() == 0 || theFilePath.equals("placeholder")) {
                            imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.user));
                        }
                        else{
                            ImageUtils.setImageToImageView(new File(theFilePath), imageView, imageView.getContext(), true);
                        }
                    }
                });

            }

            @Override
            public void onFailure(HashMap<Integer, Object> response) {
                imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.user));

            }
        });



        /*if (path == null || path.length() == 0 || path.equals("placeholder")) {
            imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.user));
        } else {
            ImageUtils.setImageToImageView(new File(path), imageView, imageView.getContext(), true);*/

            /*
            Glide.with(imageView.getContext())
                    .load(new File(path))
                    .apply(new RequestOptions().overrideOf(128, 128)
                            .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageView);
                    */
        /* }*/
    }


    @Override
    public int getItemCount() {
        return elementsList.size();
    }



    public interface NotificationAdapterListener {
        void onNotificationActionClicked(NotificationAdapterModel notification);
        void onNotificationDeleteClicked(int notificationId);
    }

}
