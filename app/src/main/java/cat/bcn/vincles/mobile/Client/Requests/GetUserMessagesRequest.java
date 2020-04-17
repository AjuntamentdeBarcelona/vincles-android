package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserMessagesRequest extends BaseRequest implements Callback<ArrayList<ChatMessageRest>> {

    private static String FROM = "from";
    private static String TO = "to";

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String idUserSender;
    Map<String, String> params;

    public GetUserMessagesRequest(RenewTokenFailed listener, String idUserSender, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
        this.idUserSender = idUserSender;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ArrayList<ChatMessageRest>> call = chatService.getUserMessages(idUserSender, params);

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
    public void onResponse(Call<ArrayList<ChatMessageRest>> call, Response<ArrayList<ChatMessageRest>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserMessagesRequest(response.body(), idUserSender);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserMessagesRequest(errorCode, idUserSender);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<ChatMessageRest>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserMessagesRequest(new Exception(t), idUserSender);
        }
    }

    public interface OnResponse {
        void onResponseGetUserMessagesRequest(ArrayList<ChatMessageRest> chatMessageRestList,
                                              String idUserSender);
        void onFailureGetUserMessagesRequest(Object error, String idUserSender);
    }
}
