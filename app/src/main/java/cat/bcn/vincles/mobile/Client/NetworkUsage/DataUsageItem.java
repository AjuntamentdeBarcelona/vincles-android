package cat.bcn.vincles.mobile.Client.NetworkUsage;

import com.google.gson.annotations.SerializedName;

public class DataUsageItem {

    public DataUsageItem() {

    }

    public DataUsageItem(String tag, long up, long down) {
        this.tag = tag;

    }

    @SerializedName("callType")
    private String tag;

    @SerializedName("down")
    private long down;

    @SerializedName("up")
    private long up;

    public long getDown() {
        return down;
    }

    public void setDown(long down) {
        this.down = down;
    }

    public long getUp() {
        return up;
    }

    public void setUp(long up) {
        this.up = up;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
