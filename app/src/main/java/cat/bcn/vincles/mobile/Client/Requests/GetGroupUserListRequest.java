package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupUserListRequest extends BaseRequest implements Callback<ArrayList<GetUser>> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();
    String groupID;

    public GetGroupUserListRequest(RenewTokenFailed listener, String accessToken, String groupID) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        groupsService = retrofit.create(GroupsService.class);
        this.groupID = groupID;
    }

    public void doRequest() {
        Call<ArrayList<GetUser>> call = groupsService.getGroupUserList(groupID);

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
    public void onResponse(Call<ArrayList<GetUser>> call, Response<ArrayList<GetUser>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGroupUserListRequest(response.body(), groupID);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupUserListRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<GetUser>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupUserListRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }


    public interface OnResponse {
        void onResponseGetGroupUserListRequest(ArrayList<GetUser> userList, String groupID);
        void onFailureGetGroupUserListRequest(Object error);
    }
}
