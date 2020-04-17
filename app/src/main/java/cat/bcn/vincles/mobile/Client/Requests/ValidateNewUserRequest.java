package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValidateNewUserRequest extends BaseRequest implements Callback<JsonObject> {
    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    JsonObject jBody;


    public ValidateNewUserRequest(String email, String code) {
        super(null, BaseRequest.UNAUTHENTICATED_REQUEST);
        userService = retrofit.create(UserService.class);
        jBody = new JsonObject();
        jBody.addProperty("email", email);
        jBody.addProperty("code", code);
    }

    public void doRequest() {
        Call<JsonObject> call = userService.validateUser(jBody);

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
    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        for (OnResponse r : onResponses) {
            if (response.isSuccessful()) {
                r.onResponseValidateNewUserRequest(response.body());
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                r.onFailureValidateNewUserRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<JsonObject> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureValidateNewUserRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseValidateNewUserRequest(JsonObject responseBody);
        void onFailureValidateNewUserRequest(Object error);
    }
}
