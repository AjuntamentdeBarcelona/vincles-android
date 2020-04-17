package cat.bcn.vincles.mobile.UI.Notifications;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;

public interface NotificationsFragmentView {

    void updateList();

    void openGroupChat(int groupId);
    void openUserChat(int userId);
    void openContacts();
    void openContactsGroups();
    void openMeeting(int meetingId);
    void openCalendarDay(long date);
    void openAddToCircles(String code);
    Resources getResources();

}
