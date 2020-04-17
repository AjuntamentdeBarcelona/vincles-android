package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateUserCodeRequest extends BaseRequest implements Callback<JsonObject> {
    private UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GenerateUserCodeRequest(RenewTokenFailed listener) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGenerateUserCode(response.body().get("registerCode").getAsString());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGenerateUserCode(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<JsonObject> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGenerateUserCode(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {
        authenticatedRequest(token);
        userService = retrofit.create(UserService.class);
        Call<JsonObject> call = userService.generateCode(new JsonObject());

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    public interface OnResponse {
        void onResponseGenerateUserCode(String code);

        void onFailureGenerateUserCode(Object error);
    }
}
