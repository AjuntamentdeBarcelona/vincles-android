package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetCircleUserRequest extends BaseRequest implements Callback<ArrayList<CircleUser>> {

    CirclesService circlesService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetCircleUserRequest(RenewTokenFailed listener) {
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        //circlesService = retrofit.create(CirclesService.class);
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        circlesService = retrofit.create(CirclesService.class);
        Call<ArrayList<CircleUser>> call = circlesService.getCircleUser();

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
    public void onResponse(Call<ArrayList<CircleUser>> call, Response<ArrayList<CircleUser>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetCircleUserRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetCircleUserRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<CircleUser>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetCircleUserRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetCircleUserRequest(ArrayList<CircleUser> circleUsers);
        void onFailureGetCircleUserRequest(Object error);
    }
}
