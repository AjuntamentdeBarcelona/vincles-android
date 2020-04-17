package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Services.UserService;

public class LoginRequest extends BaseRequest implements Callback<TokenFromLogin> {

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    private static final String USER_PREFIX = "@vincles-bcn.cat";

    String username, password;

    public LoginRequest(String username, String password) {
        super(null, BaseRequest.LOGIN_LOGOUT_REQUEST);
        this.username = username;
        this.password = password;
        userService = retrofit.create(UserService.class);
    }

    public void doRequest() {
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setRenewTokenFailed(false);
        Call<TokenFromLogin> call = userService.login("password",username + USER_PREFIX,password);

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<TokenFromLogin> call, Response<TokenFromLogin> response) {
        for (OnResponse r : onResponses) {
            UserPreferences userPreferences = new UserPreferences();
            if (response.isSuccessful()) {
                userPreferences.setLoginDataDownloaded(true);
                r.onResponseLoginRequest(response.body());
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                r.onFailureLoginRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<TokenFromLogin> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureLoginRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseLoginRequest(TokenFromLogin tokenFromLogin);
        void onFailureLoginRequest(Object error);
    }

}
