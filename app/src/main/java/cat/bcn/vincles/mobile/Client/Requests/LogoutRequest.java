package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;

import static android.support.v4.content.ContextCompat.startActivity;

public class LogoutRequest extends BaseRequest implements Callback<Void> {

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    String token;

    public LogoutRequest(RenewTokenFailed listener, String token) {
        super(listener, BaseRequest.LOGIN_LOGOUT_REQUEST);
        this.token = token;
        userService = retrofit.create(UserService.class);
    }

    public void doRequest() {
        Call<Void> call = userService.logout(token);

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
    public void onResponse(Call<Void> call, Response<Void> response) {
        for (OnResponse r : onResponses) {
            if (response.isSuccessful()) {
                r.onResponseLogoutRequest();
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                r.onFailureLogoutRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureLogoutRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseLogoutRequest();
        void onFailureLogoutRequest(Object error);
    }
}
