package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CircleUser {

    @SerializedName("relationship")
    @Expose
    private String relationship;
    @SerializedName("user")
    @Expose
    private GetUser user;

    public CircleUser() {

    }

    public String getRelationship() {
        return relationship;
    }

    public GetUser getUser() {
        return user;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "CircleUser{" +
                "relationship=" + relationship +
                ", user='" + user +
                '}';
    }

}
