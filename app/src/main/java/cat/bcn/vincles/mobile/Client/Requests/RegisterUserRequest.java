package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;

public class RegisterUserRequest extends BaseRequest implements Callback<UserRegister> {

    UserService userService;
    UserRegister userRegister;
    List<OnResponse> onResponses = new ArrayList<>();

    public RegisterUserRequest (UserRegister userRegister) {
        super(null, BaseRequest.UNAUTHENTICATED_REQUEST);
        this.userRegister = userRegister;
        userService = retrofit.create(UserService.class);
    }

    public void doRequest() {
        Call<UserRegister> call = userService.register(userRegister.toJSON());

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<UserRegister> call, Response<UserRegister> response) {
        Log.d("RegisterUserRequest","onResponseGetAuthenticatedUserDataRequest");
        for (OnResponse r : onResponses) {
            if (response.isSuccessful()) {
              r.onResponseRegisterUserRequest(response.body());
            } else {
                String errorCode = ErrorHandler.parseError(response).getCode();
                Log.d("vld","register response not succ:"+errorCode+" message:"+ErrorHandler.parseError(response).getMessage());
                r.onFailureRegisterUserRequest(errorCode);
            }
        }
    }

    @Override
    public void onFailure(Call<UserRegister> call, Throwable t) {
        Log.d("RegisterUserRequest","onFailureGetAuthenticatedUserDataRequest");
        for (OnResponse r : onResponses) {
            r.onFailureRegisterUserRequest(new Exception(t));
        }
    }

    public void addOnOnResponse(RegisterUserRequest.OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseRegisterUserRequest(UserRegister userRegister);
        void onFailureRegisterUserRequest(Object error);
    }
}
