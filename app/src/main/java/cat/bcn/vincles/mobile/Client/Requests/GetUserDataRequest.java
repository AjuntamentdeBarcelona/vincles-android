package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Errors.VinclesError;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserDataRequest extends BaseRequest implements Callback<GetUser> {

    UserService userService;
    String userId;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserDataRequest(String userId) {
        super(null, BaseRequest.AUTHENTICATED_REQUEST);

        this.userId = userId;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<GetUser> call = userService.getUserInfo(userId);

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
        //Log.d("VIN-566", "GetUserDataRequest.onResponse(): START");
        //Log.d("VIN-566", String.format("GetUserDataRequest.onResponse(): code %d", response.code()));
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    //Log.d("VIN-566", "GetUserDataRequest.onResponse(): successful");
                    r.onResponseGetUserDataRequest(response.body());
                } else {
                    VinclesError vinclesError = ErrorHandler.parseError(response);
                    //Log.d("VIN-566", String.format("GetUserDataRequest.onResponse(): error %s, exec onFailureGetUserDataRequest", vinclesError.getCode()));
                    r.onFailureGetUserDataRequest(vinclesError);
                }
            }
        }
        //Log.d("VIN-566", "GetUserDataRequest.onResponse(): END");
    }

    @Override
    public void onFailure(Call<GetUser> call, Throwable t) {
        //Log.d("VIN-566", "GetUserDataRequest.onFailure(): START");
        for (OnResponse r : onResponses) {
            //Log.d("VIN-566", String.format("GetUserDataRequest.onFailure(): exception %s, exec onFailureGetUserDataRequest", t.getMessage()));
            r.onFailureGetUserDataRequest(ErrorHandler.parseError(t));
        }
        //Log.d("VIN-566", "GetUserDataRequest.onFailure(): END");
    }


    public interface OnResponse {
        void onResponseGetUserDataRequest(GetUser user);
        void onFailureGetUserDataRequest(VinclesError error);
    }

}
