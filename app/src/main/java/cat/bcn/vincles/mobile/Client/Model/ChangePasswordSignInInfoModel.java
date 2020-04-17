package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordSignInInfoModel {

    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("access_token")
    private String accessToken;

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
