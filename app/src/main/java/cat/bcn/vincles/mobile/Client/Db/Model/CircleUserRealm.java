package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmObject;

public class CircleUserRealm extends RealmObject {

    private String relationship;
    private int userId;

    public CircleUserRealm() {

    }

    public CircleUserRealm(String relationship, int userId) {
        this.relationship = relationship;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

}
