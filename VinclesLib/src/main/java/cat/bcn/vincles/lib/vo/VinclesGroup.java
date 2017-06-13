/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VinclesGroup extends GenericObject {
    public String name;
    public String topic;
    public Long idChat;
    public Long idDynamizerChat;
    public User dynamizer;
    public boolean active;
    public byte[] photo;

    public VinclesGroup() {
        // CAUTION: Must be empty constructor!!!
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("topic", topic);
        json.addProperty("idChat", idChat);
        json.add("dynamizer", dynamizer.toJSON());

        return json;
    }

    public static VinclesGroup fromJSON(JsonObject json) {
        VinclesGroup vinclesGroup = new VinclesGroup();
        vinclesGroup.setId(json.get("id").getAsLong());

        // Control Json null properties
        JsonElement nameElement = json.get("name");
        if (nameElement != null && nameElement.isJsonNull() == false) {
            vinclesGroup.name = nameElement.getAsString();
        }

        JsonElement descriptionElement = json.get("description");
        if (descriptionElement != null && descriptionElement.isJsonNull() == false) {
            vinclesGroup.description = descriptionElement.getAsString();
        }

        JsonElement topicElement = json.get("topic");
        if (topicElement != null && topicElement.isJsonNull() == false) {
            vinclesGroup.topic = topicElement.getAsString();
        }

        JsonElement idChatElement = json.get("idChat");
        if (idChatElement != null && idChatElement.isJsonNull() == false) {
            vinclesGroup.idChat = idChatElement.getAsLong();
        }

        JsonElement dynamizerElement = json.get("dynamizer");
        if (dynamizerElement != null && dynamizerElement.isJsonNull() == false) {
            vinclesGroup.dynamizer = User.fromJSON(dynamizerElement.getAsJsonObject());
        }

        return vinclesGroup;
    }
}