package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetNotificationRequest extends BaseRequest implements Callback<NotificationRest> {

    private static String FROM = "from";
    private static String TO = "to";

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    int idNotification;
    Map<String, String> params;

    public GetNotificationRequest(RenewTokenFailed listener, int idNotification, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
        this.idNotification = idNotification;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<NotificationRest> call = userService.getNotificationsByID(idNotification, params);

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
    public void onResponse(Call<NotificationRest> call, Response<NotificationRest> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetNotificationsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetNotificationsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<NotificationRest> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetNotificationsRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetNotificationsRequest(NotificationRest notificationRest);
        void onFailureGetNotificationsRequest(Object error);
    }
}
