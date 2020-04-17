package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@JsonAdapter(ChatMessageSentResponseDeserializer.class)
public class ChatMessageSentResponse {

    @SerializedName("id")
    @Expose
    int id;

    public ChatMessageSentResponse(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
