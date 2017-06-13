/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message extends Communication {

    public void Message() {
        // CAUTION: Must be empty constructor!!!
    }

    public List<Resource> getResources() {
        String id = String.valueOf(getId());
        List<Resource> items = Resource.find(Resource.class, "message = ?", id); // Local resources
        if (resourceTempList == null) resourceTempList = new ArrayList<>();

        for (Resource it : resourceTempList) {
            if (!items.contains(it)) {
                items.add(it);
            }
        }
        return items;
    }

    public static Message fromJSON(JsonObject json) {
        Message it = new Message();
        it.setId(json.get("id").getAsLong());

        // Control Json null properties
        JsonElement idUserFromElement = json.get("idUserFrom");
        if (idUserFromElement != null && idUserFromElement.isJsonNull() == false) {
            it.idUserFrom = idUserFromElement.getAsLong();
        }
        JsonElement idUserToElement = json.get("idUserTo");
        if (idUserToElement != null && idUserToElement.isJsonNull() == false) {
            it.idUserTo = idUserToElement.getAsLong();
        }
        JsonElement watchedElement = json.get("watched");
        if (watchedElement != null && watchedElement.isJsonNull() == false) {
            it.watched = watchedElement.getAsBoolean();
        }

        JsonElement textElement = json.get("text");
        if (textElement != null && textElement.isJsonNull() == false) {
            it.text = textElement.getAsString();
        }
        JsonElement metadataTipusElement = json.get("metadataTipus");
        if (metadataTipusElement != null && metadataTipusElement.isJsonNull() == false) {
            it.metadataTipus = metadataTipusElement.getAsString();
        }
        JsonElement sendTimeElement = json.get("sendTime");
        if (sendTimeElement != null && sendTimeElement.isJsonNull() == false) {
            it.sendTime = new Date(sendTimeElement.getAsLong());
        }
        JsonElement idUserSenderElement = json.get("idUserSender");
        if (idUserSenderElement != null && idUserSenderElement.isJsonNull() == false) {
            it.idUserFrom = idUserSenderElement.getAsLong();
        }
        JsonElement idContentElement = json.get("idContent");
        if (idContentElement != null && idContentElement.isJsonNull() == false) {
            it.idContent = idContentElement.getAsLong();
        }
        JsonElement idChatElement = json.get("idChat");
        if (idChatElement != null && idChatElement.isJsonNull() == false) {
            it.idChat = idChatElement.getAsLong();
        }

        it.resourceTempList = new ArrayList<Resource>();
        JsonElement arrayElement = json.get("idAdjuntContents");
        if (arrayElement != null && arrayElement.isJsonNull() == false) {
            for (JsonElement i : arrayElement.getAsJsonArray()) {
                Resource re = new Resource();
                re.setId(i.getAsLong());
                it.resourceTempList.add(re);
            }
        }

        return it;
    }
}