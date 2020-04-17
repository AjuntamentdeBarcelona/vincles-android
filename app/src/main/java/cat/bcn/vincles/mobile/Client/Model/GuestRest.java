package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GuestRest {

    @SerializedName("userInfo")
    @Expose
    private GetUser user;
    @SerializedName("state")
    @Expose
    private String state;

    public GuestRest() {
    }

    public GetUser getUser() {
        return user;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
