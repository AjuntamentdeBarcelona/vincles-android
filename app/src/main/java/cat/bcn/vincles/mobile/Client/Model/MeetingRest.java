package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class MeetingRest {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("date")
    @Expose
    private long date;
    @SerializedName("duration")
    @Expose
    private int duration;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("hostInfo")
    @Expose
    private GetUser host;
    @SerializedName("guests")
    @Expose
    private ArrayList<GuestRest> guests = new ArrayList<>();


    public MeetingRest() {
    }

    public MeetingRest(int id, long date, int duration, String description, GetUser host, ArrayList<GuestRest> guests) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.description = description;
        this.host = host;
        this.guests = guests;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GetUser getHost() {
        return host;
    }

    public void setHost(GetUser host) {
        this.host = host;
    }

    public ArrayList<GuestRest> getGuests() {
        return guests;
    }

    public void setGuests(ArrayList<GuestRest> guests) {
        this.guests = guests;
    }
}
