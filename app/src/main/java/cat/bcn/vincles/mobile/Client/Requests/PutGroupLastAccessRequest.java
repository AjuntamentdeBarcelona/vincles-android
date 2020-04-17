package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PutGroupLastAccessRequest extends BaseRequest implements Callback<JsonObject> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String idChat;
    JsonObject jBody;

    public PutGroupLastAccessRequest(RenewTokenFailed listener, String idChat) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);

        this.idChat = idChat;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        jBody = new JsonObject();
        jBody.addProperty("lastAccess", System.currentTimeMillis());
        Call<JsonObject> call = chatService.putGroupLastAcess(idChat, jBody);

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
    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponsePutGroupLastAccessRequest(idChat);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailurePutGroupLastAccessRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<JsonObject> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailurePutGroupLastAccessRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponsePutGroupLastAccessRequest(String idChat);
        void onFailurePutGroupLastAccessRequest(Object error);
    }
}
