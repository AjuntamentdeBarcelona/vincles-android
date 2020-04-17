package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("topic")
    @Expose
    private String topic;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("photoString")
    @Expose
    private String photo;
    @SerializedName("dynamizer")
    @Expose
    private Dynamizer dynamizer;
    @SerializedName("idChat")
    @Expose
    private int idChat;

    public Group() {

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

    public Dynamizer getDynamizer() {
        return dynamizer;
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

    public void setDynamizer(Dynamizer dynamizer) {
        this.dynamizer = dynamizer;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id+
                ", name='" + name  +
                ", description='" + description  +
                ", dynamizer='" + dynamizer  +
                ", idChat='" + idChat  +
                '}';
    }

}
