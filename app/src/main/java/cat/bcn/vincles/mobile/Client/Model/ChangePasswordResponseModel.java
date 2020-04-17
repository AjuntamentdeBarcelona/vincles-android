package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordResponseModel {

    @SerializedName("newPassword")
    private String newPassword;
    @SerializedName("signInInfo")
    private ChangePasswordSignInInfoModel signInInfo;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public ChangePasswordSignInInfoModel getSignInInfo() {
        return signInInfo;
    }

    public void setSignInInfo(ChangePasswordSignInInfoModel signInInfo) {
        this.signInInfo = signInInfo;
    }

}
