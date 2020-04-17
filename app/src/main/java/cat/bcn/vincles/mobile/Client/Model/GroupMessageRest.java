package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GroupMessageRest extends RealmObject {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("text")
    @Expose
    private String text = "";
    @SerializedName("sendTime")
    @Expose
    private long sendTime;
    @SerializedName("metadataTipus")
    @Expose
    private String metadataTipus = "";
    @SerializedName("idUserSender")
    @Expose
    private int idUserSender;
    @SerializedName("fullNameUserSender")
    @Expose
    private String fullNameUserSender;
    @SerializedName("idContent")
    @Expose
    private Integer idContent;
    @SerializedName("idChat")
    @Expose
    private int idChat;

    private String pathContent;

    private String metadataContent = "";

    private boolean watched;

    public GroupMessageRest() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getMetadataTipus() {
        return metadataTipus;
    }

    public void setMetadataTipus(String metadataTipus) {
        this.metadataTipus = metadataTipus;
    }

    public int getIdUserSender() {
        return idUserSender;
    }

    public void setIdUserSender(int idUserSender) {
        this.idUserSender = idUserSender;
    }

    public Integer getIdContent() {
        return idContent;
    }

    public void setIdContent(Integer idContent) {
        this.idContent = idContent;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public String getPathContent() {
        return pathContent;
    }

    public void setPathContent(String pathContent) {
        this.pathContent = pathContent;
    }

    public String getMetadataContent() {
        return metadataContent;
    }

    public void setMetadataContent(String metadataContent) {
        this.metadataContent = metadataContent;
    }

    public String getFullNameUserSender() {
        return fullNameUserSender;
    }

    public void setFullNameUserSender(String fullNameUserSender) {
        this.fullNameUserSender = fullNameUserSender;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}
