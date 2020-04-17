package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChangePasswordResponseModel;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordRequest extends BaseRequest implements Callback<ChangePasswordResponseModel> {
    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    JsonObject jBody;
    String newPassword;
    String oldPassword;

    public ChangePasswordRequest(RenewTokenFailed listener, String newPassword, String oldPassword) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
        jBody = new JsonObject();
        jBody.addProperty("currentPassword", oldPassword);
        jBody.addProperty("newPassword", newPassword);
        Log.d("chngpwd","Request body:"+jBody);
    }

    @Override
    public void doRequest(String accessToken) {
        Log.d("chngpwd","Do request, accesToken:"+accessToken);
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<ChangePasswordResponseModel> call = userService.changePassword(jBody);

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
    public void onResponse(Call<ChangePasswordResponseModel> call, Response<ChangePasswordResponseModel> response) {
        Log.d("chngpwd","Response body:"+response.body());
        for (OnResponse r : onResponses) {
            Log.d("pwdchg","password change onResponse success:"+response.isSuccessful());
            if (response.isSuccessful()) {
                ChangePasswordResponseModel responseModel = response.body();
                UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());
                userPreferences.setExpiresIn(responseModel.getSignInInfo().getExpiresIn());
                userPreferences.setRefreshToken(responseModel.getSignInInfo().getRefreshToken());
                userPreferences.setAccessToken(responseModel.getSignInInfo().getAccessToken());

                r.onResponseChangePasswordRequest(responseModel);
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                r.onFailureChangePasswordRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<ChangePasswordResponseModel> call, Throwable t) {
        Log.d("chngpwd","Response ERROR:"+t);
        for (OnResponse r : onResponses) {
            r.onFailureChangePasswordRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseChangePasswordRequest(ChangePasswordResponseModel responseBody);

        void onFailureChangePasswordRequest(Object error);
    }
}
