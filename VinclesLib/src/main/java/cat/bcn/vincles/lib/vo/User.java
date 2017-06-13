/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orm.dsl.Ignore;

import java.util.Date;

import cat.bcn.vincles.lib.util.Security;
import cat.bcn.vincles.lib.util.VinclesConstants;

public class User extends GenericObject {
    private static final String TAG = "User";

    public String username = "";
    public String password = "";
    public byte[] cipher;
    public String name = "";
    public String alias = "";
    public String lastname = "";
    public String email = "";
    public String phone = "";
    public boolean liveInBarcelona = true;
    public boolean gender = false; // false: Male | true: Female
    public Date birthdate = new Date();
    public Long idCalendar;
    public Long idContentPhoto;
    public String photoMimeType;
    public String imageName;
    public boolean active = false;
    public boolean isDynamizer = false;
    public boolean isUserVincles = false;

    @Ignore
    public String relationship;
    @Ignore
    public String registerCode;
    @Ignore
    public int usrImgStatus = 2;  // 0: Downloading, 1: Downloaded, 2:Default

    public User() {
        // CAUTION: Must be empty constructor!!!
    }

    public JsonObject toJSON() {
        return toJSON(false, "");
    }

    public JsonObject toJSON(String tempPassword) {
        return toJSON(true, tempPassword);
    }

    public JsonObject toJSON(boolean withPassword, String tempPassword) {
        JsonObject json = new JsonObject();
        if (!withPassword) json.addProperty("id", getId());
        json.addProperty("name", name);
        if (alias.equals("")) {
            alias = name;
        }
        json.addProperty("alias", alias);
        json.addProperty("lastname", lastname);
        json.addProperty("email", email);
        json.addProperty("phone", phone);
        json.addProperty("gender", getGender());
        json.addProperty("liveInBarcelona", liveInBarcelona);
        json.addProperty("birthdate", birthdate.getTime());
        if (withPassword) {
            if (tempPassword.length() > 0)
                json.addProperty("password", tempPassword);

            else {
                try {
                    Security sec = new Security();
                    sec.loadPlainAESKey(sec.md5(getId().toString()));
                    json.addProperty("password", sec.AESdecrypt(cipher));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else json.addProperty("idCalendar", idCalendar);

        return json;
    }

    // CAUTION: Retrofit standar serialization conflits with 'id' mapping
    public static User fromJSON(JsonObject json) {
        User user = new User();
        user.setId(json.get("id").getAsLong());

        // Control Json null properties
        JsonElement usernameElement = json.get("username");
        if (usernameElement != null && usernameElement.isJsonNull() == false) {
            user.username = usernameElement.getAsString();
        }
        JsonElement passwordElement = json.get("password");
        if (passwordElement != null && passwordElement.isJsonNull() == false) {
            try {
                Security sec = new Security();
                sec.loadPlainAESKey(sec.md5(user.getId().toString()));
                user.password = null;
                user.cipher = sec.AESencrypt(passwordElement.getAsString());
            } catch (Exception e) { e.printStackTrace(); }
        }
        JsonElement aliasElement = json.get("alias");
        if (aliasElement != null && aliasElement.isJsonNull() == false) {
            user.alias = aliasElement.getAsString();
        }
        JsonElement nameElement = json.get("name");
        if (nameElement != null && nameElement.isJsonNull() == false) {
            user.name = nameElement.getAsString();
        }
        JsonElement lastnameElement = json.get("lastname");
        if (lastnameElement != null && lastnameElement.isJsonNull() == false) {
            user.lastname = lastnameElement.getAsString();
        }
        JsonElement photoElement = json.get("photo");
        if (photoElement != null && photoElement.isJsonNull() == false && photoElement.isJsonObject()) {
            JsonObject jsonPhoto = photoElement.getAsJsonObject();

            JsonElement photoMimeTypeElement = jsonPhoto.get("photoMimeType");
            if (photoMimeTypeElement != null && photoMimeTypeElement.isJsonNull() == false) {
                user.photoMimeType = photoMimeTypeElement.getAsString();
            }
            JsonElement idContentElement = jsonPhoto.get("idContent");
            if (idContentElement != null && idContentElement.isJsonNull() == false) {
                user.idContentPhoto = idContentElement.getAsLong();
                if (user.idContentPhoto == null) {
                    Log.w(TAG, user.alias + " has idContentPhoto null!");
                }
            }
        }
        JsonElement emailElement = json.get("email");
        if (emailElement != null && emailElement.isJsonNull() == false) {
            user.email = emailElement.getAsString();
        }
        JsonElement phoneElement = json.get("phone");
        if (phoneElement != null && phoneElement.isJsonNull() == false) {
            user.phone = phoneElement.getAsString();
        }
        JsonElement genderElement = json.get("gender");
        if (genderElement != null && genderElement.isJsonNull() == false) {
            user.gender = getJSONGender(genderElement.getAsString());
        }
        JsonElement liveInBarcelonaElement = json.get("liveInBarcelona");
        if (liveInBarcelonaElement != null && liveInBarcelonaElement.isJsonNull() == false) {
            user.liveInBarcelona = liveInBarcelonaElement.getAsBoolean();
        }
        JsonElement active = json.get("active");
        if (active != null && active.isJsonNull() == false) {
            user.active = active.getAsBoolean();
        }
        JsonElement birthdateElement = json.get("birthdate");
        if (birthdateElement != null && birthdateElement.isJsonNull() == false) {
            user.birthdate = new Date(birthdateElement.getAsLong());
        }
        JsonElement idCalendarElement = json.get("idCalendar");
        if (idCalendarElement != null && idCalendarElement.isJsonNull() == false) {
            user.idCalendar = idCalendarElement.getAsLong();
        }
        JsonElement idContentPhotoElement = json.get("idContentPhoto");
        if (idContentPhotoElement != null && idContentPhotoElement.isJsonNull() == false) {
            user.idContentPhoto = idContentPhotoElement.getAsLong();
        }

        return user;
    }

    private String getGender() {
        if (gender == false) {
            return VinclesConstants.GENDER_MALE;
        } else {
            return VinclesConstants.GENDER_FEMALE;
        }
    }

    private static boolean getJSONGender(String value) {
        boolean result = false;
        if (value.equals(VinclesConstants.GENDER_FEMALE)) {
            result = true;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = name;
        if (lastname != null) {
            result += " " + lastname;
        }
        return result;
    }
}
