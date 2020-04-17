package cat.bcn.vincles.mobile.UI.Calendar;


import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;

public interface CalendarFragmentView {

    static final int BUTTON_NONE = 0;
    static final int BUTTON_TODAY = 1;
    static final int BUTTON_TOMORROW = 2;
    static final int BUTTON_MONTH = 3;

    static final int TODAY = 0;
    static final int TOMORROW = 1;
    static final int OTHER_DAY = 2;

    void setButtonSelected(int which);
    void setDate(long millis, int day);

    void setDayView();
    void setMonthView(Calendar month);

    void editDate(int id);

    void showWaitDialog();
    void hideWaitDialog();
    void showError(Object error);

    void showConfirmationDialog(boolean isCancel);

    void onListsUpdated();

}
