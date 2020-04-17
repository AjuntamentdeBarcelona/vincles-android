package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecoverPasswordRequest extends BaseRequest implements Callback<ResponseBody> {
    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    JsonObject jBody;

    public RecoverPasswordRequest(String email) {
        super(null, BaseRequest.UNAUTHENTICATED_REQUEST);
        userService = retrofit.create(UserService.class);
        jBody = new JsonObject();
        jBody.addProperty("username", email);
    }

    public void doRequest() {
        Call<ResponseBody> call = userService.recoverPassword(jBody);

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
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        for (OnResponse r : onResponses) {
            if (response.isSuccessful()) {
                r.onResponseRecoverPasswordRequest(response.body());
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                r.onFailureRecoverPasswordRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureRecoverPasswordRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseRecoverPasswordRequest(ResponseBody responseBody);

        void onFailureRecoverPasswordRequest(Object error);
    }
}
