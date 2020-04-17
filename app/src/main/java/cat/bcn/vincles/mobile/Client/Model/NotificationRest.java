package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.NotificationRestDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
//Dejamos el serializer porque en este caso nos simplifica el modelo
@JsonAdapter(NotificationRestDeserializer.class)
public class NotificationRest extends RealmObject {

    @PrimaryKey
    private int id;

    private String type;

    private long creationTime;

    private boolean processed;
    private boolean shouldBeShown = false;
    private boolean watched = false;

    //info variables
    private int idUser = -1;
    private int idMessage = -1;
    private int idChat = -1;
    private int idChatMessage = -1;
    private int idGroup = -1;
    private int idMeeting = -1;
    private String userName;
    private int numberMessages = -1;
    private String idRoom;
    private String deletedGroupName;
    private String deletedGroupPhoto;
    private String code;
    private int idGalleryContent;


    public NotificationRest() {
    }

    public NotificationRest(int id, String type, long creationTime, boolean processed, int idUser,
                            int idMessage, int idChat, int idChatMessage, int idGroup,
                            int idMeeting, String idRoom, String code, int idGalleryContent) {
        this.id = id;
        this.type = type;
        this.creationTime = creationTime;
        this.processed = processed;
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.idChat = idChat;
        this.idChatMessage = idChatMessage;
        this.idGroup = idGroup;
        this.idMeeting = idMeeting;
        this.idRoom = idRoom;
        this.code = code;
        this.idGalleryContent = idGalleryContent;
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

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public int getIdChatMessage() {
        return idChatMessage;
    }

    public void setIdChatMessage(int idChatMessage) {
        this.idChatMessage = idChatMessage;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }

    public boolean isShouldBeShown() {
        return shouldBeShown;
    }

    public void setShouldBeShown(boolean shouldBeShown) {
        this.shouldBeShown = shouldBeShown;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(String idRoom) {
        this.idRoom = idRoom;
    }

    public int getIdMeeting() {
        return idMeeting;
    }

    public void setIdMeeting(int idMeeting) {
        this.idMeeting = idMeeting;
    }

    public int getNumberMessages() {
        return numberMessages;
    }

    public void setNumberMessages(int numberMessages) {
        this.numberMessages = numberMessages;
    }

    public String getDeletedGroupName() {
        return deletedGroupName;
    }

    public void setDeletedGroupName(String deletedGroupName) {
        this.deletedGroupName = deletedGroupName;
    }

    public String getDeletedGroupPhoto() {
        return deletedGroupPhoto;
    }

    public void setDeletedGroupPhoto(String deletedGroupPhoto) {
        this.deletedGroupPhoto = deletedGroupPhoto;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getIdGalleryContent() {
        return idGalleryContent;
    }

    public void setIdGalleryContent(int idGalleryContent) {
        this.idGalleryContent = idGalleryContent;
    }
}
