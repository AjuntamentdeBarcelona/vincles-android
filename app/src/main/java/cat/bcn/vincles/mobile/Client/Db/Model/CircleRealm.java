package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmObject;

public class CircleRealm extends RealmObject {

    private int id;
    private int userId;

    public CircleRealm() {

    }

    public CircleRealm(int id, int userId) {
        this.id = id;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
