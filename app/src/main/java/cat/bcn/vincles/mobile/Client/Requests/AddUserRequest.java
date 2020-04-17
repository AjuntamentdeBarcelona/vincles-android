package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.Serializers.AddUser;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserRequest extends BaseRequest implements Callback<AddUser> {
    private UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    private JsonObject jBody;
    private String code;
    private String relationship;

    public AddUserRequest(RenewTokenFailed listener, String code, String relationship) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.code = code;
        this.relationship = relationship;
    }

    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        jBody = new JsonObject();
        jBody.addProperty("registerCode", code);
        jBody.addProperty("relationship", this.relationship);
        Call<AddUser> call = userService.associateRegistered(jBody);

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
    public void onResponse(Call<AddUser> call, Response<AddUser> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseAddUser(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureAddUser(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<AddUser> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureAddUser(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseAddUser(AddUser response);
        void onFailureAddUser(Object error);
    }
}
