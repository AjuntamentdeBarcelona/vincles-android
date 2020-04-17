package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessagesSentResponseDeserializer;

//Dejamos el serializer porque en este caso nos simplifica el modelo
@JsonAdapter(ChatMessagesSentResponseDeserializer.class)
public class ChatMessagesSentResponse {

    private ArrayList<Integer> userId;
    private ArrayList<Integer> userMessageId;
    private ArrayList<Integer> chatId;
    private ArrayList<Integer> chatMessageId;

    public ChatMessagesSentResponse(ArrayList<Integer> userId, ArrayList<Integer> userMessageId, ArrayList<Integer> chatId, ArrayList<Integer> chatMessageId) {
        this.userId = userId;
        this.userMessageId = userMessageId;
        this.chatId = chatId;
        this.chatMessageId = chatMessageId;
    }

    public ArrayList<Integer> getUserId() {
        return userId;
    }

    public ArrayList<Integer> getUserMessageId() {
        return userMessageId;
    }

    public ArrayList<Integer> getChatId() {
        return chatId;
    }

    public ArrayList<Integer> getChatMessageId() {
        return chatMessageId;
    }
}
