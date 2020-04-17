package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserCircleRequest extends BaseRequest implements Callback<ArrayList<UserCircle>> {

    CirclesService circlesService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserCircleRequest(RenewTokenFailed listener, String accessToken) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        circlesService = retrofit.create(CirclesService.class);
    }

    public void doRequest() {
        Call<ArrayList<UserCircle>> call = circlesService.getUserCircle();

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
    public void onResponse(Call<ArrayList<UserCircle>> call, Response<ArrayList<UserCircle>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserCircleRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserCircleRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<UserCircle>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserCircleRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseGetUserCircleRequest(ArrayList<UserCircle> userCircles);
        void onFailureGetUserCircleRequest(Object error);
    }
}
