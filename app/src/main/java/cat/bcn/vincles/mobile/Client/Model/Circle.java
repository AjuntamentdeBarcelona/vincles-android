package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Circle {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("userVincles")
    @Expose
    private GetUser user;

    public Circle() {

    }

    public int getId() {
        return id;
    }

    public GetUser getUser() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "id=" + id+
                ", user='" + user  +
                '}';
    }

}
