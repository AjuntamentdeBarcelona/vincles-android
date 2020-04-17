package cat.bcn.vincles.mobile.Client.Model;

public class NotificationAdapterModel {

    private int id;

    private String type;

    private long creationTime;

    private int idUser = -1;
    private int idChat = -1;
    private int idMeeting = -1;
    private int idMeetingHost = -1;
    private int numberUnreadMessages = 0;
    private long meetingDate = -1;
    private boolean shouldShowButton = true;
    private boolean watched = true;
    private String meetingReminderText;
    private String code;


    public NotificationAdapterModel() {
    }

    public NotificationAdapterModel(int id, String type, long creationTime, int idUser, int numberUnreadMessages, long meetingDate) {
        this.id = id;
        this.type = type;
        this.creationTime = creationTime;
        this.idUser = idUser;
        this.numberUnreadMessages = numberUnreadMessages;
        this.meetingDate = meetingDate;
    }

    public NotificationAdapterModel(NotificationRest notificationRest) {
        this.id = notificationRest.getId();
        this.type = notificationRest.getType();
        this.creationTime = notificationRest.getCreationTime();
        this.idUser = notificationRest.getIdUser();
        this.idChat = notificationRest.getIdChat();
        this.idMeeting = notificationRest.getIdMeeting();
        this.watched = notificationRest.isWatched();
        this.code = notificationRest.getCode();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(int numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public long getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(long meetingDate) {
        this.meetingDate = meetingDate;
    }

    public int getIdMeeting() {
        return idMeeting;
    }

    public void setIdMeeting(int idMeeting) {
        this.idMeeting = idMeeting;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public int getIdMeetingHost() {
        return idMeetingHost;
    }

    public void setIdMeetingHost(int idMeetingHost) {
        this.idMeetingHost = idMeetingHost;
    }

    public boolean isShouldShowButton() {
        return shouldShowButton;
    }

    public void setShouldShowButton(boolean shouldShowButton) {
        this.shouldShowButton = shouldShowButton;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getMeetingReminderText() {
        return meetingReminderText;
    }

    public void setMeetingReminderText(String meetingReminderText) {
        this.meetingReminderText = meetingReminderText;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
