package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAuthenticatedUserDataRequest extends BaseRequest implements Callback<GetUser> {

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetAuthenticatedUserDataRequest() {
        super(null, BaseRequest.AUTHENTICATED_REQUEST);
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        //userService = retrofit.create(UserService.class);
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<GetUser> call = userService.getMyUserInfo();

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
    public void onResponse(Call<GetUser> call, Response<GetUser> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetAuthenticatedUserDataRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetAuthenticatedUserDataRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GetUser> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetAuthenticatedUserDataRequest(new Exception(t));
        }
    }


    public interface OnResponse {
        void onResponseGetAuthenticatedUserDataRequest(GetUser userRegister);
        void onFailureGetAuthenticatedUserDataRequest(Object error);
    }

}
