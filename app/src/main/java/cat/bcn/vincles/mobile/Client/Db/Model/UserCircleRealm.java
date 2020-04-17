package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmObject;

public class UserCircleRealm extends RealmObject {

    private String relationship;
    private CircleRealm circleRealm;

    public UserCircleRealm() {

    }

    public UserCircleRealm(String relationship, CircleRealm circleRealm) {
        this.relationship = relationship;
        this.circleRealm = circleRealm;
    }

    public String getRelationship() {
        return relationship;
    }

    public CircleRealm getCircleRealm() {
        return circleRealm;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setCircleRealm(CircleRealm circleRealm) {
        this.circleRealm = circleRealm;
    }

}
