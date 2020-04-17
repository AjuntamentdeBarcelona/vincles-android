package cat.bcn.vincles.mobile.Client.NetworkUsage;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DataUsage {

    @SerializedName("consumeDate")
    private long date;
    @SerializedName("userId")
    private int userId;
    @SerializedName("details")
    private ArrayList<DataUsageItem> dataUsageItems = new ArrayList<>();


    transient private String dataUsageItemJson;


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public ArrayList<DataUsageItem> getDataUsageItems() {
        return dataUsageItems;
    }

    public void setDataUsageItems(ArrayList<DataUsageItem> dataUsageItems) {
        this.dataUsageItems = dataUsageItems;
    }

    public String getDataUsageItemJson() {
        return dataUsageItemJson;
    }

    public void setDataUsageItemJson(String dataUsageItemJson) {
        this.dataUsageItemJson = dataUsageItemJson;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
