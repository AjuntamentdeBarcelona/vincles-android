package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Business.Firebase.Installation;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetNotificationsRequest extends BaseRequest implements Callback<ArrayList<NotificationRest>> {

    private static String PLATFORM_VERSION = "platform_version";
    private static String FROM = "from";
    private static String TO = "to";

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    Map<String, String> params;

    public GetNotificationsRequest(RenewTokenFailed listener, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        params.put(PLATFORM_VERSION, String.valueOf(Installation.INSTALLATION_PLATFORM_VERSION));

        Long newestProcessedNotificationDate = null;
        NotificationsDb notificationsDb = new NotificationsDb(MyApplication.getAppContext());
        if(notificationsDb.findAllProcessedNotifications().size() > 0){
            newestProcessedNotificationDate = notificationsDb.findAllProcessedNotifications().get(notificationsDb.findAllProcessedNotifications().size() - 1).getCreationTime();
            Log.d("notificationstest from0", String.valueOf(newestProcessedNotificationDate));

        }
        else{
            UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());
            newestProcessedNotificationDate = userPreferences.getServerTime();
            Log.d("notificationstest from1", String.valueOf(newestProcessedNotificationDate));
        }


        Long to = null;

        if(notificationsDb.findAllUnProcessedNotifications().size() > 0){
            to = notificationsDb.findAllUnProcessedNotifications().get(0).getCreationTime();
        }


        params.put(FROM, String.valueOf(newestProcessedNotificationDate + 1));

        if (to != null) {
            params.put(TO, String.valueOf(to));
        }
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<ArrayList<NotificationRest>> call = userService.getNotifications(params);

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
    public void onResponse(Call<ArrayList<NotificationRest>> call, Response<ArrayList<NotificationRest>> response) {
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
        else{
            for (OnResponse r : onResponses) {
                r.onFailureGetNotificationsRequest("-1");
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<NotificationRest>> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetNotificationsRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetNotificationsRequest(ArrayList<NotificationRest> notificationsRestList);
        void onFailureGetNotificationsRequest(Object error);
    }
}
