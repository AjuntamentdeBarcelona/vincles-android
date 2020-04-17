package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;

public class NotificationRestDeserializer implements JsonDeserializer<NotificationRest> {

    @Override
    public NotificationRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String type = jsonObject.get("type").getAsString();
        long creationTime = jsonObject.get("creationTime").getAsLong();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        GetUser getUser = gson.fromJson(jsonObject.get("user"), GetUser.class);

        JsonObject info = jsonObject.getAsJsonObject("info");
        int idUser = -1;
        int idMessage = -1;
        int idChat = -1;
        int idChatMessage = -1;
        int idGroup = -1;
        int idMeeting = -1;
        String idRoom = null;
        String code = null;
        int idHost = -1;
        int idGalleryContent = -1;
        if (info != null) {
            idUser = info.get("idUser") != null ? info.get("idUser").getAsInt() : -1;
            idMessage = info.get("idMessage") != null ? info.get("idMessage").getAsInt() : -1;
            idChat = info.get("idChat") != null ? info.get("idChat").getAsInt() : -1;
            idChatMessage = info.get("idChatMessage") != null ? info.get("idChatMessage").getAsInt() : -1;
            idGroup = info.get("idGroup") != null ? info.get("idGroup").getAsInt() : -1;
            idMeeting = info.get("idMeeting") != null ? info.get("idMeeting").getAsInt() : -1;
            idRoom = info.get("idRoom") != null ? info.get("idRoom").getAsString() : null;
            code = info.get("code") != null ? info.get("code").getAsString() : null;
            idHost = info.get("idHost") != null ? info.get("idHost").getAsInt() : -1;
            idGalleryContent = info.get("idGalleryContent") != null ? info.get("idGalleryContent").getAsInt() : -1;
        }

        return new NotificationRest(id, type, creationTime, false,
                idHost != -1 ? idHost : idUser, idMessage, idChat, idChatMessage, idGroup,
                idMeeting, idRoom, code, idGalleryContent);
    }

}
