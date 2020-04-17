package cat.bcn.vincles.mobile.UI.Chats.Model;

import java.util.ArrayList;

public class ChatMessage extends ChatElement {

    long id;
    int idUserFrom;
    String fullNameUserSender;
    boolean watched;
    private Integer notificationId = -1;


    public ChatMessage(int type, long sendTime, String text, long id, int idUserFrom, String fullNameUserSender, boolean watched) {
        super(type, sendTime, text);
        this.id = id;
        this.idUserFrom = idUserFrom;
        this.fullNameUserSender = fullNameUserSender;
        this.watched = watched;
    }

    public ChatMessage(int type, long sendTime, String text, long id, int idUserFrom, String fullNameUserSender, boolean watched, Integer notificationId) {
        super(type, sendTime, text);
        this.id = id;
        this.idUserFrom = idUserFrom;
        this.fullNameUserSender = fullNameUserSender;
        this.watched = watched;
        this.notificationId = notificationId;
    }

    public long getId() {
        return id;
    }

    public int getIdUserFrom() {
        return idUserFrom;
    }

    public boolean isWatched() {
        return watched;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setIdUserFrom(int idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getFullNameUserSender() {
        return fullNameUserSender;
    }

    public void setFullNameUserSender(String fullNameUserSender) {
        this.fullNameUserSender = fullNameUserSender;
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }
}
