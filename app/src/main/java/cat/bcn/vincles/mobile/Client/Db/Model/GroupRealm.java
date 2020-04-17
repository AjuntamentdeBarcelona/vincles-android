package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GroupRealm extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private String topic;
    private String description;
    private String photo;
    private int idDynamizer;
    private int idChat;
    private RealmList<Integer> users;
    private boolean shouldShow = true;

    private int numberUnreadMessages;
    private long numberInteractions;
    private long lastAccess;

    public GroupRealm() {

    }

    public GroupRealm(int id, String name, String topic, String description, String photo, int idDynamizer, int idChat) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.description = description;
        this.photo = photo;
        this.idDynamizer = idDynamizer;
        this.idChat = idChat;
    }

    public int getIdGroup() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }

    public int getIdDynamizer() {
        return idDynamizer;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdGroup(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setIdDynamizer(int idDynamizer) {
        this.idDynamizer = idDynamizer;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public RealmList<Integer> getUsers() {
        return users;
    }

    public void setUsers(RealmList<Integer> users) {
        this.users = users;
    }

    public boolean isShouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public int getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(int numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public long getNumberInteractions() {
        return numberInteractions;
    }

    public void setNumberInteractions(long numberInteractions) {
        this.numberInteractions = numberInteractions;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }
}
