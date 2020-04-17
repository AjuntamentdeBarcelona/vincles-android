package cat.bcn.vincles.mobile.UI.Chats.Model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ChatElement implements Comparable<ChatElement> {

    public final static int TYPE_USER_TEXT = 0;
    public final static int TYPE_USER_TEXT_FIRST = 1;
    public final static int TYPE_USER_IMAGE = 2;
    public final static int TYPE_USER_IMAGE_FIRST = 3;
    public final static int TYPE_USER_AUDIO = 4;
    public final static int TYPE_USER_AUDIO_FIRST = 5;
    public final static int TYPE_ME_TEXT = 6;
    public final static int TYPE_ME_TEXT_FIRST = 7;
    public final static int TYPE_ME_IMAGE = 8;
    public final static int TYPE_ME_IMAGE_FIRST = 9;
    public final static int TYPE_ME_AUDIO = 10;
    public final static int TYPE_ME_AUDIO_FIRST = 11;
    public final static int TYPE_ALERT_MISSED_CALL = 50;
    public final static int TYPE_ALERT_NOT_READ = 100;
    public final static int TYPE_ALERT_DATE = 200;

    int type = 0;
    long sendTime;
    String text;

    public ChatElement(int type, long sendTime, String text) {
        this.type = type;
        this.sendTime = sendTime;
        this.text = text;
    }

    private int compare(ChatElement o1, ChatElement o2) {
        if (o1.sendTime != o2.sendTime) {
            return o1.sendTime > o2.sendTime ? -1 : 1;
        }
        if (o1.type == o2.type) {
            return 0;
        }
        return o1.type < o2.type  ? -1 : 1;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int compareTo(@NonNull ChatElement o) {
        return compare(this, o);
    }
}
