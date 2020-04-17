package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupMessageRequest extends BaseRequest implements Callback<GroupMessageRest> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String idMessage;
    private String idChat;

    public GetGroupMessageRequest(RenewTokenFailed listener, String idChat, String idMessage) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idChat = idChat;
        this.idMessage = idMessage;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<GroupMessageRest> call = chatService.getGroupMessage(idChat, idMessage);

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
    public void onResponse(Call<GroupMessageRest> call, Response<GroupMessageRest> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGroupMessageRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupMessageRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GroupMessageRest> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupMessageRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetGroupMessageRequest(GroupMessageRest groupMessageRest);
        void onFailureGetGroupMessageRequest(Object error);
    }
}
