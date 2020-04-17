package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model. ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMessageRequest extends BaseRequest implements Callback<ChatMessageRest> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String idMessage;

    public GetMessageRequest(RenewTokenFailed listener, String idMessage) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idMessage = idMessage;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ChatMessageRest> call = chatService.getMessage(idMessage);

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
    public void onResponse(Call<ChatMessageRest> call, Response<ChatMessageRest> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetMessageRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetMessageRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ChatMessageRest> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetMessageRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetMessageRequest(ChatMessageRest chatMessageRest);
        void onFailureGetMessageRequest(Object error);
    }
}
