package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmObject;

public class UserGroupRealm extends RealmObject {

    private int idDynamizerChat;
    private int idGroup;

    public UserGroupRealm() {

    }

    public UserGroupRealm(int idDynamizerChat, int idGroup) {
        this.idDynamizerChat = idDynamizerChat;
        this.idGroup = idGroup;
    }

    public int getIdDynamizerChat() {
        return idDynamizerChat;
    }

    public int getIdGroup() {
        return idGroup;
    }


    public void setIdDynamizerChat(int idDynamizerChat) {
        this.idDynamizerChat = idDynamizerChat;
    }

    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }

}
