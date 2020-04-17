package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserGroup {
    @SerializedName("idDynamizerSharedChat")
    @Expose
    private int idDynamizerChat;
    @SerializedName("group")
    @Expose
    private Group group;

    public UserGroup() {

    }

    public int getIdDynamizerChat() {
        return idDynamizerChat;
    }

    public Group getGroup() {
        return group;
    }


    public void setIdDynamizerChat(int idDynamizerChat) {
        this.idDynamizerChat = idDynamizerChat;
    }

    public void setGroup(Group group) {
        this.group = group;
    }


    @Override
    public String toString() {
        return "CircleUser{" +
                "idDynamizerChat=" + idDynamizerChat+
                ", group='" + group  +
                '}';
    }

}
