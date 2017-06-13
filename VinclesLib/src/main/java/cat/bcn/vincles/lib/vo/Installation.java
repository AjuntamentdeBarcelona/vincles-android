/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.orm.dsl.Ignore;

import java.util.Arrays;
import java.util.Objects;

public class Installation extends GenericObject {
    @Ignore
    public static final String OS_ANDROID= "ANDROID";
    @Ignore
    public static final String OS_IOS= "IOS";

    private Long idUser;

    private Long idInstallation;

    @SerializedName("so")
    private String operatingSystem;

    private String imei;

    private String pushToken;

    private String pin;

    public Installation() {
        super();
    }

    public Installation(Long idUser, Long idInstallation, String operatingSystem, String imei, String pushToken, String pin) {
        this.idUser = idUser;
        this.idInstallation = idInstallation;
        this.operatingSystem = operatingSystem;
        this.pushToken = pushToken;
        this.imei = imei;
        this.pin = pin;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdInstallation() {
        return idInstallation;
    }

    public void setIdInstallation(Long idInstallation) {
        this.idInstallation = idInstallation;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Installation that = (Installation) o;
        return ((idUser == null) ? (that.idUser == null) : idUser.equals(that.idUser)) &&
                ((idInstallation == null) ? (that.idInstallation == null) : idInstallation.equals(that.idInstallation)) &&
                ((operatingSystem == null) ? (that.operatingSystem == null) : operatingSystem.equals(that.operatingSystem)) &&
                ((pushToken == null) ? (that.pushToken == null) : pushToken.equals(that.pushToken)) &&
                ((imei == null) ? (that.imei == null) : imei.equals(that.imei)) &&
                ((pin == null) ? (that.pin == null) : pin.equals(that.pin));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{idUser, idInstallation, operatingSystem, imei, pushToken, pin});
    }

    public JsonObject toJSON() {
        //{
        //    "idUser" : 0,
        //    "so" : <"ANDROID" or "IOS">,
        //    "pushToken" : "pushtokeninfo"
        //}

        JsonObject json = new JsonObject();
        json.addProperty("idUser", this.idUser);
        json.addProperty("so", this.operatingSystem);
        json.addProperty("pushToken", this.pushToken);
        json.addProperty("imei", this.imei);

        return json;
    }

    public static Installation updateFromJSON(Installation installation, JsonObject json) {
        //{
        //    "idUser" : 0,
        //    "idInstallation": 0,
        //    "so" : <"ANDROID" or "IOS">,
        //    "pushToken" : "pushtokeninfo",
        //    "pin" : "PIN"
        //}

        installation.setIdUser(json.get("idUser").getAsLong());
        installation.setIdInstallation(json.get("id").getAsLong());
        installation.setOperatingSystem(json.get("so").getAsString());
        installation.setPushToken(json.get("pushToken").getAsString());
        installation.setImei(json.get("imei").getAsString());
        installation.setPin(json.get("pin").getAsString());
        installation.save();

        return installation;
    }
}
