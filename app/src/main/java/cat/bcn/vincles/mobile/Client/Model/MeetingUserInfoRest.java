package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MeetingUserInfoRest extends RealmObject {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name = "";
    @SerializedName("lastName")
    @Expose
    private String lastName = "";
    @SerializedName("idContentPhoto")
    @Expose
    private int idContentPhoto;
    @SerializedName("state")
    @Expose
    private String state = "";


    public MeetingUserInfoRest() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getIdContentPhoto() {
        return idContentPhoto;
    }

    public void setIdContentPhoto(int idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
