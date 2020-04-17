package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserGroupsRequest extends BaseRequest implements Callback<ArrayList<UserGroup>> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserGroupsRequest(RenewTokenFailed listener, String accessToken) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        groupsService = retrofit.create(GroupsService.class);
    }

    public void doRequest() {
        Call<ArrayList<UserGroup>> call = groupsService.getUserGroupList();

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
    public void onResponse(Call<ArrayList<UserGroup>> call, Response<ArrayList<UserGroup>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserGroupsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserGroupsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<UserGroup>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserGroupsRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseGetUserGroupsRequest(ArrayList<UserGroup> userGroups);
        void onFailureGetUserGroupsRequest(Object error);
    }
}
