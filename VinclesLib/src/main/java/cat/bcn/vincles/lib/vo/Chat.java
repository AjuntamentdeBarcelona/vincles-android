/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orm.dsl.Ignore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends Communication {
    @Ignore
    public int resStatus = 2;  // 0: Downloading, 1: Downloaded, 2:Default

    public void Chat() {
        // CAUTION: Must be empty constructor!!!
    }

    @Override
    public List<Resource> getResources() {
        String id = String.valueOf(getId());
        List<Resource> items = Resource.find(Resource.class, "chat = ?", id); // Local resources
        if (resourceTempList == null) resourceTempList = new ArrayList<>();

        for (Resource it : resourceTempList) {
            if (!items.contains(it)) {
                items.add(it);
            }
        }
        return items;
    }

    public static Chat fromJSON(JsonObject json) {
        Chat it = new Chat();
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
        if (it.idContent != null) {
            Resource re = new Resource();
            re.setId(it.idContent);
            it.resourceTempList.add(re);
        }

        return it;
    }
}