package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MigrationPostLogin extends BaseRequest implements Callback<ResponseBody> {

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();

    public MigrationPostLogin() {
        super(null, BaseRequest.AUTHENTICATED_REQUEST);
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<ResponseBody> call = userService.migrationPostLogin();

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
                    r.onResponseMigrationPostLogin();
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureMigrationPostLogin(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureMigrationPostLogin(new Exception(t));
        }
    }


    public interface OnResponse {
        void onResponseMigrationPostLogin();
        void onFailureMigrationPostLogin(Object error);
    }

}
