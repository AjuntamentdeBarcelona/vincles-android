package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.SerializedName;

public class ServerTime {

    @SerializedName("currentTime")
    private long currentTime;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
