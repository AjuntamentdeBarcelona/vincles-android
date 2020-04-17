package cat.bcn.vincles.mobile.Client.Db.Model;

import com.google.gson.annotations.SerializedName;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import android.util.Log;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class GalleryContentRealm extends RealmObject {

    @PrimaryKey @Index
    private int id;
    private int idContent;
    private String mimeType;
    private int userId;
    private long inclusionTime;
    private String path = "";
    private String thumbnailPath = "";
    @Ignore
    private String tag;
    @SerializedName("userCreator")
    private GetUser userCreator;


    public GalleryContentRealm() {

    }

    public GalleryContentRealm(int id, int idContent, String mimeType, int userId, long inclusionTime) {
        this.id = id;
        this.idContent = idContent;
        this.mimeType = mimeType;
        this.userId = userId;
        this.inclusionTime = inclusionTime;
    }

    public GalleryContentRealm(int id, int idContent, String mimeType, GetUser getUser,
                          long inclusionTime, String tag) {
        this.id = id;
        this.idContent = idContent;
        this.mimeType = mimeType;
        this.userCreator = getUser;
        this.inclusionTime = inclusionTime;
        this.tag = tag;
    }

    public GalleryContentRealm(int id, String mimeType, GetUser user, int inclusionTime) {
        this.id = id;
        this.mimeType = mimeType;
        this.userCreator = user;
        this.inclusionTime = inclusionTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public GetUser getUserCreator() {
        return userCreator;
    }

    public void setUserCreator(GetUser userCreator) {
        this.userCreator = userCreator;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getInclusionTime() {
        return inclusionTime;
    }

    public void setInclusionTime(long inclusionTime) {
        this.inclusionTime = inclusionTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIdContent() {
        return idContent;
    }

    public void setIdContent(int idContent) {
        this.idContent = idContent;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
