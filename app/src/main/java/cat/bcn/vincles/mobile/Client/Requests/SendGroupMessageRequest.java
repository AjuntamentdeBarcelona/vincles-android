package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.ChatMessagesSentResponse;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendGroupMessageRequest extends BaseRequest implements Callback<ChatMessageSentResponse> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    GroupMessageRest groupMessageRest;
    String idChat;

    public SendGroupMessageRequest(RenewTokenFailed listener, GroupMessageRest groupMessageRest,
                                   String idChat) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.groupMessageRest = groupMessageRest;
        this.idChat = idChat;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ChatMessageSentResponse> call = chatService.sendGroupMessage(groupMessageRest, idChat);

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
                    r.onResponseSendGroupMessageRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureSendGroupMessageRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ChatMessageSentResponse> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureSendGroupMessageRequest(new Exception(t));
        }
    }



    public interface OnResponse {
        void onResponseSendGroupMessageRequest(ChatMessageSentResponse responseBody);
        void onFailureSendGroupMessageRequest(Object error);
    }
}
