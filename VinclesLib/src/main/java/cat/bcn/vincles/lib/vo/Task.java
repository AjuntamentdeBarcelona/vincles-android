/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orm.dsl.Ignore;

import java.util.Date;

public class Task extends GenericObject {
    public Network network;
    private Date date;
    public int duration;
    public int state;
    public String datetime;
    public Long calendarId;
    public Long androidCalendarId;
    public User owner;

    @Ignore
    public static final int STATE_PENDING = 0;
    @Ignore
    public static final int STATE_ACCEPTED = 1;
    @Ignore
    public static final int STATE_REJECTED = 2;
    @Ignore
    public static final int STATE_OCCUPIED = 3;

    @Ignore
    public static final String STATE_PENDING_LABEL = "PENDING";
    @Ignore
    public static final String STATE_ACCEPTED_LABEL = "ACCEPTED";
    @Ignore
    public static final String STATE_REJECTED_LABEL = "REJECTED";

    public void Task() {
        // CAUTION: Must be empty constructor!!!
    }

    public Date getDate() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        this.datetime = String.valueOf(date.getTime());
    }

    public JsonObject toJSON() {
        JsonObject json = toJSONNoOnwer();
        if (owner != null) {
            JsonElement userCreatorElement = owner.toJSON();
            json.add("userCreator", userCreatorElement);
        }

        return json;
    }

    public JsonObject toJSONNoOnwer() {
        JsonObject json = new JsonObject();

        json.addProperty("id", getId());
        json.addProperty("date", getDate().getTime());
        json.addProperty("duration", duration);
        json.addProperty("description", description);

        return  json;
    }

    public static Task fromJSON(JsonObject json) {
        Task it = new Task();

        // Control Json null properties
        JsonElement idElement = json.get("id");
        if (idElement != null && idElement.isJsonNull() == false) {
            it.setId(idElement.getAsLong());
        }
        JsonElement dateElement = json.get("date");
        if (dateElement != null && dateElement.isJsonNull() == false) {
            it.setDate(new Date(dateElement.getAsLong()));
        }
        JsonElement durationElement = json.get("duration");
        if (durationElement != null && durationElement.isJsonNull() == false) {
            it.duration = durationElement.getAsInt();
        }
        JsonElement descriptionElement = json.get("description");
        if (descriptionElement != null && descriptionElement.isJsonNull() == false) {
            it.description = descriptionElement.getAsString();
        }
        JsonElement stateElement = json.get("state");
        if (stateElement != null && stateElement.isJsonNull() == false) {
            it.state = setState(stateElement.getAsString());
        }
        JsonElement calendarIdElement = json.get("calendarId");
        if (calendarIdElement != null && calendarIdElement.isJsonNull() == false) {
            it.calendarId = calendarIdElement.getAsLong();
        }
        JsonElement ownerElement = json.get("userCreator");
        if (ownerElement != null && ownerElement.isJsonNull() == false) {
            it.owner = User.fromJSON(ownerElement.getAsJsonObject());
        }

        return it;
    }

    private String getState() {
        String result = "";
        switch (state) {
            case STATE_PENDING:
                result = STATE_PENDING_LABEL;
                break;
            case STATE_ACCEPTED:
                result = STATE_ACCEPTED_LABEL;
                break;
            case STATE_REJECTED:
                result = STATE_REJECTED_LABEL;
                break;
        }

        return result;
    }

    private static int setState(String value) {
        int result = 0;
        switch (value) {
            case STATE_PENDING_LABEL:
                result = STATE_PENDING;
                break;
            case STATE_ACCEPTED_LABEL:
                result = STATE_ACCEPTED;
                break;
            case STATE_REJECTED_LABEL:
                result = STATE_REJECTED;
                break;
        }

        return result;
    }
}
