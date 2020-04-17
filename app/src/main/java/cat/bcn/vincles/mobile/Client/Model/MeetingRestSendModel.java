package cat.bcn.vincles.mobile.Client.Model;

public class MeetingRestSendModel {

    private long date;
    private int duration;
    private String description;
    private int[] inviteTo;

    public MeetingRestSendModel(long date, int duration, String description, int[] inviteTo) {
        this.date = date;
        this.duration = duration;
        this.description = description;
        this.inviteTo = inviteTo;
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

    public int[] getInviteTo() {
        return inviteTo;
    }

    public void setInviteTo(int[] inviteTo) {
        this.inviteTo = inviteTo;
    }
}
