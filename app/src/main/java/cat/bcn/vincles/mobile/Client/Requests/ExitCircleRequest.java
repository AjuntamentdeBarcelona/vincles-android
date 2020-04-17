package cat.bcn.vincles.mobile.Client.Requests;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExitCircleRequest extends BaseRequest implements Callback<ResponseBody> {

    private int idUserToUnlink;
    private int circleId;
    private CirclesService circlesService;
    private List<ExitCircleRequest.OnResponse> onResponses = new ArrayList<>();

    public ExitCircleRequest(RenewTokenFailed listener, String accesToken, int idUserToUnlink,
                             int circleId) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accesToken);
        this.idUserToUnlink = idUserToUnlink;
        this.circleId = circleId;
        circlesService = retrofit.create(CirclesService.class);
    }

    public void doRequest() {
        Call<ResponseBody> call = circlesService.exitCircle(String.valueOf(circleId));

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    public void addOnOnResponse(ExitCircleRequest.OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (ExitCircleRequest.OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        r.onResponseExitCircleRequest(idUserToUnlink, circleId);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureExitCircleRequest(errorCode, idUserToUnlink, circleId);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (ExitCircleRequest.OnResponse r : onResponses) {
            r.onFailureExitCircleRequest(new Exception(t), idUserToUnlink, circleId);
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseExitCircleRequest(int idUserToUnlink, int circleId);
        void onFailureExitCircleRequest(Object error, int idUserToUnlink, int circleId);
    }
}
