package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmField;


public class GetUser extends RealmObject {

    @PrimaryKey
    @SerializedName(value="id", alternate="userId")
    @Expose
    int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("idContentPhoto")
    @Expose
    private Integer idContentPhoto;

    @SerializedName("active")
    @Expose
    private Boolean active;
    @SerializedName("idInstallation")
    @Expose
    private Integer idInstallation;
    @SerializedName("idCircle")
    @Expose
    private Integer idCircle;
    @SerializedName("idLibrary")
    @Expose
    private Integer idLibrary;
    @SerializedName("idCalendar")
    @Expose
    private Integer idCalendar;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("birthdate")
    @Expose
    private long birthdate;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("liveInBarcelona")
    @Expose
    private Boolean liveInBarcelona;

    private int numberUnreadMessages;
    private long lastInteraction;



    private Integer idContent;

    private String photoMimeType;

    @RealmField("photo")
    private String photoPath;



    public  GetUser () {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getIdContentPhoto() {
        return idContentPhoto;
    }

    public void setIdContentPhoto(Integer idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public Integer getIdContent() {
        if (idContent==null)idContent = idContentPhoto;
        return idContent;
    }

    public void setIdContent(Integer idContent) {
        this.idContent = idContent;
    }

    public String getPhoto() {
        return photoPath;
    }

    public void setPhoto(String photo) {
        this.photoPath = photo;
    }

    public String getPhotoMimeType() {
        return photoMimeType;
    }

    public void setPhotoMimeType(String photoMimeType) {
        this.photoMimeType = photoMimeType;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getIdInstallation() {
        return idInstallation;
    }

    public void setIdInstallation(Integer idInstallation) {
        this.idInstallation = idInstallation;
    }

    public Integer getIdCircle() {
        return idCircle;
    }

    public void setIdCircle(Integer idCircle) {
        this.idCircle = idCircle;
    }

    public Integer getIdLibrary() {
        return idLibrary;
    }

    public void setIdLibrary(Integer idLibrary) {
        this.idLibrary = idLibrary;
    }

    public Integer getIdCalendar() {
        return idCalendar;
    }

    public void setIdCalendar(Integer idCalendar) {
        this.idCalendar = idCalendar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getLiveInBarcelona() {
        return liveInBarcelona;
    }

    public void setLiveInBarcelona(Boolean liveInBarcelona) {
        this.liveInBarcelona = liveInBarcelona;
    }

    public int getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(int numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(long lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

}
