/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.orm.dsl.Ignore;

import java.util.Objects;

public class PushMessage extends GenericObject {
    @Ignore
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    @Ignore
    public static final String TYPE_NEW_EVENT = "NEW_EVENT";
    @Ignore
    public static final String TYPE_REMEMBER_EVENT = "REMEMBER_EVENT";
    @Ignore
    public static final String TYPE_DELETED_EVENT = "EVENT_DELETED";
    @Ignore
    public static final String TYPE_EVENT_ACCEPTED = "EVENT_ACCEPTED";
    @Ignore
    public static final String TYPE_EVENT_REJECTED = "EVENT_REJECTED";
    @Ignore
    public static final String TYPE_EVENT_UPDATED = "EVENT_UPDATED";
    @Ignore
    public static final String TYPE_INCOMING_CALL = "INCOMING_CALL";
    @Ignore
    public static final String TYPE_LOST_CALL = "LOST_CALL";
    @Ignore
    public static final String TYPE_USER_UPDATED = "USER_UPDATED";
    @Ignore
    public static final String TYPE_USER_LINKED = "USER_LINKED";
    @Ignore
    public static final String TYPE_USER_UNLINKED = "USER_UNLINKED";
    @Ignore
    public static final String TYPE_NEW_CHAT = "NEW_CHAT_MESSAGE";
    @Ignore
    public static final String TYPE_INVITATION_SENT = "INVITATION_SENDED";
    @Ignore
    public static final String TYPE_ADDED_TO_GROUP = "ADDED_TO_GROUP";
    @Ignore
    public static final String TYPE_NEW_USER_GROUP = "NEW_USER_GROUP";

    // NOT PUSH BUT NOTIFICATIONS
    @Ignore
    public static final String TYPE_BATTERY_LOW = "BATTERY_LOW";
    @Ignore
    public static final String TYPE_BATTERY_OKAY = "BATTERY_OKAY";
    @Ignore
    public static final String TYPE_NO_CONNECTION = "NO_CONNECTION";
    @Ignore
    public static final String TYPE_CONNECTION_OKAY = "CONNECTION_OKAY";
    @Ignore
    public static final String TYPE_FREE_SPACE_LOW = "FREE_SPACE_LOW";
    @Ignore
    public static final String TYPE_FREE_SPACE_OKAY = "FREE_SPACE_OKAY";
    @Ignore
    public static final String TYPE_STRENGTH_CONNECTION_LOW = "STRENGTH_CONNECTION_LOW";
    @Ignore
    public static final String TYPE_STRENGTH_CONNECTION_OK = "STRENGTH_CONNECTION_OK";


    @SerializedName("id")
    private Long idPush;
    private Long creationTime;
    private Long idData;
    private Long idExtra = -1l;
    private String type;
    private String info;

    // NOT USED, ONLY FOR SAFETY OR FUTURE REASONS
    private String rawDataJson;

    public Long getIdPush() {
        return idPush;
    }

    public PushMessage setIdPush(Long idPush) {
        this.idPush = idPush;
        return this;
    }

    public Long getIdExtra() {
        return idExtra;
    }

    public void setIdExtra(Long idExtra) {
        this.idExtra = idExtra;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public PushMessage setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getType() {
        return type;
    }

    public PushMessage setType(String type) {
        this.type = type;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public PushMessage setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getRawDataJson() {
        return rawDataJson;
    }

    public PushMessage setRawDataJson(String rawDataJson) {
        this.rawDataJson = rawDataJson;
        return this;
    }

    public Long getIdData() {
        return idData;
    }

    public PushMessage setIdData(Long idData) {
        this.idData = idData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushMessage that = (PushMessage) o;
        return Objects.equals(idPush, that.idPush);
    }

    public static PushMessage fromJSON(JsonObject json) {
        //{
        //    "idUser" : 0,
        //    "idInstallation": 0,
        //    "so" : <"ANDROID" or "IOS">,
        //    "pushToken" : "pushtokeninfo",
        //    "pin" : "PIN"
        //}

        PushMessage pm = new PushMessage();
        pm.setIdPush(json.get("id").getAsLong());
        pm.setCreationTime(json.get("creationTime").getAsLong());
        pm.setType(json.get("type").getAsString());

        JsonElement info = json.get("info");
        if (info != null) {
            pm.setInfo(info.toString());
            // Looks better option to add it into GCMListener side
//            if (info.getAsJsonObject().has("idMessage"))
//                pm.setIdData(info.getAsJsonObject().get("idMessage").getAsLong());
        }

        return pm;
    }
}
