package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupInviteUserToCircle extends BaseRequest implements Callback<ResponseBody> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();
    int groupID;
    int userId;

    public GroupInviteUserToCircle(RenewTokenFailed listener, String accessToken, int groupID,
                                   int userId) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        groupsService = retrofit.create(GroupsService.class);
        this.groupID = groupID;
        this.userId = userId;
    }

    public void doRequest() {
        Call<ResponseBody> call = groupsService.groupInviteUserToCircle(groupID, userId);

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
                    r.onResponseGroupInviteUserToCircle(groupID, userId);
                } else {
                    Log.d("gdp","invite request notSuccessful: "+response.code()+" "+response.body());
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGroupInviteUserToCircle(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.d("gdp","invite request failure: "+t.toString());
        for (OnResponse r : onResponses) {
            r.onFailureGroupInviteUserToCircle(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }


    public interface OnResponse {
        void onResponseGroupInviteUserToCircle(int groupID, int userId);
        void onFailureGroupInviteUserToCircle(Object error);
    }
}
