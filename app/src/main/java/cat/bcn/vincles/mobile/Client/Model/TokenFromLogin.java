package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class TokenFromLogin {

    @SerializedName("scope")
    String scope;

    @SerializedName("token_type")
    String tokenType;

    @SerializedName("expires_in")
    int expiresIn;

    @SerializedName("refresh_token")
    String refreshToken;

    @SerializedName("access_token")
    String accessToken;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
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

    @Override
    public String toString() {
        return "TokenFromLogin{" +
                "scope='" + scope + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn='" + expiresIn + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("scope", scope);
        json.addProperty("token_type", tokenType);
        json.addProperty("expires_in", expiresIn);
        json.addProperty("refresh_token", refreshToken);
        json.addProperty("access_token", accessToken);
        return json;
    }

}
