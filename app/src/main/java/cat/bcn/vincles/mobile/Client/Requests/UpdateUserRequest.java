package cat.bcn.vincles.mobile.Client.Requests;


import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserRequest extends BaseRequest implements Callback<JSONObject> {

    UserService userService;
    UserRegister userRegister;
    List<OnResponse> onResponses = new ArrayList<>();

    public UpdateUserRequest(RenewTokenFailed listener, UserRegister userRegister) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.userRegister = userRegister;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<JSONObject> call = userService.updateUser(userRegister.toJSON());

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseUpdateUserRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureUpdateUserRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<JSONObject> call, Throwable t) {
        Log.d("","");
        for (OnResponse r : onResponses) {
            r.onFailureUpdateUserRequest(new Exception(t));
        }
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    public interface OnResponse {
        void onResponseUpdateUserRequest(JSONObject userRegister);
        void onFailureUpdateUserRequest(Object error);
    }

}
