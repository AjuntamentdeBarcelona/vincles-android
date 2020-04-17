package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserCircle {
    @SerializedName("relationship")
    @Expose
    private String relationship;
    @SerializedName("circle")
    @Expose
    private Circle circle;

    public UserCircle() {

    }

    public String getRelationship() {
        return relationship;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    @Override
    public String toString() {
        return "UserCircle{" +
                "circle=" + circle+
                ", relationship='" + relationship  +
                '}';
    }

}
