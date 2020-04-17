package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupMessagesRequest extends BaseRequest implements Callback<ArrayList<GroupMessageRest>> {

    private static String FROM = "from";
    private static String TO = "to";

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String idChat;
    Map<String, String> params;

    public GetGroupMessagesRequest(RenewTokenFailed listener, String idChat, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
        this.idChat = idChat;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ArrayList<GroupMessageRest>> call = chatService.getGroupMessages(idChat, params);

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
    public void onResponse(Call<ArrayList<GroupMessageRest>> call, Response<ArrayList<GroupMessageRest>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGroupMessagesRequest(response.body(), idChat);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupMessagesRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<GroupMessageRest>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupMessagesRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetGroupMessagesRequest(ArrayList<GroupMessageRest> groupMessageRestList, String idChat);
        void onFailureGetGroupMessagesRequest(Object error);
    }
}
