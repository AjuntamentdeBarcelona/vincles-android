package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class UserRegister {

    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String CAT = "CAT";
    public static final String ESP = "ESP";
    public static final String LANGUAGE_NOT_SET = "LANGUAGE_NOT_SET";
    public static final boolean LIVES_IN_BARCELONA = true;
    public static final boolean NOT_LIVES_IN_BARCELONA = false;

    @SerializedName("email")
    String email;
    @SerializedName("password")
    String password;
    @SerializedName("alias")
    String alias;
    @SerializedName("name")
    String name;
    @SerializedName("username")
    String username;
    @SerializedName("lastname")
    String lastname;
    @SerializedName("birthdate")
    long birthdate;
    @SerializedName("phone")
    String phone;
    @SerializedName("gender")
    String gender;
    @SerializedName("liveInBarcelona")
    Boolean liveInBarcelona;
    @SerializedName("photo")
    String photo;
    @SerializedName("photoMimeType")
    String photoMimeType;

    public UserRegister(String email, String password, String name, String lastname, long birthdate,
                        String phone, String gender, Boolean liveInBarcelona, String photo,
                        String photoMimeType ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastname = lastname;
        this.birthdate = birthdate;
        this.phone = phone;
        this.gender = gender;
        this.liveInBarcelona = liveInBarcelona;
        this.photo = photo;
        this.photoMimeType = photoMimeType;
    }

    public UserRegister(String alias, String name, String lastname, long birthdate, String email,
                        String phone, String gender, Boolean liveInBarcelona, String photo, String photoMimeType) {
        this.alias = alias;
        this.email = email;
        this.name = name;
        this.lastname = lastname;
        this.birthdate = birthdate;
        this.phone = phone;
        this.gender = gender;
        this.liveInBarcelona = liveInBarcelona;
        this.photo = photo;
        this.photoMimeType = photoMimeType;
    }

    public UserRegister(String name, String lastname, String phone, Boolean liveInBarcelona,
                        long birthdate, String gender) {
        this.name = name;
        this.lastname = lastname;
        this.phone = phone;
        this.liveInBarcelona = liveInBarcelona;
        this.birthdate = birthdate;
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getLiveInBarcelona() {
        return liveInBarcelona;
    }

    public void setLiveInBarcelona(Boolean liveInBarcelona) {
        this.liveInBarcelona = liveInBarcelona;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhotoMimeType() {
        return photoMimeType;
    }

    public void setPhotoMimeType(String photoMimeType) {
        this.photoMimeType = photoMimeType;
    }

    @Override
    public String toString() {
        return "UserRegister{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", alias='" + alias + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", lastname='" + lastname + '\'' +
                ", birthdate=" + birthdate +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", liveInBarcelona=" + liveInBarcelona +
                ", photo='" + photo + '\'' +
                ", photoMimeType='" + photoMimeType + '\'' +
                '}';
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        if (email != null) json.addProperty("email", email);
        if (password != null) json.addProperty("password", password);
        if (alias != null) json.addProperty("alias", alias);
        if (name != null) json.addProperty("name", name);
        if (lastname != null) json.addProperty("lastname", lastname);
        json.addProperty("birthdate", birthdate);
        if (phone != null) json.addProperty("phone", phone);
        if (gender != null) json.addProperty("gender", gender);
        json.addProperty("liveInBarcelona", liveInBarcelona);
        if (username != null) json.addProperty("username",username);
        if (photo != null) json.addProperty("photo",photo);
        if (photoMimeType != null) json.addProperty("photoMimeType",photoMimeType);
        return json;
    }

}