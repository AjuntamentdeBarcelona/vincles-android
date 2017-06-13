/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orm.dsl.Ignore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Communication extends GenericObject {
    public Long idUserFrom;
    public Long idUserTo;
    public boolean watched;
    public String text;
    public Date sendTime;
    public String metadataTipus;
    public Long idContent;
    public Long idChat;

    // CAUTION: Override in subclass!!!
    public List<Resource> getResources() {
        return new ArrayList<>();
    }

    @Ignore
    public User userFrom;
    @Ignore
    public List<Resource> resourceTempList;
    @Ignore
    public VinclesGroup group;

    public void Message() {
        // CAUTION: Must be empty constructor!!!
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("idUserFrom", idUserFrom);
        json.addProperty("idUserTo", idUserTo);
        json.addProperty("text", text);
        json.addProperty("metadataTipus", metadataTipus);
        json.addProperty("idUserSender", idUserFrom);
        json.addProperty("idContent", idContent);
        json.addProperty("idChat", idChat);

        JsonArray idAdjuntContentsArray = new JsonArray();
        if (resourceTempList != null && resourceTempList.size()>0) {
            // Add first resource to chat idContent
            Resource item = resourceTempList.get(0);
            json.addProperty("idContent", item.getId());

            for (Resource it : resourceTempList) {
                idAdjuntContentsArray.add(it.getId());
            }
            json.add("idAdjuntContents", idAdjuntContentsArray);
        }

        return json;
    }

    // Get principal/unique Resource
    public Resource getCurrentResource() {
        Resource result = new Resource();
        result.filename = "";
        if (getResources().size() > 0) {
            result = getResources().get(0);
        }
        return result;
    }
}