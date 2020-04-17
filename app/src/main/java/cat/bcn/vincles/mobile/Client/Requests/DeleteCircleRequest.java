package cat.bcn.vincles.mobile.Client.Requests;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteCircleRequest extends BaseRequest implements Callback<ResponseBody> {

    private int idUserToUnlink;
    private CirclesService circlesService;
    private List<DeleteCircleRequest.OnResponse> onResponses = new ArrayList<>();

    public DeleteCircleRequest(RenewTokenFailed listener, String accesToken, int idUserToUnlink) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accesToken);
        this.idUserToUnlink = idUserToUnlink;
        circlesService = retrofit.create(CirclesService.class);
    }

    public void doRequest() {
        Call<ResponseBody> call = circlesService.deleteCircle(String.valueOf(idUserToUnlink));

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    public void addOnOnResponse(DeleteCircleRequest.OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (DeleteCircleRequest.OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        r.onResponseDeleteCircleRequest(idUserToUnlink);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureDeleteCircleRequest(errorCode, idUserToUnlink);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (DeleteCircleRequest.OnResponse r : onResponses) {
            r.onFailureDeleteCircleRequest(new Exception(t), idUserToUnlink);
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseDeleteCircleRequest(int idUserToUnlink);
        void onFailureDeleteCircleRequest(Object error, int idUserToUnlink);
    }
}
