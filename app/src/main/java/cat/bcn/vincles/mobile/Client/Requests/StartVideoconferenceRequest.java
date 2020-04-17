package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.CallService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartVideoconferenceRequest extends BaseRequest implements Callback<ResponseBody> {

    CallService callService;
    List<OnResponse> onResponses = new ArrayList<>();
    int idUser;
    String idRoom;
    JsonObject jBody;

    public StartVideoconferenceRequest(RenewTokenFailed listener, int idUser, String idRoom) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idUser = idUser;
        this.idRoom = idRoom;
        jBody = new JsonObject();
        jBody.addProperty("idUser", idUser);
        jBody.addProperty("idRoom", idRoom);
        Log.d("callvid", "StartVideoconferenceRequest idRoom:"+idRoom);
    }

    @Override
    public void doRequest(String accessToken) {
        Log.d("startVidConf", "doRequest");
        authenticatedRequest(accessToken);
        callService = retrofit.create(CallService.class);
        Call<ResponseBody> call = callService.startVideoconference(jBody);

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
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseStartVideoconference(idUser, idRoom);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureStartVideoconference(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureStartVideoconference(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseStartVideoconference(int idUser, String idRoom);
        void onFailureStartVideoconference(Object error);
    }
}
