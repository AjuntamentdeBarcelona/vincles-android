package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.ChatMessagesSentResponse;

public class ChatMessagesSentResponseDeserializer implements JsonDeserializer<ChatMessagesSentResponse> {

    @Override
    public ChatMessagesSentResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        ArrayList<Integer> userId = new ArrayList<>();
        ArrayList<Integer> userMessageId = new ArrayList<>();
        ArrayList<Integer> chatId = new ArrayList<>();
        ArrayList<Integer> chatMessageId = new ArrayList<>();

        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.get("communityMessageIdDto").equals(JsonNull.INSTANCE)) {
            for ( JsonElement jsonElement : jsonObject.get("communityMessageIdDto").getAsJsonArray()) {
                JsonObject jo = jsonElement.getAsJsonObject();

                userId.add(jo.get("userToId").getAsInt());
                userMessageId.add(jo.get("messageId").getAsInt());
            }
        }


        jsonObject = json.getAsJsonObject();
        if (!jsonObject.get("communityChatMessageIdDto").equals(JsonNull.INSTANCE)) {
            for ( JsonElement jsonElement : jsonObject.get("communityChatMessageIdDto").getAsJsonArray()) {
                JsonObject jo = jsonElement.getAsJsonObject();

                chatId.add(jo.get("chatId").getAsInt());
                chatMessageId.add(jo.get("chatMessageId").getAsInt());
            }
        }

        return new ChatMessagesSentResponse(userId, userMessageId, chatId, chatMessageId);
    }
}
