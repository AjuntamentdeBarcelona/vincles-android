package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessageRequest extends BaseRequest implements Callback<ChatMessageSentResponse> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    ChatMessageRest chatMessageRest;

    public SendMessageRequest(RenewTokenFailed listener, ChatMessageRest chatMessageRest) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.chatMessageRest = chatMessageRest;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ChatMessageSentResponse> call = chatService.sendChatMessage(chatMessageRest);

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
    public void onResponse(Call<ChatMessageSentResponse> call, Response<ChatMessageSentResponse> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseSendMessageRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureSendMessageRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ChatMessageSentResponse> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureSendMessageRequest(new Exception(t));
        }
    }



    public interface OnResponse {
        void onResponseSendMessageRequest(ChatMessageSentResponse responseBody);
        void onFailureSendMessageRequest(Object error);
    }
}
