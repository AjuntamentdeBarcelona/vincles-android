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

public class GetPublicUserDataRequest extends BaseRequest implements Callback<GetUser> {

    UserService userService;
    String userId;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetPublicUserDataRequest(String userId) {
        super(null, BaseRequest.AUTHENTICATED_REQUEST);

        this.userId = userId;
    }

    @Override
    public void doRequest(String accessToken) {
        //Log.d("VIN-566", "GetPublicUserDataRequest.doRequest(): START");
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<GetUser> call = userService.getPublicUserInfo(userId);

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
        //Log.d("VIN-566", "GetPublicUserDataRequest.doRequest(): END");
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<GetUser> call, Response<GetUser> response) {
        //Log.d("VIN-566", "GetPublicUserDataRequest.onResponse(): START");
        //Log.d("VIN-566", String.format("GetPublicUserDataRequest.onResponse(): code %d", response.code()));
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    //Log.d("VIN-566", "GetPublicUserDataRequest.onResponse(): successful");
                    r.onResponseGetPublicUserDataRequest(response.body());
                } else {
                    VinclesError vinclesError = ErrorHandler.parseError(response);
                    //Log.d("VIN-566", String.format("GetPublicUserDataRequest.onResponse(): error %s, exec onFailureGetPublicUserDataRequest", vinclesError.getCode()));
                    r.onFailureGetPublicUserDataRequest(vinclesError);
                }
            }
        }
        //Log.d("VIN-566", "GetPublicUserDataRequest.onResponse(): END");
    }

    @Override
    public void onFailure(Call<GetUser> call, Throwable t) {
        //Log.d("VIN-566", "GetPublicUserDataRequest.onFailure(): START");
        for (OnResponse r : onResponses) {
            //Log.d("VIN-566", String.format("GetPublicUserDataRequest.onFailure(): exception %s, exec onFailureGetPublicUserDataRequest", t.getMessage()));
            r.onFailureGetPublicUserDataRequest(ErrorHandler.parseError(t));
        }
        //Log.d("VIN-566", "GetPublicUserDataRequest.onFailure(): END");
    }


    public interface OnResponse {
        void onResponseGetPublicUserDataRequest(GetUser user);
        void onFailureGetPublicUserDataRequest(VinclesError error);
    }

}
