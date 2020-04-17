package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetMessageWatchedRequest extends BaseRequest implements Callback<ResponseBody> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    long idMessage = 0;

    public SetMessageWatchedRequest(RenewTokenFailed listener, long idMessage) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idMessage = idMessage;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ResponseBody> call = chatService.setMessageWatched(String.valueOf(idMessage));

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
                    r.onResponseSetMessageWatchedRequest(true);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    Log.d("rntk","onResponse error code:"+errorCode);
                    r.onFailureSetMessageWatchedRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.d("rntk","on failure throwable:"+t.toString());
        for (OnResponse r : onResponses) {
            r.onFailureSetMessageWatchedRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseSetMessageWatchedRequest(boolean result);
        void onFailureSetMessageWatchedRequest(Object error);
    }
}

